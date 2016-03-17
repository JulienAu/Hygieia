package com.capitalone.dashboard.service;

import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.request.Build2Request;

public interface Build2Service {

    /**
     * Finds all of the Builds matching the specified request criteria.
     *
     * @param request search criteria
     * @return builds matching criteria
     */
    DataResponse<Iterable<Build2>> search(Build2Request request);
}
