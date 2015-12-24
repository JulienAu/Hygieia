package com.capitalone.dashboard.client.project;

import com.capitalone.dashboard.model.Scope;
import com.capitalone.dashboard.repository.FeatureCollectorRepository;
import com.capitalone.dashboard.repository.ProjectRepository;
import com.capitalone.dashboard.util.ClientUtil;
import com.capitalone.dashboard.util.FeatureSettings;
import com.capitalone.dashboard.util.FeatureWidgetQueries;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This is the primary implemented/extended data collector for the feature
 * collector. This will get data from the source system, but will grab the
 * majority of needed data and aggregate it in a single, flat MongoDB collection
 * for consumption.
 *
 * @author kfk884
 *
 */
public class ProjectDataClientImpl extends ProjectDataClientSetupImpl implements
		ProjectDataClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDataClientImpl.class);

	private final FeatureSettings featureSettings;
	private final FeatureWidgetQueries featureWidgetQueries;
	private final ProjectRepository projectRepo;
	private final static ClientUtil TOOLS = new ClientUtil();

	/**
	 * Extends the constructor from the super class.
	 *
	 * @param teamRepository
	 */
	public ProjectDataClientImpl(FeatureSettings featureSettings,
			ProjectRepository projectRepository, FeatureCollectorRepository featureCollectorRepository) {
		super(featureSettings, projectRepository, featureCollectorRepository);
		LOGGER.debug("Constructing data collection for the feature widget, project-level data...");

		this.featureSettings = featureSettings;
		this.projectRepo = projectRepository;
		this.featureWidgetQueries = new FeatureWidgetQueries(
				this.featureSettings);
	}

	/**
	 * Updates the MongoDB with a JSONArray received from the source system
	 * back-end with story-based data.
	 *
	 * @param tmpMongoDetailArray
	 *            A JSON response in JSONArray format from the source system
	 * @param featureCollector
	 */
	protected void updateMongoInfo(JSONArray tmpMongoDetailArray) {
		try {
			JSONObject dataMainObj = new JSONObject();
			for (int i = 0; i < tmpMongoDetailArray.size(); i++) {
				if (dataMainObj != null) {
					dataMainObj.clear();
				}
				dataMainObj = (JSONObject) tmpMongoDetailArray.get(i);
				Scope scope = new Scope();

				@SuppressWarnings("unused")
				boolean deleted = this.removeExistingEntity(
						TOOLS.sanitizeResponse((String) dataMainObj.get("_oid")));

				// collectorId
				scope.setCollectorId(featureCollectorRepository
						.findByName("Jira").getId());

				// ID;
				scope.setpId(TOOLS.sanitizeResponse(dataMainObj.get("id")));

				// name;
				scope.setName(TOOLS.sanitizeResponse(dataMainObj.get("name")));

				// beginDate - does not exist for jira
				scope.setBeginDate("");

				// endDate - does not exist for jira
				scope.setEndDate("");

				// changeDate - does not exist for jira
				scope.setChangeDate("");

				// assetState - does not exist for jira
				scope.setAssetState("Active");

				// isDeleted - does not exist for jira
				scope.setIsDeleted("False");

				// path - does not exist for Jira
				scope.setProjectPath(TOOLS.sanitizeResponse(dataMainObj
						.get("name")));

				try {
					projectRepo.save(scope);
				} catch (Exception e) {
					LOGGER.error("Unexpected error caused when attempting to save data\nCaused by:\n"
							+ e.getMessage()
							+ " : "
							+ e.getCause()
							+ "\n"
							+ Arrays.toString(e.getStackTrace()));
				}
			}
		} catch (Exception e) {
			LOGGER.error("Unexpected error caused while mapping data from source system to local data store:\n"
					+ e.getMessage()
					+ " : "
					+ e.getCause()
					+ "\n"
					+ Arrays.toString(e.getStackTrace()));
		}
	}

	/**
	 * Explicitly updates queries for the source system, and initiates the
	 * update to MongoDB from those calls.
	 */
	public void updateProjectInformation() {
		super.objClass = Scope.class;
		super.returnDate = this.featureSettings.getDeltaStartDate();
		if (super.getMaxChangeDate() != null) {
			super.returnDate = super.getMaxChangeDate();
		}
		super.returnDate = getChangeDateMinutePrior(super.returnDate);
		String queryName = this.featureSettings.getProjectQuery();
		super.query = this.featureWidgetQueries.getQuery(queryName);
		LOGGER.debug("updateProjectInformation: queryName = " + query + "; query = " + query);
		updateObjectInformation();
	}

	/**
	 * Validates current entry and removes new entry if an older item exists in
	 * the repo
	 *
	 * @param A
	 *            local repository item ID (not the precise mongoID)
	 */
	protected Boolean removeExistingEntity(String localId) {
		boolean deleted = false;

		try {
			ObjectId tempEntId = projectRepo.getProjectIdById(localId).get(0)
					.getId();
			if (localId.equalsIgnoreCase(projectRepo.getProjectIdById(localId)
					.get(0).getpId())) {
				projectRepo.delete(tempEntId);
				deleted = true;
			}
		} catch (IndexOutOfBoundsException ioobe) {
			LOGGER.debug("Nothing matched the redundancy checking from the database", ioobe);
		} catch (Exception e) {
			LOGGER.error("There was a problem validating the redundancy of the data model", e);
		}

		return deleted;
	}
}
