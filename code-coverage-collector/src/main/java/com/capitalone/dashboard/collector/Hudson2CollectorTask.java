package com.capitalone.dashboard.collector;


import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Hudson2Collector;
import com.capitalone.dashboard.model.Hudson2Job;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.Build2Repository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.Hudson2CollectorRepository;
import com.capitalone.dashboard.repository.Hudson2JobRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * CollectorTask that fetches Build information from Hudson
 */
@Component
public class Hudson2CollectorTask extends CollectorTask<Hudson2Collector> {
	private static final Logger LOG = LoggerFactory.getLogger(Hudson2CollectorTask.class);
	
	private final Hudson2CollectorRepository hudsonCollectorRepository;
	private final Hudson2JobRepository hudsonJobRepository;
	private final Build2Repository buildRepository;
	private final Hudson2Client hudsonClient;
	private final Hudson2Settings hudsonSettings;
	private final ComponentRepository dbComponentRepository;

	@Autowired
	public Hudson2CollectorTask(TaskScheduler taskScheduler,
			Hudson2CollectorRepository hudsonCollectorRepository,
			Hudson2JobRepository hudsonJobRepository,
			Build2Repository buildRepository, Hudson2Client hudsonClient,
			Hudson2Settings hudsonSettings,
			ComponentRepository dbComponentRepository) {
		super(taskScheduler, "Hudson2");
		this.hudsonCollectorRepository = hudsonCollectorRepository;
		this.hudsonJobRepository = hudsonJobRepository;
		this.buildRepository = buildRepository;
		this.hudsonClient = hudsonClient;
		this.hudsonSettings = hudsonSettings;
		this.dbComponentRepository = dbComponentRepository;
	}

	@Override
	public Hudson2Collector getCollector() {
		return Hudson2Collector.prototype(hudsonSettings.getServers());
	}

	@Override
	public BaseCollectorRepository<Hudson2Collector> getCollectorRepository() {
		return hudsonCollectorRepository;
	}

	@Override
	public String getCron() {
		return hudsonSettings.getCron();
	}

	@Override
	public void collect(Hudson2Collector collector) throws MalformedURLException, IOException {
		long start = System.currentTimeMillis();

		clean(collector);
		for (String instanceUrl : collector.getBuildServers()) {
			logBanner(instanceUrl);

			Map<Hudson2Job, Set<Build2>> buildsByJob = hudsonClient.getInstanceJobs(instanceUrl);
			log("Fetched jobs", start);

			addNewJobs(buildsByJob.keySet(), collector);

			addNewBuilds(enabledJobs(collector, instanceUrl), buildsByJob);

			log("Finished", start);
		}

	}

	/**
	 * Clean up unused hudson/jenkins collector items
	 *
	 * @param collector the {@link Hudson2Collector}
	 */

	protected void clean(Hudson2Collector collector) {

		// First delete jobs that will be no longer collected because servers have moved etc.
		deleteUnwantedJobs(collector);
		Set<ObjectId> uniqueIDs = uniqueIDs();
		for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
				.findAll()) {
			if (comp.getCollectorItems() == null
					|| comp.getCollectorItems().isEmpty()) continue;
			List<CollectorItem> itemList = comp.getCollectorItems().get(
					CollectorType.Build2);
			if (itemList == null) continue;
			for (CollectorItem ci : itemList) {
				if (ci != null
						&& ci.getCollectorId().equals(collector.getId())) {
					uniqueIDs.add(ci.getId());
				}

			}
		}
		List<Hudson2Job> jobList = new ArrayList<>();
		Set<ObjectId> udId = new HashSet<>();
		udId.add(collector.getId());
		for (Hudson2Job job : hudsonJobRepository.findByCollectorIdIn(udId)) {
			if (job != null) {
				job.setEnabled(uniqueIDs.contains(job.getId()));
				jobList.add(job);
			}
		}
		hudsonJobRepository.save(jobList);
	}

	private void deleteUnwantedJobs(Hudson2Collector collector) {

		List<Hudson2Job> deleteJobList = new ArrayList<>();
		Set<ObjectId> udId = new HashSet<>();
		udId.add(collector.getId());
		for (Hudson2Job job : hudsonJobRepository.findByCollectorIdIn(udId)) {
			if (!collector.getBuildServers().contains(job.getInstanceUrl()) ||
					(!job.getCollectorId().equals(collector.getId()))) {
				deleteJobList.add(job);
			}
		}

		hudsonJobRepository.delete(deleteJobList);

	}

	/**
	 * Iterates over the enabled build jobs and adds new builds to the database.
	 *
	 * @param enabledJobs list of enabled {@link Hudson2Job}s
	 * @param buildsByJob maps a {@link Hudson2Job} to a set of {@link Build}s.
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private void addNewBuilds(List<Hudson2Job> enabledJobs,
			Map<Hudson2Job, Set<Build2>> buildsByJob) throws MalformedURLException, IOException {
		long start = System.currentTimeMillis();
		int count = 0;

		for (Hudson2Job job : enabledJobs){
			try {


				for (Build2 buildSummary : nullSafe(buildsByJob.get(job))) {

					if (isNewBuild(job, buildSummary)) {
						logBanner(job.getJobUrl());
						Build2 build = hudsonClient.getBuildDetails(buildSummary.getBuildUrl(), job.getJobUrl());
						if (build != null) {
							build.setCollectorItemId(job.getId());
							buildRepository.save(build);
							count++;
						}
					}

				}
			} catch (Exception e) {
				LOG.info("Erreur lors du addNewBuild");
			}
		}

		log("New builds", start, count);
	}

	private Set<Build2> nullSafe(Set<Build2> builds) {
		return builds == null ? new HashSet<Build2>() : builds;
	}

	/**
	 * Adds new {@link Hudson2Job}s to the database as disabled jobs.
	 *
	 * @param jobs      list of {@link Hudson2Job}s
	 * @param collector the {@link Hudson2Collector}
	 */
	private void addNewJobs(Set<Hudson2Job> jobs, Hudson2Collector collector) {
		long start = System.currentTimeMillis();
		int count = 0;

		for (Hudson2Job job : jobs) {

			if (isNewJob(collector, job)) {
				job.setCollectorId(collector.getId());
				job.setEnabled(false); // Do not enable for collection. Will be
				// enabled when added to dashboard
				job.setDescription(job.getJobName());
				hudsonJobRepository.save(job);
				count++;
			}

		}
		log("New jobs", start, count);
	}

	private List<Hudson2Job> enabledJobs(Hudson2Collector collector,
			String instanceUrl) {
		return hudsonJobRepository.findEnabledHudsonJobs(collector.getId(),
				instanceUrl);
	}

	private boolean isNewJob(Hudson2Collector collector, Hudson2Job job) {
		return hudsonJobRepository.findHudsonJob(collector.getId(),
				job.getInstanceUrl(), job.getJobName()) == null;
	}

	private boolean isNewBuild(Hudson2Job job, Build2 build) {
		return buildRepository.findByCollectorItemIdAndNumber(job.getId(),
				build.getNumber()) == null;
	}
	
	/** Help for Unit test**/
	protected Set<ObjectId> uniqueIDs (){
		return new HashSet<>();
	}
}
