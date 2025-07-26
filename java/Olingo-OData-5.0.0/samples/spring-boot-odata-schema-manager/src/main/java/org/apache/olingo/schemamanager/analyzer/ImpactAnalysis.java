package org.apache.olingo.schemamanager.analyzer;

import java.util.List;
import java.util.Set;

/**
 * Impact analysis result when a schema element changes
 */
public class ImpactAnalysis {
    
    private final String changedElement;
    private final Set<String> directlyAffectedElements;
    private final Set<String> transitivelyAffectedElements;
    private final List<List<String>> impactPaths;
    private final ImpactLevel impactLevel;
    private final String summary;
    
    public enum ImpactLevel {
        LOW,    // Only affects a few elements
        MEDIUM, // Affects moderate number of elements
        HIGH,   // Affects many elements
        CRITICAL // Affects core elements or causes breaking changes
    }
    
    public ImpactAnalysis(String changedElement, 
                         Set<String> directlyAffectedElements,
                         Set<String> transitivelyAffectedElements,
                         List<List<String>> impactPaths,
                         ImpactLevel impactLevel,
                         String summary) {
        this.changedElement = changedElement;
        this.directlyAffectedElements = directlyAffectedElements;
        this.transitivelyAffectedElements = transitivelyAffectedElements;
        this.impactPaths = impactPaths;
        this.impactLevel = impactLevel;
        this.summary = summary;
    }
    
    public String getChangedElement() {
        return changedElement;
    }
    
    public Set<String> getDirectlyAffectedElements() {
        return directlyAffectedElements;
    }
    
    public Set<String> getTransitivelyAffectedElements() {
        return transitivelyAffectedElements;
    }
    
    public List<List<String>> getImpactPaths() {
        return impactPaths;
    }
    
    public ImpactLevel getImpactLevel() {
        return impactLevel;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public int getTotalAffectedCount() {
        return directlyAffectedElements.size() + transitivelyAffectedElements.size();
    }
    
    @Override
    public String toString() {
        return String.format("ImpactAnalysis{element='%s', level=%s, directlyAffected=%d, transitivelyAffected=%d, summary='%s'}", 
                           changedElement, impactLevel, directlyAffectedElements.size(), 
                           transitivelyAffectedElements.size(), summary);
    }
}
