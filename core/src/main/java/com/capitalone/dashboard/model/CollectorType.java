package com.capitalone.dashboard.model;

/**
 * Enumerates the possible {@link Collector} types.
 */
public enum CollectorType {
    SCM,
    Build,
    Artifact,
    Deployment,
    Feature,
    ScopeOwner,
    Scope,
    CodeQuality,
    Test,
    StaticSecurityScan,
    ChatOps,
    Cloud,
    Build2;

    public static CollectorType fromString(String value) {
        for (CollectorType collectorType : values()) {
            if (collectorType.toString().equalsIgnoreCase(value)) {
                return collectorType;
            }
        }
        throw new IllegalArgumentException(value + " is not a CollectorType");
    }
}
