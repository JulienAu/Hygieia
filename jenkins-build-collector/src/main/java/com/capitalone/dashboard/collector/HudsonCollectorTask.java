package com.capitalone.dashboard.collector;


import com.capitalone.dashboard.model.Build;
import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.HudsonCollector;
import com.capitalone.dashboard.model.HudsonJob;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.BuildRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.HudsonCollectorRepository;
import com.capitalone.dashboard.repository.HudsonJobRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * CollectorTask that fetches Build information from Hudson
 */
@Component
public class HudsonCollectorTask extends CollectorTask<HudsonCollector> {
    private final HudsonCollectorRepository hudsonCollectorRepository;
    private final HudsonJobRepository hudsonJobRepository;
    private final BuildRepository buildRepository;
    private final HudsonClient hudsonClient;
    private final HudsonSettings hudsonSettings;
    private final ComponentRepository dbComponentRepository;

    @Autowired
    public HudsonCollectorTask(TaskScheduler taskScheduler,
                               HudsonCollectorRepository hudsonCollectorRepository,
                               HudsonJobRepository hudsonJobRepository,
                               BuildRepository buildRepository, HudsonClient hudsonClient,
                               HudsonSettings hudsonSettings,
                               ComponentRepository dbComponentRepository) {
        super(taskScheduler, "Hudson");
        this.hudsonCollectorRepository = hudsonCollectorRepository;
        this.hudsonJobRepository = hudsonJobRepository;
        this.buildRepository = buildRepository;
        this.hudsonClient = hudsonClient;
        this.hudsonSettings = hudsonSettings;
        this.dbComponentRepository = dbComponentRepository;
    }

    @Override
    public HudsonCollector getCollector() {
        return HudsonCollector.prototype(hudsonSettings.getServers());
    }

    @Override
    public BaseCollectorRepository<HudsonCollector> getCollectorRepository() {
        return hudsonCollectorRepository;
    }

    @Override
    public String getCron() {
        return hudsonSettings.getCron();
    }

    @Override
    public void collect(HudsonCollector collector) {
        long start = System.currentTimeMillis();

        clean(collector);
        for (String instanceUrl : collector.getBuildServers()) {
            logBanner(instanceUrl);
            
            Map<HudsonJob, Set<Build>> buildsByJob = hudsonClient.getInstanceJobs(instanceUrl);
            log("Fetched jobs", start);

            addNewJobs(buildsByJob.keySet(), collector);

            addNewBuilds(enabledJobs(collector, instanceUrl), buildsByJob);

            log("Finished", start);
        }

    }

    /**
     * Clean up unused hudson/jenkins collector items
     *
     * @param collector the {@link HudsonCollector}
     */

    private void clean(HudsonCollector collector) {

        // First delete jobs that will be no longer collected because servers have moved etc.
        deleteUnwantedJobs(collector);
        Set<ObjectId> uniqueIDs = new HashSet<>();
        for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
                .findAll()) {
            if (comp.getCollectorItems() == null
                    || comp.getCollectorItems().isEmpty()) continue;
            List<CollectorItem> itemList = comp.getCollectorItems().get(
                    CollectorType.Build);
            if (itemList == null) continue;
            for (CollectorItem ci : itemList) {
                if (ci != null
                        && ci.getCollectorId().equals(collector.getId())) {
                    uniqueIDs.add(ci.getId());
                }

            }
        }
        List<HudsonJob> jobList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        for (HudsonJob job : hudsonJobRepository.findByCollectorIdIn(udId)) {
            if (job != null) {
                job.setEnabled(uniqueIDs.contains(job.getId()));
                jobList.add(job);
            }
        }
        hudsonJobRepository.save(jobList);
    }

    private void deleteUnwantedJobs(HudsonCollector collector) {

        List<HudsonJob> deleteJobList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        for (HudsonJob job : hudsonJobRepository.findByCollectorIdIn(udId)) {
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
     * @param enabledJobs list of enabled {@link HudsonJob}s
     * @param buildsByJob maps a {@link HudsonJob} to a set of {@link Build}s.
     */
    private void addNewBuilds(List<HudsonJob> enabledJobs,
                              Map<HudsonJob, Set<Build>> buildsByJob) {
        long start = System.currentTimeMillis();
        int count = 0;

        for (HudsonJob job : enabledJobs) {
        	
            for (Build buildSummary : nullSafe(buildsByJob.get(job))) {

                if (isNewBuild(job, buildSummary)) {
                	logBanner(job.getJobUrl());
                    Build build = hudsonClient.getBuildDetails(buildSummary.getBuildUrl(), job.getJobUrl());
                    if (build != null) {
                        build.setCollectorItemId(job.getId());
                        buildRepository.save(build);
                        count++;
                    }
                }

            }
        }
        log("New builds", start, count);
    }

    private Set<Build> nullSafe(Set<Build> builds) {
        return builds == null ? new HashSet<Build>() : builds;
    }

    /**
     * Adds new {@link HudsonJob}s to the database as disabled jobs.
     *
     * @param jobs      list of {@link HudsonJob}s
     * @param collector the {@link HudsonCollector}
     */
    private void addNewJobs(Set<HudsonJob> jobs, HudsonCollector collector) {
        long start = System.currentTimeMillis();
        int count = 0;

        for (HudsonJob job : jobs) {

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

    private List<HudsonJob> enabledJobs(HudsonCollector collector,
                                        String instanceUrl) {
        return hudsonJobRepository.findEnabledHudsonJobs(collector.getId(),
                instanceUrl);
    }

    private boolean isNewJob(HudsonCollector collector, HudsonJob job) {
        return hudsonJobRepository.findHudsonJob(collector.getId(),
                job.getInstanceUrl(), job.getJobName()) == null;
    }

    private boolean isNewBuild(HudsonJob job, Build build) {
        return buildRepository.findByCollectorItemIdAndNumber(job.getId(),
                build.getNumber()) == null;
    }
}
