package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.Hudson2Job;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
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
    private static final String URL_TEST2 = "http://jenkins.net/job/Test/383/";
    private static final String JOBS_URL_DETAILS = "/Functional_Coverage/index.html";

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

    @Test
    public void buildDetails_full() throws Exception {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(getJson("buildDetails_full.json"), HttpStatus.OK));

        Build2 build = hudsonClient.getBuildDetails(URL_TEST2, null);
        assertThat(build.getNumber(), is("383"));
        assertThat(build.getBuildUrl(), is(URL_TEST2));
        

    }
    
    @Test @Ignore
    public void codeCoverage2() throws Exception {
    	when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(getJson("buildDetails_full.json"), HttpStatus.OK));
    	String url = DefaultHudson2Client.joinURL("http://jenkins.net/job/Test/383/", JOBS_URL_DETAILS);
    	File input = new File("CodeCoverageUnitaire.html");
    	when(defaultHudsonClient.getDocumentHelper(url)).thenReturn(Jsoup.parse(input, "UTF-8", "http://example.com/")); 
    	Build2 build = hudsonClient.getBuildDetails("http://jenkins.net/job/Test/383/", "http://jenkins.net/job/Test/");
  
       // assertNotNull(build.getBranchCoverage());
        assertNotNull(build.getBranchCoverageUnitaire());
        //assertNotNull(build.getBytesLostValgrind());
        //assertNotNull(build.getDuplicateCodeHigh());
        //assertNotNull(build.getDuplicateCodeMedium());
        //assertNotNull(build.getDuplicateCodeLow());
        /*assertNotNull(build.getLocLanguage());
        assertNotNull(build.getLocFile());
        assertNotNull(build.getLoc());*/
        assertNotNull(build.getLineCoverageUnitaire());
       // assertNotNull(build.getLineCoverage());
        assertNotNull(build.getFunctionCoverageUnitaire());
       /* assertNotNull(build.getFunctionCoverage());
        assertNotNull(build.getDuplicateCodeWarnings());
        assertNotNull(build.getClassesCoverageCobertura());
        assertNotNull(build.getFileCoverageCobertura());
        assertNotNull(build.getPackageCoverageCobertura());
        assertNotNull(build.getLineCoverageCobertura());*/
        
        

    }
    
    @Test
    public void codeCoverage() throws Exception {
    	when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(getJson("buildDetails_full.json"), HttpStatus.OK));
   
    	Build2 build = hudsonClient.getBuildDetails("http://pic2.s1.p.fti.net/view/SDFY/view/PNS%20C++/job/WS_31_PACKAGE_KPI/lastSuccessfulBuild/", "http://pic2.s1.p.fti.net/view/SDFY/view/PNS%20C++/job/WS_31_PACKAGE_KPI/");
  
        assertNotNull(build.getBranchCoverage());
        assertNotNull(build.getBranchCoverageUnitaire());
        assertNotNull(build.getBytesLostValgrind());
        assertNotNull(build.getDuplicateCodeHigh());
        assertNotNull(build.getDuplicateCodeMedium());
        assertNotNull(build.getDuplicateCodeLow());
        assertNotNull(build.getLocLanguage());
        assertNotNull(build.getLocFile());
        assertNotNull(build.getLoc());
        assertNotNull(build.getLineCoverageUnitaire());
        assertNotNull(build.getLineCoverage());
        assertNotNull(build.getFunctionCoverageUnitaire());
        assertNotNull(build.getFunctionCoverage());
        assertNotNull(build.getDuplicateCodeWarnings());
        assertNotNull(build.getClassesCoverageCobertura());
        assertNotNull(build.getFileCoverageCobertura());
        assertNotNull(build.getPackageCoverageCobertura());
        assertNotNull(build.getLineCoverageCobertura());
        
        

    }
    

	@Test
    public void buildingBuild() throws Exception {
        when(rest.exchange(Matchers.any(URI.class), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(getJson("buildBuildingDetails_full.json"), HttpStatus.OK));
  
        assertEquals(hudsonClient.getBuildDetails(URL_TEST2, null), null);

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
