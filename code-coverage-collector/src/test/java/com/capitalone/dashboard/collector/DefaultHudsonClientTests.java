package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.Hudson2Job;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultHudsonClientTests {

    @Mock private Supplier<RestOperations> restOperationsSupplier;
    @Mock private RestOperations rest;
    private Hudson2Settings settings;
    private Hudson2Client hudsonClient;
    private DefaultHudson2Client defaultHudsonClient;

    private static final String URL_TEST = "URL";

    @Before
    public void init() {
        when(restOperationsSupplier.get()).thenReturn(rest);
        settings = new Hudson2Settings();
        hudsonClient = defaultHudsonClient = new DefaultHudson2Client(restOperationsSupplier,
                settings);
    }

    @Test
    public void joinURLsTest() throws Exception {
        String u = DefaultHudson2Client.joinURL("http://jenkins.com",
                "/api/json?tree=jobs[name,url,builds[number,url]]");
        assertEquals("http://jenkins.com/api/json?tree=jobs[name,url,builds[number,url]]", u);

        String u4 = DefaultHudson2Client.joinURL("http://jenkins.com/", "test",
                "/api/json?tree=jobs[name,url,builds[number,url]]");
        assertEquals("http://jenkins.com/test/api/json?tree=jobs[name,url,builds[number,url]]", u4);

        String u2 = DefaultHudson2Client.joinURL("http://jenkins.com/", "/test/",
                "/api/json?tree=jobs[name,url,builds[number,url]]");
        assertEquals("http://jenkins.com/test/api/json?tree=jobs[name,url,builds[number,url]]", u2);

        String u3 = DefaultHudson2Client.joinURL("http://jenkins.com", "///test",
                "/api/json?tree=jobs[name,url,builds[number,url]]");
        assertEquals("http://jenkins.com/test/api/json?tree=jobs[name,url,builds[number,url]]", u3);
    }

    @Test
    public void verifyBasicAuth() throws Exception {
        HttpHeaders headers = defaultHudsonClient.createHeaders("Aladdin:open sesame");
        assertEquals("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==",
                headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    public void verifyAuthCredentials() throws Exception {
        HttpEntity<Object> headers = new HttpEntity<Object>(defaultHudsonClient.createHeaders("user:pass"));
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        settings.setApiKey("doesnt");
        settings.setUsername("matter");
        defaultHudsonClient.makeRestCall("http://user:pass@jenkins.com");
        verify(rest).exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class));
    }

    @Test
    public void verifyAuthCredentialsBySettings() throws Exception {
        HttpEntity<Object> headers = new HttpEntity<Object>(defaultHudsonClient.createHeaders("does:matter"));
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        settings.setApiKey("matter");
        settings.setUsername("does");
        defaultHudsonClient.makeRestCall("http://jenkins.com");
        verify(rest).exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class));
    }

    @Test
    public void verifyGetLogUrl() throws Exception {
        HttpEntity<Object> headers = new HttpEntity<Object>(defaultHudsonClient.createHeaders("does:matter"));
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        settings.setApiKey("matter");
        settings.setUsername("does");
        defaultHudsonClient.getLog("http://jenkins.com");
        verify(rest).exchange(eq(URI.create("http://jenkins.com/consoleText")), eq(HttpMethod.GET),
                eq(headers), eq(String.class));
    }

    @Test
    public void instanceJobs_emptyResponse_returnsEmptyMap() {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<String>("", HttpStatus.OK));

        Map<Hudson2Job, Set<Build2>> jobs = hudsonClient.getInstanceJobs(URL_TEST);

        assertThat(jobs.size(), is(0));
    }

    @Test
    public void instanceJobs_twoJobsTwoBuilds() throws Exception {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<String>(getJson("instanceJobs_twoJobsTwoBuilds.json"), HttpStatus.OK));

        Map<Hudson2Job, Set<Build2>> jobs = hudsonClient.getInstanceJobs(URL_TEST);

        assertThat(jobs.size(), is(2));
        Iterator<Hudson2Job> jobIt = jobs.keySet().iterator();

        //First job
        Hudson2Job job = jobIt.next();
        assertJob(job, "job1", "http://server/job/job1/");

        Iterator<Build2> buildIt = jobs.get(job).iterator();
        assertBuild(buildIt.next(),"2", "http://server/job/job1/2/");
        assertBuild(buildIt.next(),"1", "http://server/job/job1/1/");
        assertThat(buildIt.hasNext(), is(false));

        //Second job
        job = jobIt.next();
        assertJob(job, "job2", "http://server/job/job2/");

        buildIt = jobs.get(job).iterator();
        assertBuild(buildIt.next(),"2", "http://server/job/job2/2/");
        assertBuild(buildIt.next(),"1", "http://server/job/job2/1/");
        assertThat(buildIt.hasNext(), is(false));

        assertThat(jobIt.hasNext(), is(false));
    }

    @Ignore @Test
    public void buildDetails_full() throws Exception {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(getJson("buildDetails_full.json"), HttpStatus.OK));

        Build2 build = hudsonClient.getBuildDetails(URL_TEST, null);

        assertThat(build.getTimestamp(), notNullValue());
        assertThat(build.getNumber(), is("2483"));
        assertThat(build.getBuildUrl(), is(URL_TEST));

    }

    private void assertBuild(Build2 build, String number, String url) {
        assertThat(build.getNumber(), is(number));
        assertThat(build.getBuildUrl(), is(url));
    }

    private String getJson(String fileName) throws IOException {
        InputStream inputStream = DefaultHudsonClientTests.class.getResourceAsStream(fileName);
        return IOUtils.toString(inputStream);
    }

    private void assertJob(Hudson2Job job, String name, String url) {
        assertThat(job.getJobName(), is(name));
        assertThat(job.getJobUrl(), is(url));
    }
}
