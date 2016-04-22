package com.capitalone.dashboard.model;



import static org.junit.Assert.*;

import org.junit.Test;


public class HudsonjobTest {



	@Test
	public void test_Equals(){
		HudsonJob job = new HudsonJob();
		HudsonJob job2 = hudsonJob("Test","http://jenkins2.net/job", "http://jenkins.net/job/Test");
		HudsonJob job3 = hudsonJob("Test","http://jenkins2.net/job", "http://jenkins.net/job/Test");
		HudsonJob job4 = hudsonJob("Test2","http://jenkins2.net", "http://jenkins.net/job/Test2");
		assertTrue(job2.equals(job3));
		assertTrue(!job2.equals(job4));
		assertTrue(job.equals(job));
		assertTrue(!job.equals(null));

	}


	private HudsonJob hudsonJob(String jobName, String instanceUrl, String jobUrl) {
		HudsonJob job = new HudsonJob();
		job.setJobName(jobName);
		job.setInstanceUrl(instanceUrl);
		job.setJobUrl(jobUrl);
		return job;
	}

}

