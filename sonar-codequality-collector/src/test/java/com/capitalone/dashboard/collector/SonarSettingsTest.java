package com.capitalone.dashboard.collector;

import java.util.List;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;


public class SonarSettingsTest {

	
    @Test
    public void Settings(){
		List<String> servers = new ArrayList<>();
		servers.add("http://jenkins.net");
		SonarSettings settings = new SonarSettings();
		settings.setCron("0=0 ***");
		settings.setServers(servers);
		settings.setMetrics("metrics");
       assertEquals(settings.getServers().size() , 1);
       assertTrue(!settings.getCron().isEmpty());
       assertEquals(settings.getServers().size() , 1);
    }


}

