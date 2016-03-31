package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.List;

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
	private String lineCoverageUnitaire;
    private String functionCoverageUnitaire;
    private String branchCoverageUnitaire;
    private String bytesLostValgrind;
	private String packageCoverageCobertura;
	private String lineCoverageCobertura;
    private String fileCoverageCobertura;
    private String classesCoverageCobertura;
    private String conditionalsCoverageCobertura;
    private String duplicateCodeWarnings;
    private String duplicateCodeLow;
    private String duplicateCodeMedium;
    private String duplicateCodeHigh;
    private List<String> locLanguage = new 	ArrayList<String>();
    private List<String> loc = new 	ArrayList<String>();;
    private List<String> locFile= new ArrayList<String>();;
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

	
    public String getLineCoverageUnitaire() {
		return lineCoverageUnitaire;
	}

	public void setLineCoverageUnitaire(String lineCoverageUnitaire) {
		this.lineCoverageUnitaire = lineCoverageUnitaire;
	}

	public String getFunctionCoverageUnitaire() {
		return functionCoverageUnitaire;
	}

	public void setFunctionCoverageUnitaire(String functionCoverageUnitaire) {
		this.functionCoverageUnitaire = functionCoverageUnitaire;
	}

	public String getBranchCoverageUnitaire() {
		return branchCoverageUnitaire;
	}

	public void setBranchCoverageUnitaire(String branchCoverageUnitaire) {
		this.branchCoverageUnitaire = branchCoverageUnitaire;
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

    public String getBytesLostValgrind() {
		return bytesLostValgrind;
	}

	public void setBytesLostValgrind(String bytesLostValgrind) {
		this.bytesLostValgrind = bytesLostValgrind;
	}

	public String getPackageCoverageCobertura() {
		return packageCoverageCobertura;
	}

	public void setPackageCoverageCobertura(String packageCoverageCobertura) {
		this.packageCoverageCobertura = packageCoverageCobertura;
	}

	public String getLineCoverageCobertura() {
		return lineCoverageCobertura;
	}

	public void setLineCoverageCobertura(String lineCoverageCobertura) {
		this.lineCoverageCobertura = lineCoverageCobertura;
	}

	public String getFileCoverageCobertura() {
		return fileCoverageCobertura;
	}

	public void setFileCoverageCobertura(String functionCoverageCobertura) {
		this.fileCoverageCobertura = functionCoverageCobertura;
	}

	public String getConditionalsCoverageCobertura() {
		return conditionalsCoverageCobertura;
	}

	public void setConditionalsCoverageCobertura(String branchCoverageCobertura) {
		this.conditionalsCoverageCobertura = branchCoverageCobertura;
	}

	public String getClassesCoverageCobertura() {
		return classesCoverageCobertura;
	}

	public void setClassesCoverageCobertura(String classesCoverageCobertura) {
		this.classesCoverageCobertura = classesCoverageCobertura;
	}

	public String getDuplicateCodeWarnings() {
		return duplicateCodeWarnings;
	}

	public void setDuplicateCodeWarnings(String duplicateCodeWarnings) {
		this.duplicateCodeWarnings = duplicateCodeWarnings;
	}

	public String getDuplicateCodeLow() {
		return duplicateCodeLow;
	}

	public void setDuplicateCodeLow(String duplicateCodeLow) {
		this.duplicateCodeLow = duplicateCodeLow;
	}

	public String getDuplicateCodeMedium() {
		return duplicateCodeMedium;
	}

	public void setDuplicateCodeMedium(String duplicateCodeMedium) {
		this.duplicateCodeMedium = duplicateCodeMedium;
	}

	public String getDuplicateCodeHigh() {
		return duplicateCodeHigh;
	}

	public void setDuplicateCodeHigh(String duplicateCodeHigh) {
		this.duplicateCodeHigh = duplicateCodeHigh;
	}
	
	public List<String> getLocLanguage() {
		return locLanguage;
	}

	public void setLocLanguage(List<String> locLanguage) {
		this.locLanguage = locLanguage;
	}

	public List<String> getLoc() {
		return loc;
	}

	public void setLoc(List<String> loc) {
		this.loc = loc;
	}

	public List<String> getLocFile() {
		return locFile;
	}

	public void setLocFile(List<String> locFile) {
		this.locFile = locFile;
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
