package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.Hudson2Job;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;


public interface Hudson2JobRepository extends BaseCollectorItemRepository<Hudson2Job> {

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, options.jobName : ?2}")
    Hudson2Job findHudsonJob(ObjectId collectorId, String instanceUrl, String jobName);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<Hudson2Job> findEnabledHudsonJobs(ObjectId collectorId, String instanceUrl);
}
