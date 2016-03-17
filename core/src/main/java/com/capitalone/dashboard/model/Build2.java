package com.capitalone.dashboard.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * The result of a Continuous Integration build execution. Typically produces binary artifacts.
 * Often triggered by one or more SCM commits.
 *
 * Possible collectors:
 *  Hudson (in scope)
 *  Team City
 *  TFS
 *  Go
 *  Bamboo
 *  TravisCI
 *
 */
@Document(collection="builds2")
public class Build2 extends BaseModel {
    private ObjectId collectorItemId;
    private long timestamp;
    private String number;
	private String lineCoverage;
    private String functionCoverage;
    private String branchCoverage;
    private String buildUrl;
   
  

    public ObjectId getCollectorItemId() {
        return collectorItemId;
    }

    public void setCollectorItemId(ObjectId collectorItemId) {
        this.collectorItemId = collectorItemId;
    }



	public String getLineCoverage() {
		return lineCoverage;
	}

	public String getFunctionCoverage() {
		return functionCoverage;
	}

	public String getBranchCoverage() {
		return branchCoverage;
	}

	public void setLineCoverage(String lineCoverage) {
		this.lineCoverage = lineCoverage;
	}



	public void setFunctionCoverage(String functionCoverage) {
		this.functionCoverage = functionCoverage;
	}



	public void setBranchCoverage(String branchCoverage) {
		this.branchCoverage = branchCoverage;
	}


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public void setBuildUrl(String buildUrl) {
        this.buildUrl = buildUrl;
    }

  
    @Override
    public String toString(){
    	String res ="";
    	res += this.buildUrl+ "///"+ this.number + "///";
    	return res;
    }
}
