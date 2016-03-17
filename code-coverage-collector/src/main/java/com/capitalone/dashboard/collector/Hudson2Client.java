package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.Hudson2Job;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;

/**
 * Client for fetching job and build information from Hudson
 */
public interface Hudson2Client {

    /**
     * Finds all of the configured jobs for a given instance and returns the set of
     * builds for each job. At a minimum, the number and url of each Build will be
     * populated.
     *
     * @param instanceUrl the URL for the Hudson instance
     * @return a summary of every build for each job on the instance
     */
    Map<Hudson2Job, Set<Build2>> getInstanceJobs(String instanceUrl);

    /**
     * Fetch full populated build information for a build.
     *
     * @param buildUrl the url of the build
     * @param jobUrl TODO
     * @return a Build instance or null
     * @throws MalformedURLException 
     * @throws IOException 
     */
    Build2 getBuildDetails(String buildUrl, String jobUrl) throws MalformedURLException, IOException;
}
