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
	private static final String JOBS_URL_DETAILS_UNITAIRE = "/UT_Coverage/index.html";
	private static final String BUILD_DETAILS_URL_SUFFIX = "/api/json?tree=building";



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
						if (!buildNumber.equals("0") ) {
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
		String[] urlWithoutBuild = buildUrl.split("/");
		String urlWB ="http:";
		for (int i = 1 ; i<urlWithoutBuild.length-1 ; i++){
			urlWB = urlWB+"/"+urlWithoutBuild[i];
		}
		String url = joinURL(urlWB, JOBS_URL_DETAILS);
		String url2 = joinURL(urlWB, JOBS_URL_DETAILS_UNITAIRE);
		if(!building(buildUrl)){
			List<String> cobertura = coberturaCoverageReport(buildUrl);
			List<String> codeCoverage = codeCoverage(url);
			List<String> codeCoverageUnitaire = codeCoverage(url2);
			List<String> valgrind = valgrindReport(buildUrl);
			List<String> dryResult = dryResultAll(joinURL(buildUrl , "/dryResult/"));
			List<String> loc = lOC(buildUrl);
			Build2 build = new Build2();
			if(!codeCoverage.isEmpty()){
				build.setLineCoverage(codeCoverage.get(0));
				build.setFunctionCoverage(codeCoverage.get(1));
				build.setBranchCoverage(codeCoverage.get(2));
			}
			if(!codeCoverageUnitaire.isEmpty()){
				build.setLineCoverageUnitaire(codeCoverageUnitaire.get(0));
				build.setFunctionCoverageUnitaire(codeCoverageUnitaire.get(1));
				build.setBranchCoverageUnitaire(codeCoverageUnitaire.get(2));
			}
			if(!valgrind.isEmpty()){
				build.setBytesLostValgrind(valgrind.get(0));
			}
			if(!dryResult.isEmpty()){
				build.setDuplicateCodeWarnings(dryResult.get(0));
				build.setDuplicateCodeHigh(dryResult.get(1));
				build.setDuplicateCodeMedium(dryResult.get(2));
				build.setDuplicateCodeLow(dryResult.get(3));
			}
			if(!cobertura.isEmpty()){
				build.setPackageCoverageCobertura(cobertura.get(0));
				build.setFileCoverageCobertura(cobertura.get(1));
				build.setClassesCoverageCobertura(cobertura.get(2));
				build.setLineCoverageCobertura(cobertura.get(3));
				build.setConditionalsCoverageCobertura(cobertura.get(4));

			}
			locBuild(loc , build);
			build.setNumber(urlWithoutBuild[urlWithoutBuild.length-1]);
			build.setBuildUrl(buildUrl);
			build.setTimestamp(System.currentTimeMillis());
			return build;
		}
		return null;
	}


	private void locBuild(List<String> loc , Build2 build){
		if(!loc.isEmpty()){
			build.getLoc().add(loc.get(0));
			build.getLocFile().add(loc.get(1));
			build.getLocLanguage().add(loc.get(2));
			for (int i = 3 ; i < loc.size() ; i= i + 3){
				build.getLocLanguage().add(loc.get(i));
				build.getLoc().add(loc.get(i+1));
				build.getLocFile().add(loc.get(i+2));
			}
		}
	}



	private String getString(JSONObject json, String key) {
		return (String) json.get(key);
	}



	private JSONArray getJsonArray(JSONObject json, String key) {
		Object array = json.get(key);
		return array == null ? new JSONArray() : (JSONArray) array;
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

	protected List<String> codeCoverage(String url) throws IOException{
		ArrayList<String> res = new ArrayList<String>();
		try{
			Document doc = Jsoup.connect(url).get();
			Element elementsByTag = doc.getElementsByTag("body").get(0);
			Elements rows = elementsByTag.getElementsByTag("td");
			for(Element row : rows) {
				String test = row.text();
				if (test.matches("[0-9]+.[0-9]+ %")){
					res.add(test);
				}
			}
		} catch (IOException e) {
			LOG.info("No codeCoverage");
		}
		return res;
	}

	private static List<String> coberturaCoverageReport(String url) throws IOException{
		List<String> res = new ArrayList<String>();	
		try{
			Document doc = Jsoup.connect(url).get();	
			Elements elementsByTag = doc.getElementsByTag("body");
			for(Element row1 : elementsByTag) {
				Element row = row1.getElementsByTag("table").get(0);
				String test = row.text();
				String[] resSplit = test.split(" ");
				for(String res2 : resSplit){
					if (res2.matches("[0-9]*+%")){
						res.add(res2);
					}
				}
			}
		} catch (IOException e) {
			LOG.info("No Cobertura KPI");
		}
		return res;
	}

	private static List<String> valgrindReport(String url) throws IOException{
		List<String> res = new ArrayList<String>();	
		try{
			Document doc = Jsoup.connect(url).get();	
			Elements elementsByTag = doc.getElementsByTag("body");
			for(Element row1 : elementsByTag) {

				Element row = row1.getElementsByTag("table").get(1);

				String test = row.text();
				String[] resSplit = test.split(" ");
				for(String res2 : resSplit){
					if (res2.matches("[0-9]+")){
						res.add(res2);
					}
				}

			}		
		} catch (IOException e) {
			LOG.info("No Valgrind KPI");
		}
		return res;
	}


	private static List<String> dryResultAll(String url) throws IOException{
		List<String> res = new ArrayList<String>();	
		try{
			Document doc = Jsoup.connect(url).get();	
			Elements elementsByTag = doc.getElementsByTag("body");
			for(Element row1 : elementsByTag) {
				Element row = row1.getElementsByTag("table").get(1);
				String test = row.text();
				String[] resSplit = test.split(" ");
				for(String res2 : resSplit){
					if (res2.matches("[0-9]+")){
						res.add(res2);
					}
				}
			}
		} catch (IOException e) {
			LOG.info("No Duplicate Code KPI");
		}
		return res;
	}


	private boolean building(String buildUrl) {
		String url;
		try {
			url = joinURL(buildUrl, BUILD_DETAILS_URL_SUFFIX);
			ResponseEntity<String> result = makeRestCall(url);

			String returnJSON = result.getBody();
			JSONParser parser = new JSONParser();
			JSONObject buildJson = (JSONObject) parser.parse(returnJSON);

			return (Boolean) buildJson.get("building");
			
		} catch (MalformedURLException e) {
			LOG.info("Error json building build2");
		} catch (ParseException e) {
			LOG.info("Error parse json building build2");
		}
		return true;
	}


	private static List<String> lOC(String url) throws IOException{
		List<String> res = new ArrayList<String>();	
		try{
			Document doc = Jsoup.connect(url).get();	
			Elements elementsByTag = doc.getElementsByTag("body");
			for(Element row1 : elementsByTag) {
				Elements row = row1.getElementsByTag("table").get(0).getElementsByTag("td");
				for(Element row2 : row) {
					String test = row2.text();
					if (test.matches("[0-9]++.+")){
						String[] resSplit = test.split("\\.");
						for(String res2 : resSplit){

							String[] resSplit2 = res2.split(" ");
							String language="";
							for(String res3 : resSplit2){

								if (res3.matches("[1-9]++.*")){
									res3=res3.replaceAll("[^0-9]","");
									res.add(res3);
								}
								if(res3.matches("\\:")){
									res.add(language);
								}
								language = res3;
							}
						}

					}
				}
			}		
		} catch (IOException e) {
			LOG.info("No LOC KPI");
		}
		return res;
	}


}


