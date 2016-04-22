package com.capitalone.dashboard.model;



import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class HudsonCollectorTest {



	@Test
	public void test(){
		HudsonCollector job = new HudsonCollector();
		List<String> servers = new ArrayList<>();
		servers.add("http://jenkins.net/job/Test");
		assertTrue(job.getBuildServers().isEmpty());
		job.setBuildServers(servers);
		
		assertTrue(!job.getBuildServers().isEmpty());
		assertEquals(job.getBuildServers().get(0) , "http://jenkins.net/job/Test");
	}


}

