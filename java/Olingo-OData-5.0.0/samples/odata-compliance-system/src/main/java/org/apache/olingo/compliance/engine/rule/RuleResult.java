package org.apache.olingo.compliance.engine.rule;

import org.apache.olingo.compliance.core.model.ComplianceIssue;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of executing a validation rule.
 * Contains all issues found during rule execution.
 */
public class RuleResult {
    private final List<ComplianceIssue> issues;
    private final boolean passed;
    
    public RuleResult() {
        this.issues = new ArrayList<>();
        this.passed = true;
    }
    
    public RuleResult(List<ComplianceIssue> issues) {
        this.issues = new ArrayList<>(issues);
        this.passed = issues.isEmpty();
    }
    
    public RuleResult(ComplianceIssue issue) {
        this.issues = new ArrayList<>();
        this.issues.add(issue);
        this.passed = false;
    }
    
    public List<ComplianceIssue> getIssues() {
        return new ArrayList<>(issues);
    }
    
    public boolean isPassed() {
        return passed;
    }
    
    public boolean hasFailed() {
        return !passed;
    }
    
    public void addIssue(ComplianceIssue issue) {
        issues.add(issue);
    }
    
    public static RuleResult success() {
        return new RuleResult();
    }
    
    public static RuleResult failure(ComplianceIssue issue) {
        return new RuleResult(issue);
    }
    
    public static RuleResult failure(List<ComplianceIssue> issues) {
        return new RuleResult(issues);
    }
}
