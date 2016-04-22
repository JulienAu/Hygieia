package com.capitalone.dashboard.model;



import static org.junit.Assert.*;

import org.junit.Test;


public class Hudson2jobTest {



	@Test
	public void test_Equals(){
		Hudson2Job job = new Hudson2Job();
		Hudson2Job job2 = hudsonJob("Test","http://jenkins2.net/job", "http://jenkins.net/job/Test");
		Hudson2Job job3 = hudsonJob("Test","http://jenkins2.net/job", "http://jenkins.net/job/Test");
		Hudson2Job job4 = hudsonJob("Test2","http://jenkins2.net", "http://jenkins.net/job/Test2");
		assertTrue(job2.equals(job3));
		assertTrue(!job2.equals(job4));
		assertTrue(job.equals(job));
		assertTrue(!job.equals(null));

	}


	private Hudson2Job hudsonJob(String jobName, String instanceUrl, String jobUrl) {
		Hudson2Job job = new Hudson2Job();
		job.setJobName(jobName);
		job.setInstanceUrl(instanceUrl);
		job.setJobUrl(jobUrl);
		return job;
	}

}

