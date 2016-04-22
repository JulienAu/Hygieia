package com.capitalone.dashboard.collector;

import java.util.List;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;


public class Hudson2jobSettingsTest {


	@Before
	public void setup(){
		List<String> servers = new ArrayList<>();
		servers.add("http://jenkins.net");
		Hudson2Settings settings = new Hudson2Settings();
		settings.setCron("0=0 ***");
		settings.setSaveLog(true);
		settings.setServers(servers);
		
	}
	
    @Test
    public void Settings(){
		List<String> servers = new ArrayList<>();
		servers.add("http://jenkins.net");
		Hudson2Settings settings = new Hudson2Settings();
		settings.setCron("0=0 ***");
		settings.setSaveLog(true);
		settings.setServers(servers);
       assertTrue(settings.isSaveLog());
       assertEquals(settings.getServers().size() , 1);
       assertTrue(!settings.getCron().isEmpty());
    }


}

