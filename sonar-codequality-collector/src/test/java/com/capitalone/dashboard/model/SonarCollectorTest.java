package com.capitalone.dashboard.model;



import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class SonarCollectorTest {



	@Test
	public void test(){
		SonarCollector job = new SonarCollector();
		List<String> servers = new ArrayList<>();
		servers.add("http://sonar.net/job/Test");
		assertTrue(job.getSonarServers().isEmpty());
		job.setSonarServers(servers);
		
		assertTrue(!job.getSonarServers().isEmpty());
		assertEquals(job.getSonarServers().get(0) , "http://sonar.net/job/Test");
	}


}

