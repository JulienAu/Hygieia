package com.capitalone.dashboard.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.capitalone.dashboard.repository.CollectorRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of widgets, collectors and application components that represent a software
 * project under development and/or in production use.
 *
 */
@Document(collection="dashboards")
public class Dashboard extends BaseModel {
	private String template;
	private String title;
	private Application application;
	private List<Widget> widgets = new ArrayList<>();
	private String owner;

	Dashboard() {
	}

	public Dashboard(String template, String title, Application application,String owner) {
		this.template = template;
		this.title = title;
		this.application = application;
		this.owner = owner;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public List<Widget> getWidgets() {
		return widgets;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	////////////////////////////////////////Config code coverage////////////////////////////////////
	public void updateDashboard(CollectorRepository collectorRepository){
		List<Widget> widgets = this.getWidgets();
		for (Widget widget2 : widgets){
			if(widget2.getName().equals("codeCoverage") && !this.getApplication().getComponents().get(0).getCollectorItems(CollectorType.Build2).isEmpty()){
				CollectorItem collectorItem = this.getApplication().getComponents().get(0).getCollectorItems(CollectorType.Build2).get(0);
				Collector collector = collectorRepository.findOne(collectorItem.getCollectorId());
				collectorItem.getOptions().putAll(widget2.getOptions());
				collectorItem.setCollector(collector);
			}
		}
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
