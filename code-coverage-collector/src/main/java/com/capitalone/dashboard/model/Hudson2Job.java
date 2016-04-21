package com.capitalone.dashboard.model;

/**
 * CollectorItem extension to store the instance, build job and build url.
 */
public class Hudson2Job extends CollectorItem {
    private static final String INSTANCE_URL = "instanceUrl";
    private static final String JOB_NAME = "jobName";
    private static final String JOB_URL = "jobUrl";
    //private static final String UT_COVERAGE = "utCoverage";
    //private static final String FT_COVERAGE = "ftCoverage" ;

    public String getInstanceUrl() {
        return (String) getOptions().get(INSTANCE_URL);
    }

    public void setInstanceUrl(String instanceUrl) {
        getOptions().put(INSTANCE_URL, instanceUrl);
    }

    public String getJobName() {
        return (String) getOptions().get(JOB_NAME);
    }

    public void setJobName(String jobName) {
        getOptions().put(JOB_NAME, jobName);
    }

    public String getJobUrl() {
        return (String) getOptions().get(JOB_URL);
    }

    public void setJobUrl(String jobUrl) {
        getOptions().put(JOB_URL, jobUrl);
    }
    /*
    public String getUtCoverage() {
        return (String) getOptions().get(UT_COVERAGE);
    }

    public void setUtCoverage(String utCoverage) {
        getOptions().put(UT_COVERAGE, utCoverage);
    }
 
    public String getftCoverage() {
        return (String) getOptions().get(FT_COVERAGE);
    }

    public void setFtCoverage(String ftCoverage) {
        getOptions().put(FT_COVERAGE, ftCoverage);
    }
 */
    
	@Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }

        Hudson2Job hudsonJob = (Hudson2Job) o;

        return getInstanceUrl().equals(hudsonJob.getInstanceUrl()) && getJobName().equals(hudsonJob.getJobName());
    }

    @Override
    public int hashCode() {
        int result = getInstanceUrl().hashCode();
        result = 31 * result + getJobName().hashCode();
        return result;
    }
}
