package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.Build2;
import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for {@link Build} data.
 */
public interface Build2Repository extends CrudRepository<Build2, ObjectId>, QueryDslPredicateExecutor<Build2> {

    /**
     * Finds the {@link Build2} with the given number for a specific {@link com.capitalone.dashboard.model.CollectorItem}.
     *
     * @param collectorItemId collector item id
     * @param number build number
     * @return a {@link Build2}
     */
    Build2 findByCollectorItemIdAndNumber(ObjectId collectorItemId, String number);
}
