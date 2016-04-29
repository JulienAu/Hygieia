package com.capitalone.dashboard.collector;
import com.capitalone.dashboard.model.CodeQuality;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.SonarCollector;
import com.capitalone.dashboard.model.SonarProject;
import com.capitalone.dashboard.repository.CodeQualityRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.SonarCollectorRepository;
import com.capitalone.dashboard.repository.SonarProjectRepository;
import static org.junit.Assert.*;
import org.bson.types.ObjectId;

import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SonarCollectorTaskTests {

    @Mock private TaskScheduler taskScheduler;
    @Mock private SonarCollectorRepository sonarCollectorRepository;
    @Mock private SonarProjectRepository sonarProjectRepository;
    @Mock private CodeQualityRepository qualityRepository;
    @Mock private SonarClient sonarClient;
    @Mock private SonarSettings sonarSettings;
    @Mock private ComponentRepository dbComponentRepository;
    @Mock private SonarProject SonarProject;

    @InjectMocks private SonarCollectorTask task;

    private static final String SERVER1 = "http://sonar.net";

    @Test
    public void collect_noServers_nothingAdded() throws MalformedURLException, IOException {
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(new SonarCollector());
        verifyZeroInteractions(sonarClient, qualityRepository);
    }



    @Test
    public void collect_noprojectsOnServer_nothingAdded() throws MalformedURLException, IOException {
        when(sonarClient.getProjects(SERVER1)).thenReturn(new ArrayList<SonarProject>());
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collectorWithOneServer());

        verify(sonarClient).getProjects(SERVER1);
        verifyNoMoreInteractions(sonarClient, qualityRepository);
    }

    @Test
    public void collect_twoprojects_projectsAdded() throws MalformedURLException, IOException {
        when(sonarClient.getProjects(SERVER1)).thenReturn(twoprojects(SERVER1));
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collectorWithOneServer());

        verify(sonarProjectRepository, times(2)).save(any(SonarProject.class));
    }

    @Test
    public void collect_projectEnabled_newBuild_buildAdded() throws MalformedURLException, IOException {
        SonarCollector collector = collectorWithOneServer();
        SonarProject project = sonarProject("Test", SERVER1, "15");
        project.setId(ObjectId.get());
        CodeQuality quality = quality("383", "http://sonar.net/project/Test");
        assertNotNull(quality);
        when(sonarClient.getProjects(SERVER1)).thenReturn(oneprojects(project));
        when(sonarProjectRepository.findEnabledProjects(collector.getId(), SERVER1))
                .thenReturn(Arrays.asList(project));
        when(qualityRepository.findByCollectorItemIdAndTimestamp(project.getId(), quality.getTimestamp())).thenReturn(null);
        when(sonarClient.currentCodeQuality(project)).thenReturn(quality);
        when(dbComponentRepository.findAll()).thenReturn(components());
        task.collect(collector);
   
        verify(qualityRepository, times(1)).save(quality);
    }
    
    
    @Test
    public void delete_projects() throws MalformedURLException, IOException {
        SonarCollector collector = collectorWithOneServer();
        SonarProject project = sonarProject("Test","http://sonar.net/project", "189");
        Set<ObjectId> udId = new HashSet<>();
		udId.add(collector.getId());
        project.setId(ObjectId.get());
        CodeQuality quality = quality("383", "http://sonar.net/project/Test");
        List<SonarProject> deleteprojectList = new ArrayList<>();
        List<SonarProject> projectList = new ArrayList<>();
        projectList.add(project);
        deleteprojectList.add(project);
        when(sonarProjectRepository.findByCollectorIdIn(udId)).thenReturn(projectList);
        when(sonarClient.getProjects(SERVER1)).thenReturn(oneprojects(project));
        when(sonarProjectRepository.findEnabledProjects(collector.getId(), SERVER1))
        .thenReturn(Arrays.asList(project));
        when(qualityRepository.findByCollectorItemIdAndTimestamp(project.getId(), quality.getTimestamp())).thenReturn(null);
        when(sonarClient.currentCodeQuality(project)).thenReturn(quality);
        when(dbComponentRepository.findAll()).thenReturn(components());
        
        
        task.collect(collector);
        verify(sonarProjectRepository, times(1)).delete(deleteprojectList);
        
    }
    


    private CodeQuality quality(String number, String url) {
        CodeQuality quality = new CodeQuality();
        quality.setVersion(number);
        quality.setUrl(url);
        quality.setTimestamp(15465465);
        return quality;
    }
    

    private SonarCollector collectorWithOneServer() {
        return SonarCollector.prototype(Arrays.asList(SERVER1));
    }
    
    private List<SonarProject> oneprojects(SonarProject project) {
        List<SonarProject> projects = new ArrayList<>();
        projects.add(project);
        return projects;
    }
    
    private List<SonarProject> twoprojects(String server) {
        List<SonarProject> projects = new ArrayList<>();
        projects.add(sonarProject("project1", server, "1"));
        projects.add(sonarProject("project2", server, "2"));
        return projects;
    }
    
    private SonarProject sonarProject(String projectName, String instanceUrl, String id) {
        SonarProject project = new SonarProject();
        project.setProjectName(projectName);
        project.setInstanceUrl(instanceUrl);
        project.setProjectId(id);
        return project;
    }
    
    private ArrayList<com.capitalone.dashboard.model.Component> components() {
    	ArrayList<com.capitalone.dashboard.model.Component> cArray = new ArrayList<com.capitalone.dashboard.model.Component>();
    	com.capitalone.dashboard.model.Component c = new Component();
    	c.setId(new ObjectId());
    	c.setName("COMPONENT1");
    	c.setOwner("JOHN");
    	cArray.add(c);
    	return cArray;
    }
}
