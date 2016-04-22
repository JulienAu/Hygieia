package com.capitalone.dashboard.collector;

import java.util.List;

import static org.junit.Assert.*;
import java.util.ArrayList;
import org.junit.Test;


public class HudsonjobSettingsTest {


    @Test
    public void Settings(){
		List<String> servers = new ArrayList<>();
		servers.add("http://jenkins.net");
		HudsonSettings settings = new HudsonSettings();
		settings.setCron("0=0 ***");
		settings.setSaveLog(true);
		settings.setServers(servers);
       assertTrue(settings.isSaveLog());
       assertEquals(settings.getServers().size() , 1);
       assertTrue(!settings.getCron().isEmpty());
    }


}

