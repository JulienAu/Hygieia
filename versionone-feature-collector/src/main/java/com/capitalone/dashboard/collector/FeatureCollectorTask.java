package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.client.project.ProjectDataClientImpl;
import com.capitalone.dashboard.client.story.StoryDataClientImpl;
import com.capitalone.dashboard.client.team.TeamDataClientImpl;
import com.capitalone.dashboard.datafactory.versionone.VersionOneDataFactoryImpl;
import com.capitalone.dashboard.model.FeatureCollector;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.FeatureCollectorRepository;
import com.capitalone.dashboard.repository.FeatureRepository;
import com.capitalone.dashboard.repository.ProjectRepository;
import com.capitalone.dashboard.repository.TeamRepository;
import com.capitalone.dashboard.util.FeatureSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects {@link FeatureCollector} data from feature content source system.
 *
 * @author KFK884
 */
@Component
public class FeatureCollectorTask extends CollectorTask<FeatureCollector> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureCollectorTask.class);

	private final FeatureRepository featureRepository;
	private final TeamRepository teamRepository;
	private final ProjectRepository projectRepository;
	private final FeatureCollectorRepository featureCollectorRepository;
	private final FeatureSettings featureSettings;
	private final VersionOneDataFactoryImpl v1Connection;

	/**
	 * Default constructor for the collector task. This will construct this
	 * collector task with all repository, scheduling, and settings
	 * configurations custom to this collector.
	 *
	 * @param taskScheduler
	 *            A task scheduler artifact
	 * @param teamRepository
	 *            The repository being use for feature collection
	 * @param featureSettings
	 *            The settings being used for feature collection from the source
	 *            system
	 */
	@Autowired
	@SuppressWarnings("unused")
	public FeatureCollectorTask(TaskScheduler taskScheduler,
			FeatureRepository featureRepository, TeamRepository teamRepository,
			ProjectRepository projectRepository,
			FeatureCollectorRepository featureCollectorRepository,
			FeatureSettings featureSettings,VersionOneDataFactoryImpl v1Connection) {
		super(taskScheduler, "VersionOne");
		this.featureCollectorRepository = featureCollectorRepository;
		this.teamRepository = teamRepository;
		this.projectRepository = projectRepository;
		this.featureRepository = featureRepository;
		this.featureSettings = featureSettings;

		this.v1Connection = connectToPersistentClient();
	}

	/**
	 * Accessor method for the collector prototype object
	 */
	@Override
	public FeatureCollector getCollector() {
		return FeatureCollector.prototype();
	}

	/**
	 * Accessor method for the collector repository
	 */
	@Override
	public BaseCollectorRepository<FeatureCollector> getCollectorRepository() {
		return featureCollectorRepository;
	}

	/**
	 * Accessor method for the current chronology setting, for the scheduler
	 */
	@Override
	public String getCron() {
		return featureSettings.getCron();
	}

	/**
	 * The collection action. This is the task which will run on a schedule to
	 * gather data from the feature content source system and update the
	 * repository with retrieved data.
	 */
	@Override
	public void collect(FeatureCollector collector) {
		LOGGER.info("Starting Feature collection...");

		TeamDataClientImpl teamData = new TeamDataClientImpl(
				this.featureCollectorRepository, this.featureSettings,
				this.teamRepository, this.v1Connection);
		teamData.updateTeamInformation();

		ProjectDataClientImpl projectData = new ProjectDataClientImpl(
				this.featureSettings, this.projectRepository,
				this.featureCollectorRepository, this.v1Connection);
		projectData.updateProjectInformation();

		StoryDataClientImpl storyData = new StoryDataClientImpl(
				this.featureSettings, this.featureRepository,
				this.featureCollectorRepository, this.v1Connection);
		storyData.updateStoryInformation();

		LOGGER.info("Feature Data Collection Finished");
	}

	private VersionOneDataFactoryImpl connectToPersistentClient() {
		Map<String, String> auth = new HashMap<>();
		auth.put("v1ProxyUrl", this.featureSettings.getVersionOneProxyUrl());
		auth.put("v1BaseUri", this.featureSettings.getVersionOneBaseUri());
		auth.put("v1AccessToken",
				this.featureSettings.getVersionOneAccessToken());

		return new VersionOneDataFactoryImpl(auth);
	}
}
