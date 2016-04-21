package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.Hudson2Collector;
import com.capitalone.dashboard.model.Hudson2Job;
import com.capitalone.dashboard.repository.Build2Repository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.Hudson2CollectorRepository;
import com.capitalone.dashboard.repository.Hudson2JobRepository;
import com.google.common.collect.Sets;

import org.bson.types.ObjectId;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HudsonCollectorTaskTests {

    @Mock private TaskScheduler taskScheduler;
    @Mock private Hudson2CollectorRepository hudsonCollectorRepository;
    @Mock private Hudson2JobRepository hudsonJobRepository;
    @Mock private Build2Repository buildRepository;
    @Mock private Hudson2Client hudsonClient;
    @Mock private Hudson2Settings hudsonSettings;
    @Mock private ComponentRepository dbComponentRepository;

    @InjectMocks private Hudson2CollectorTask task;

    private static final String SERVER1 = "http://jenkins.net";

    @Test
    public void collect_noBuildServers_nothingAdded() throws MalformedURLException, IOException {
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(new Hudson2Collector());
        verifyZeroInteractions(hudsonClient, buildRepository);
    }

    @Test
    public void collect_noJobsOnServer_nothingAdded() throws MalformedURLException, IOException {
        when(hudsonClient.getInstanceJobs(SERVER1)).thenReturn(new HashMap<Hudson2Job, Set<Build2>>());
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collectorWithOneServer());

        verify(hudsonClient).getInstanceJobs(SERVER1);
        verifyNoMoreInteractions(hudsonClient, buildRepository);
    }

    @Test
    public void collect_twoJobs_jobsAdded() throws MalformedURLException, IOException {
        when(hudsonClient.getInstanceJobs(SERVER1)).thenReturn(twoJobsWithTwoBuilds(SERVER1));
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collectorWithOneServer());

        verify(hudsonJobRepository, times(2)).save(any(Hudson2Job.class));
    }

    @Test
    public void collect_oneJob_exists_notAdded() throws MalformedURLException, IOException {
        Hudson2Collector collector = collectorWithOneServer();
        Hudson2Job job = hudsonJob("JOB1", SERVER1, "JOB1_URL");
        when(hudsonClient.getInstanceJobs(SERVER1)).thenReturn(oneJobWithBuilds(job));
        when(hudsonJobRepository.findHudsonJob(collector.getId(), SERVER1, job.getJobName()))
                .thenReturn(job);
        when(dbComponentRepository.findAll()).thenReturn(components());

        task.collect(collector);

        verify(hudsonJobRepository, never()).save(job);
    }

    @Test
    public void collect_jobNotEnabled_buildNotAdded() throws MalformedURLException, IOException {
        Hudson2Collector collector = collectorWithOneServer();
        Hudson2Job job = hudsonJob("JOB1", SERVER1, "JOB1_URL");
        Build2 build = build("JOB1_1", "JOB1_1_URL");

        when(hudsonClient.getInstanceJobs(SERVER1)).thenReturn(oneJobWithBuilds(job, build));
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collector);

        verify(buildRepository, never()).save(build);
    }

    @Test
    public void collect_jobEnabled_buildExists_buildNotAdded() throws MalformedURLException, IOException {
        Hudson2Collector collector = collectorWithOneServer();
        Hudson2Job job = hudsonJob("JOB1", SERVER1, "JOB1_URL");
        Build2 build = build("JOB1_1", "JOB1_1_URL");

        when(hudsonClient.getInstanceJobs(SERVER1)).thenReturn(oneJobWithBuilds(job, build));
        when(hudsonJobRepository.findEnabledHudsonJobs(collector.getId(), SERVER1))
                .thenReturn(Arrays.asList(job));
        when(buildRepository.findByCollectorItemIdAndNumber(job.getId(), build.getNumber())).thenReturn(build);
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collector);

        verify(buildRepository, never()).save(build);
    }

    @Ignore @Test
    public void collect_jobEnabled_newBuild_buildAdded() throws MalformedURLException, IOException {
        Hudson2Collector collector = collectorWithOneServer();
        Hudson2Job job = hudsonJob("Test", SERVER1, "http://jenkins.net/job/Test");
        job.setId(ObjectId.get());
        Build2 build = build("383", "http://jenkins.net/job/Test");

        when(hudsonClient.getInstanceJobs(SERVER1)).thenReturn(oneJobWithBuilds(job, build));
        when(hudsonJobRepository.findEnabledHudsonJobs(collector.getId(), SERVER1))
                .thenReturn(Arrays.asList(job));
        when(buildRepository.findByCollectorItemIdAndNumber(job.getId(), build.getNumber())).thenReturn(null);
        when(hudsonClient.getBuildDetails(build.getBuildUrl(), null)).thenReturn(build);
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collector);

        verify(buildRepository, times(1)).save(build);
    }

    private Hudson2Collector collectorWithOneServer() {
        return Hudson2Collector.prototype(Arrays.asList(SERVER1));
    }

    private Map<Hudson2Job, Set<Build2>> oneJobWithBuilds(Hudson2Job job, Build2... builds) {
        Map<Hudson2Job, Set<Build2>> jobs = new HashMap<>();
        jobs.put(job, Sets.newHashSet(builds));
        return jobs;
    }

    private Map<Hudson2Job, Set<Build2>> twoJobsWithTwoBuilds(String server) {
        Map<Hudson2Job, Set<Build2>> jobs = new HashMap<>();
        jobs.put(hudsonJob("JOB1", server, "JOB1_URL"), Sets.newHashSet(build("JOB1_1", "JOB1_1_URL"), build("JOB1_2", "JOB1_2_URL")));
        jobs.put(hudsonJob("JOB2", server, "JOB2_URL"), Sets.newHashSet(build("JOB2_1", "JOB2_1_URL"), build("JOB2_2", "JOB2_2_URL")));
        return jobs;
    }

    private Hudson2Job hudsonJob(String jobName, String instanceUrl, String jobUrl) {
        Hudson2Job job = new Hudson2Job();
        job.setJobName(jobName);
        job.setInstanceUrl(instanceUrl);
        job.setJobUrl(jobUrl);
        return job;
    }

    private Build2 build(String number, String url) {
        Build2 build = new Build2();
        build.setNumber(number);
        build.setBuildUrl(url);
        return build;
    }

    private ArrayList<com.capitalone.dashboard.model.Component> components() {
    	ArrayList<com.capitalone.dashboard.model.Component> cArray = new ArrayList<com.capitalone.dashboard.model.Component>();
    	com.capitalone.dashboard.model.Component c = new Component();
    	c.setId(new ObjectId());
    	c.setName("COMPONENT1");
    	c.setOwner("JOHN");
    	cArray.add(c);
    	return cArray;
    }
}
