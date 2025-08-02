package org.apache.olingo.compliance.engine.rule;

import org.apache.olingo.compliance.engine.rule.ValidationRule.RuleResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ValidationRule.RuleResult inner class.
 * Ensures 100% code coverage for all methods and branches.
 */
class ValidationRuleResultTest {

    @Test
    void testConstructor() {
        String ruleName = "test-rule";
        boolean passed = true;
        String message = "Test message";
        String details = "Test details";
        long executionTime = 100L;
        
        RuleResult result = new RuleResult(ruleName, passed, message, details, executionTime);
        
        assertEquals(ruleName, result.getRuleName());
        assertTrue(result.isPassed());
        assertFalse(result.isFailed());
        assertEquals(message, result.getMessage());
        assertEquals(details, result.getDetails());
        assertEquals(executionTime, result.getExecutionTime());
    }

    @Test
    void testConstructorWithFailedResult() {
        String ruleName = "failed-rule";
        boolean passed = false;
        String message = "Failure message";
        String details = "Failure details";
        long executionTime = 200L;
        
        RuleResult result = new RuleResult(ruleName, passed, message, details, executionTime);
        
        assertEquals(ruleName, result.getRuleName());
        assertFalse(result.isPassed());
        assertTrue(result.isFailed());
        assertEquals(message, result.getMessage());
        assertEquals(details, result.getDetails());
        assertEquals(executionTime, result.getExecutionTime());
    }

    @Test
    void testPassStaticMethod() {
        String ruleName = "pass-rule";
        long executionTime = 50L;
        
        RuleResult result = RuleResult.pass(ruleName, executionTime);
        
        assertEquals(ruleName, result.getRuleName());
        assertTrue(result.isPassed());
        assertFalse(result.isFailed());
        assertNull(result.getMessage());
        assertNull(result.getDetails());
        assertEquals(executionTime, result.getExecutionTime());
    }

    @Test
    void testFailStaticMethodWithMessage() {
        String ruleName = "fail-rule";
        String message = "Failure occurred";
        long executionTime = 150L;
        
        RuleResult result = RuleResult.fail(ruleName, message, executionTime);
        
        assertEquals(ruleName, result.getRuleName());
        assertFalse(result.isPassed());
        assertTrue(result.isFailed());
        assertEquals(message, result.getMessage());
        assertNull(result.getDetails());
        assertEquals(executionTime, result.getExecutionTime());
    }

    @Test
    void testFailStaticMethodWithMessageAndDetails() {
        String ruleName = "fail-rule-details";
        String message = "Detailed failure";
        String details = "Additional failure information";
        long executionTime = 75L;
        
        RuleResult result = RuleResult.fail(ruleName, message, details, executionTime);
        
        assertEquals(ruleName, result.getRuleName());
        assertFalse(result.isPassed());
        assertTrue(result.isFailed());
        assertEquals(message, result.getMessage());
        assertEquals(details, result.getDetails());
        assertEquals(executionTime, result.getExecutionTime());
    }

    @Test
    void testToString() {
        String ruleName = "toString-rule";
        String message = "Test message for toString";
        long executionTime = 25L;
        
        RuleResult passResult = RuleResult.pass(ruleName, executionTime);
        RuleResult failResult = RuleResult.fail(ruleName, message, executionTime);
        
        String passString = passResult.toString();
        String failString = failResult.toString();
        
        assertTrue(passString.contains(ruleName));
        assertTrue(passString.contains("true"));
        assertTrue(passString.contains("25ms"));
        
        assertTrue(failString.contains(ruleName));
        assertTrue(failString.contains("false"));
        assertTrue(failString.contains(message));
        assertTrue(failString.contains("25ms"));
    }

    @Test
    void testToStringWithNullMessage() {
        String ruleName = "null-message-rule";
        long executionTime = 10L;
        
        RuleResult result = new RuleResult(ruleName, false, null, null, executionTime);
        String resultString = result.toString();
        
        assertTrue(resultString.contains(ruleName));
        assertTrue(resultString.contains("false"));
        assertTrue(resultString.contains("null"));
        assertTrue(resultString.contains("10ms"));
    }

    @Test
    void testGettersWithNullValues() {
        String ruleName = "null-values-rule";
        
        RuleResult result = new RuleResult(ruleName, true, null, null, 0L);
        
        assertEquals(ruleName, result.getRuleName());
        assertTrue(result.isPassed());
        assertNull(result.getMessage());
        assertNull(result.getDetails());
        assertEquals(0L, result.getExecutionTime());
    }
}
