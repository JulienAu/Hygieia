package com.capitalone.dashboard.rest;

import com.capitalone.dashboard.config.TestConfig;
import com.capitalone.dashboard.config.WebMVCConfig;
import com.capitalone.dashboard.model.Build2;
import com.capitalone.dashboard.model.DataResponse;
import com.capitalone.dashboard.request.Build2Request;
import com.capitalone.dashboard.service.Build2Service;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.Arrays;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class , WebMVCConfig.class})
@WebAppConfiguration
public class Build2ControllerTest {
    private MockMvc mockMvc;

    @Autowired private WebApplicationContext wac;
    @Autowired private Build2Service buildService;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void builds() throws Exception {
        Build2 build = makeBuild();
        Iterable<Build2> builds = Arrays.asList(build);
        DataResponse<Iterable<Build2>> response = new DataResponse<>(builds, 1);
  

        when(buildService.search(Mockito.any(Build2Request.class))).thenReturn(response);

        mockMvc.perform(get("/build2?componentId=" + ObjectId.get()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$result", hasSize(1)))
                .andExpect(jsonPath("$result[0].id", is(build.getId().toString())))
                .andExpect(jsonPath("$result[0].collectorItemId", is(build.getCollectorItemId().toString())))
                .andExpect(jsonPath("$result[0].timestamp", is(intVal(build.getTimestamp()))))
                .andExpect(jsonPath("$result[0].number", is(build.getNumber())))
                .andExpect(jsonPath("$result[0].lineCoverage", is(build.getLineCoverage())))
                .andExpect(jsonPath("$result[0].lineCoverageUnitaire", is(build.getLineCoverageUnitaire())))
                .andExpect(jsonPath("$result[0].branchCoverage", is(build.getBranchCoverage())))
                .andExpect(jsonPath("$result[0].branchCoverageUnitaire", is(build.getBranchCoverageUnitaire())))
                .andExpect(jsonPath("$result[0].buildUrl", is(build.getBuildUrl())))
                .andExpect(jsonPath("$result[0].functionCoverage", is(build.getFunctionCoverage())))
                .andExpect(jsonPath("$result[0].functionCoverageUnitaire", is(build.getFunctionCoverageUnitaire())));
    }

    @Test
    public void  builds_noComponentId_badRequest() throws Exception {
        mockMvc.perform(get("/build2")).andExpect(status().isBadRequest());
    }

    private Build2 makeBuild() {
        Build2 build = new Build2();
        build.setId(ObjectId.get());
        build.setCollectorItemId(ObjectId.get());
        build.setTimestamp(1);
        build.setNumber("1");
        build.setBuildUrl("buildUrl");
        build.setLineCoverage("80.8%");
        build.setLineCoverageUnitaire("75.9%");
        build.setBranchCoverage("55.3%");
        build.setBranchCoverageUnitaire("62.3%");
        build.setFunctionCoverage("59.4%");
        build.setFunctionCoverageUnitaire("65.8%");

        return build;
    }



    private int intVal(long value) {
        return Long.valueOf(value).intValue();
    }

}
