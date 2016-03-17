package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of Collector that stores current build server configuration.
 */
public class Hudson2Collector extends Collector {
    private List<String> buildServers = new ArrayList<>();

    public List<String> getBuildServers() {
        return buildServers;
    }

    public void setBuildServers(List<String> buildServers) {
        this.buildServers = buildServers;
    }

    public static Hudson2Collector prototype(List<String> buildServers) {
        Hudson2Collector protoType = new Hudson2Collector();
        protoType.setName("HudsonCodeCoverage");
        protoType.setCollectorType(CollectorType.Build2);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.getBuildServers().addAll(buildServers);
        return protoType;
    }
}
