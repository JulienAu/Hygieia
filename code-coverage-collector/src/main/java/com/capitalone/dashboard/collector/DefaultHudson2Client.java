package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.Hudson2Job;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.util.List;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * HudsonClient implementation that uses RestTemplate and JSONSimple to
 * fetch information from Hudson instances.
 */
@Component
public class DefaultHudson2Client implements Hudson2Client {
	private static final Logger LOG = LoggerFactory.getLogger(DefaultHudson2Client.class);

	private final RestOperations rest;
	private final Hudson2Settings settings;

	private static final String JOBS_URL_SUFFIX = "/api/json?tree=jobs[name,url,builds[number,url]]";
	private static final String JOBS_URL_DETAILS = "/Functional_Coverage/index.html";

	
	@Autowired
	public DefaultHudson2Client(Supplier<RestOperations> restOperationsSupplier, Hudson2Settings settings) {
		this.rest = restOperationsSupplier.get();
		this.settings = settings;
	}

	@Override
	public Map<Hudson2Job, Set<Build2>> getInstanceJobs(String instanceUrl) {
		Map<Hudson2Job, Set<Build2>> result = new LinkedHashMap<>();
		try {
			String url = joinURL(instanceUrl, JOBS_URL_SUFFIX);  
			ResponseEntity<String> responseEntity = makeRestCall(url);
			String returnJSON = responseEntity.getBody();
			JSONParser parser = new JSONParser();
			try {
				JSONObject object = (JSONObject) parser.parse(returnJSON);
				for (Object job : getJsonArray(object, "jobs")) {
					JSONObject jsonJob = (JSONObject) job;

					Hudson2Job hudsonJob = new Hudson2Job();
					hudsonJob.setInstanceUrl(instanceUrl);
					hudsonJob.setJobName(getString(jsonJob, "name"));
					hudsonJob.setJobUrl(getString(jsonJob, "url"));

					Set<Build2> builds = new LinkedHashSet<>();
					for (Object build : getJsonArray(jsonJob, "builds")) {
						JSONObject jsonBuild = (JSONObject) build;

						// A basic Build object. This will be fleshed out later if this is a new Build.
						String buildNumber = jsonBuild.get("number").toString();
						if (!buildNumber.equals("0")) {
							Build2 hudsonBuild = new Build2();
							hudsonBuild.setNumber(buildNumber);
							hudsonBuild.setBuildUrl(getString(jsonBuild, "url"));
							builds.add(hudsonBuild);
						}
					}
					// add the builds to the job
					result.put(hudsonJob, builds);
				}
			} catch (ParseException e) {
				LOG.error("Parsing jobs on instance: " + instanceUrl, e);
			}
		} catch (RestClientException rce) {
			LOG.error("client exception loading jobs", rce);
		} catch (MalformedURLException mfe) {
			LOG.error("malformed url for loading jobs", mfe);
		}

		return result;
	}

	@Override
	public Build2 getBuildDetails(String buildUrl, String jobUrl) throws IOException {
		String url = joinURL(buildUrl, JOBS_URL_DETAILS);
		List<String> codeCoverage = codeCoverage(url);
		Build2 build = new Build2();
		build.setLineCoverage(codeCoverage.get(0));
		build.setFunctionCoverage(codeCoverage.get(1));
		build.setBranchCoverage(codeCoverage.get(2));
		return build;
		
	}

	


	private String getString(JSONObject json, String key) {
		return (String) json.get(key);
	}

	

	private JSONArray getJsonArray(JSONObject json, String key) {
		Object array = json.get(key);
		return array == null ? new JSONArray() : (JSONArray) array;
	}





	

	/* Function name need to be changed */
	protected boolean cppCheck(String sUrl){
		try{
			makeRestCall(sUrl);
			return true;
		} catch (HttpClientErrorException rce) {
			LOG.info("Metrics Not found");
			return false;
		} catch (MalformedURLException mfe) {
			return false;
		}


	}
	protected ResponseEntity<String> makeRestCall(String sUrl) throws MalformedURLException{
		URI thisuri = URI.create(sUrl);
		String userInfo = thisuri.getUserInfo();

		//get userinfo from URI or settings (in spring properties)
		if (StringUtils.isEmpty(userInfo) && (this.settings.getUsername() != null) && (this.settings.getApiKey() != null)) {
			userInfo = this.settings.getUsername() + ":" + this.settings.getApiKey();
		}
		// Basic Auth only.
		if (StringUtils.isNotEmpty(userInfo)) {
			return rest.exchange(thisuri, HttpMethod.GET,
					new HttpEntity<>(createHeaders(userInfo)),
					String.class);
		} else {
			return rest.exchange(thisuri, HttpMethod.GET, null,
					String.class);
		}

	}

	protected HttpHeaders createHeaders(final String userInfo) {
		byte[] encodedAuth = Base64.encodeBase64(
				userInfo.getBytes(StandardCharsets.US_ASCII));
		String authHeader = "Basic " + new String(encodedAuth);

		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, authHeader);
		return headers;
	}

	protected String getLog(String buildUrl) {
		try {
			return makeRestCall(joinURL(buildUrl, "consoleText")).getBody();
		} catch (MalformedURLException mfe) {
			LOG.error("malformed url for build log", mfe);
		}

		return "";
	}

	// join a base url to another path or paths - this will handle trailing or non-trailing /'s
	public static String joinURL(String base, String ... paths) throws MalformedURLException {
		StringBuilder result = new StringBuilder(base);
		for (String path : paths) {
			String p = path.replaceFirst("^(\\/)+", "");
			if (result.lastIndexOf("/") != result.length() - 1) {
				result.append('/');
			}
			result.append(p);
		}
		return result.toString();
	}
	
	private List<String> codeCoverage(String url) throws IOException{
		ArrayList<String> res = new ArrayList<String>();
		//Document doc = Jsoup.parse(input , "UTF-8");
		Document doc = Jsoup.connect(url).get();
		Element elementsByTag = doc.getElementsByTag("body").get(0);
		Elements rows = elementsByTag.getElementsByTag("td");
		for(Element row : rows) {
			String test = row.text();
			if (test.matches("[0-9]+.[0-9]+ %")){
				res.add(test);
			}
		}
		return res;
	}
}
