package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CodeQuality;
import com.capitalone.dashboard.model.CodeQualityMetric;
import com.capitalone.dashboard.model.CodeQualityMetricStatus;
import com.capitalone.dashboard.model.CodeQualityType;
import com.capitalone.dashboard.model.SonarProject;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultSonarClientTests {

    @Mock private Supplier<RestOperations> restOperationsSupplier;
    @Mock private RestOperations rest;
    private SonarSettings settings;
    private SonarClient SonarClient;
    private DefaultSonarClient defaultSonarClient;

    private static final String URL_RESOURCES = "/api/resources?format=json";
    private static final String URL_TEST = "URL";
    private static final String URL_TEST2 = "http://jenkins.net/job/Test/383/";

    @Before
    public void init() {
        when(restOperationsSupplier.get()).thenReturn(rest);
        settings = new SonarSettings();
        SonarClient = defaultSonarClient = new DefaultSonarClient(restOperationsSupplier,
                settings);
        
        
    }

    @Test
    public void getProjectsTest() throws Exception {
    	List<SonarProject> projects = new ArrayList<>();
    	when(rest.getForObject("http://sonar.net"+URL_RESOURCES, String.class))
        .thenReturn(getJson("Projects.html"));
    	projects=defaultSonarClient.getProjects("http://sonar.net");
    	
    	assertEquals(projects.size() , 2);
    	assertEquals(projects.get(0).getInstanceUrl() , "http://sonar.net");
    	assertEquals(projects.get(0).getProjectName() , "myproject");
    	assertEquals(projects.get(0).getProjectId() , "6322");
    	
    	
    }
    
    @Test
    public void currentCodeQuality() throws Exception {
    	SonarProject project = new SonarProject();
    	project.setProjectId("6322");
    	project.setProjectName("myproject");
    	
    	when(rest.getForObject(Matchers.any(String.class), eq(String.class)))
        .thenReturn(getJson("CodeQuality.html"));
    	
    	CodeQuality quality = defaultSonarClient.currentCodeQuality(project);
    	
    	assertEquals(quality.getName() , "myproject");
    	//assertEquals(quality.getTimestamp() , 1461688802000);
    	assertEquals(quality.getType() , CodeQualityType.StaticAnalysis);
    	assertEquals(quality.getVersion() , "199");
    	assertEquals(quality.getMetrics().size() , 13);
    	
    	
    }
    
    
    @Test
    public void currentCodeQualityEmpty() throws Exception {
    	SonarProject project = new SonarProject();
    	project.setProjectId("6322");
    	project.setProjectName("myproject");
    	
    	when(rest.getForObject(Matchers.any(String.class), eq(String.class)))
        .thenReturn(getJson(""));
    	
    	assertEquals(defaultSonarClient.currentCodeQuality(project) , null);
    	
 
    	
    	
    }
    
    @Test
    public void metricStatus(){

    	String ok ="";
    	String warn = "WARN";
    	String error= "ALERT"; 
    	CodeQualityMetricStatus code = defaultSonarClient.metricStatus(ok);
    	assertEquals(code , CodeQualityMetricStatus.Ok);
    	code = defaultSonarClient.metricStatus(warn);
    	assertEquals(code , CodeQualityMetricStatus.Warning);
    	code = defaultSonarClient.metricStatus(error);
    	assertEquals(code , CodeQualityMetricStatus.Alert);
    	
    }
    
    private String getJson(String fileName) throws IOException {
        InputStream inputStream = DefaultSonarClientTests.class.getResourceAsStream(fileName);
        return IOUtils.toString(inputStream);
    }
    
    /*

    @Test
    public void verifyBasicAuth() throws Exception {
        @SuppressWarnings("unused")
		URL u = new URL(new URL("http://jenkins.com"), "/api/json?tree=jobs[name,url," +
                "CodeQualitys[number,url]]");

        HttpHeaders headers = defaultSonarClient.createHeaders("Aladdin:open sesame");
        assertEquals("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==",
                headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void verifyAuthCredentials() throws Exception {
        @SuppressWarnings("rawtypes")
		HttpEntity headers = new HttpEntity(defaultSonarClient.createHeaders("user:pass"));
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        settings.setApiKey("doesnt");
        settings.setUsername("matter");
        defaultSonarClient.makeRestCall("http://user:pass@jenkins.com");
        verify(rest).exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class));
    }

    @SuppressWarnings("rawtypes")
	@Test
    public void verifyAuthCredentialsBySettings() throws Exception {
        @SuppressWarnings("unchecked")
		HttpEntity headers = new HttpEntity(defaultSonarClient.createHeaders("does:matter"));
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        settings.setApiKey("matter");
        settings.setUsername("does");
        defaultSonarClient.makeRestCall("http://jenkins.com");
        verify(rest).exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
    public void verifyGetLogUrl() throws Exception {
        HttpEntity headers = new HttpEntity(defaultSonarClient.createHeaders("does:matter"));
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET),
                eq(headers), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        settings.setApiKey("matter");
        settings.setUsername("does");
        defaultSonarClient.getLog("http://jenkins.com");
        verify(rest).exchange(eq(URI.create("http://jenkins.com/consoleText")), eq(HttpMethod.GET),
                eq(headers), eq(String.class));
    }

    @Test
    public void instanceJobs_emptyResponse_returnsEmptyMap() {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<String>("", HttpStatus.OK));

        Map<SonarProject, Set<CodeQuality>> jobs = SonarClient.getInstanceJobs(URL_TEST);

        assertThat(jobs.size(), is(0));
    }

    @Test
    public void instanceJobs_twoJobsTwoCodeQualitys() throws Exception {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<String>(getJson("instanceJobs_twoJobsTwoCodeQualitys.json"), HttpStatus.OK));

        Map<SonarProject, Set<CodeQuality>> jobs = SonarClient.getInstanceJobs(URL_TEST);

        assertThat(jobs.size(), is(2));
        Iterator<SonarProject> jobIt = jobs.keySet().iterator();

        //First job
        SonarProject job = jobIt.next();
        assertJob(job, "job1", "http://server/job/job1/");

        Iterator<CodeQuality> CodeQualityIt = jobs.get(job).iterator();
        assertCodeQuality(CodeQualityIt.next(),"2", "http://server/job/job1/2/");
        assertCodeQuality(CodeQualityIt.next(),"1", "http://server/job/job1/1/");
        assertThat(CodeQualityIt.hasNext(), is(false));

        //Second job
        job = jobIt.next();
        assertJob(job, "job2", "http://server/job/job2/");

        CodeQualityIt = jobs.get(job).iterator();
        assertCodeQuality(CodeQualityIt.next(),"2", "http://server/job/job2/2/");
        assertCodeQuality(CodeQualityIt.next(),"1", "http://server/job/job2/1/");
        assertThat(CodeQualityIt.hasNext(), is(false));

        assertThat(jobIt.hasNext(), is(false));
    }

    @Test
    public void CodeQualityDetails_full() throws Exception {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(getJson("CodeQualityDetails_full.json"), HttpStatus.OK));
        
        CodeQuality CodeQuality = SonarClient.getCodeQualityDetails("http://jenkins.net/job/2483", "http://jenkins.net/job/");

        assertThat(CodeQuality.getTimestamp(), notNullValue());
        assertThat(CodeQuality.getNumber(), is("2483"));
        assertThat(CodeQuality.getCodeQualityUrl(), is("http://jenkins.net/job/2483"));
        assertThat(CodeQuality.getArtifactVersionNumber(), nullValue());
        assertThat(CodeQuality.getStartTime(), is(1421281415000L));
        assertThat(CodeQuality.getEndTime(), is(1421284113495L));
        assertThat(CodeQuality.getDuration(), is(2698495L));
        assertThat(CodeQuality.getCodeQualityStatus(), is(CodeQualityStatus.Failure));
        assertThat(CodeQuality.getStartedBy(), is("ab"));
        assertThat(CodeQuality.getSourceChangeSet().size(), is(2));
        assertThat(CodeQuality.getStartTime(), notNullValue());

        // ChangeSet 1
        SCM scm = CodeQuality.getSourceChangeSet().get(0);
        assertThat(scm.getScmUrl(), is("http://svn.apache.org/repos/asf/lucene/dev/branches/branch_5x"));
        assertThat(scm.getScmRevisionNumber(), is("1651902"));
        assertThat(scm.getScmCommitLog(), is("Merged revision(s) 1651901 from lucene/dev/trunk:\nLUCENE-6177: fix typo"));
        assertThat(scm.getScmAuthor(), is("uschindler"));
        assertThat(scm.getScmCommitTimestamp(), notNullValue());
        assertThat(scm.getNumberOfChanges(), is(4L));

        // ChangeSet 2
        scm = CodeQuality.getSourceChangeSet().get(1);
        assertThat(scm.getScmUrl(), nullValue());
        assertThat(scm.getScmRevisionNumber(), is("1651896"));
        assertThat(scm.getScmCommitLog(), is("SOLR-6900: bin/post improvements including glob handling, spaces in file names, and improved help output (merged from trunk r1651895)"));
        assertThat(scm.getScmAuthor(), is("ehatcher"));
        assertThat(scm.getScmCommitTimestamp(), notNullValue());
        assertThat(scm.getNumberOfChanges(), is(5L));
    }

    @Test
    public void CodeQualityingCodeQuality() throws Exception {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(getJson("CodeQualityCodeQualityingDetails_full.json"), HttpStatus.OK));
  
        assertEquals(SonarClient.getCodeQualityDetails(URL_TEST2, "http://jenkins.net/job/Test/"), null);

    }
    
    private void assertCodeQuality(CodeQuality CodeQuality, String number, String url) {
        assertThat(CodeQuality.getNumber(), is(number));
        assertThat(CodeQuality.getCodeQualityUrl(), is(url));
    }

    private String getJson(String fileName) throws IOException {
        InputStream inputStream = DefaultSonarClientTests.class.getResourceAsStream(fileName);
        return IOUtils.toString(inputStream);
    }

    private void assertJob(SonarProject job, String name, String url) {
        assertThat(job.getJobName(), is(name));
        assertThat(job.getJobUrl(), is(url));
    }
    */
}
