package com.capitalone.dashboard.model;



import static org.junit.Assert.*;

import org.junit.Test;


public class SonarProjectTest {



	@Test
	public void test_Equals(){
		SonarProject project = new SonarProject();
		SonarProject project2 = SonarProject("Test","http://sonar2.net/project", "100");
		SonarProject project3 = SonarProject("Test","http://sonar2.net/project", "100");
		SonarProject project4 = SonarProject("Test2","http://sonar2.net", "125");
		assertTrue(project2.equals(project3));
		assertTrue(!project2.equals(project4));
		assertTrue(project.equals(project));
		assertTrue(!project.equals(null));

	}


	private SonarProject SonarProject(String projectName, String instanceUrl, String id) {
		SonarProject project = new SonarProject();
		project.setProjectName(projectName);
		project.setInstanceUrl(instanceUrl);
		project.setProjectId(id);;
		return project;
	}

}

