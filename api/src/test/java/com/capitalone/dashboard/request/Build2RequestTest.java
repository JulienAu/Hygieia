package com.capitalone.dashboard.request;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.capitalone.dashboard.model.Build;
import com.capitalone.dashboard.model.BuildStatus;






public class Build2RequestTest {

	@Test
	public void test_Equals(){
		Build2Request build = new Build2Request();
		List<BuildStatus> list = new ArrayList<>();
		build.setBuildStatuses(list);
		build.setComponentId(ObjectId.get());
		build.setDurationGreaterThan(Long.MIN_VALUE);
		build.setDurationLessThan(Long.MAX_VALUE);
		build.setEndDateBegins(Long.MIN_VALUE);
		build.setEndDateEnds(Long.MAX_VALUE);
		build.setNumberOfDays(5);
		build.setStartDateBegins(Long.MAX_VALUE);
		build.setStartDateEnds(Long.MIN_VALUE);
		
		assertTrue(build.getBuildStatuses().isEmpty());
		assertNotNull(build.getComponentId());
		assertEquals((long)build.getDurationGreaterThan() , Long.MIN_VALUE);
		assertNotNull(build.getDurationLessThan());
		assertEquals((long)build.getEndDateBegins() , Long.MIN_VALUE);
		assertNotNull(build.getEndDateEnds());
		assertEquals((int)build.getNumberOfDays() , 5);
		assertNotNull(build.getStartDateBegins());
		assertEquals((long)build.getStartDateEnds() , Long.MIN_VALUE);
	}

}