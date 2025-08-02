package org.apache.olingo.compliance.engine.rule.security;

import org.apache.olingo.compliance.core.api.ValidationConfig;
import org.apache.olingo.compliance.engine.core.ValidationContext;
import org.apache.olingo.compliance.engine.rule.ValidationRule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for AbstractSecurityRule class.
 * Ensures 100% code coverage for all methods and branches.
 */
class AbstractSecurityRuleTest {

    @Mock
    private ValidationContext mockContext;
    
    @Mock
    private ValidationConfig mockConfig;
    
    private TestSecurityRule testRule;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testRule = new TestSecurityRule();
    }
    
    @Test
    void testGetName() {
        assertEquals("test-security-rule", testRule.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Test security rule", testRule.getDescription());
    }
    
    @Test
    void testGetCategory() {
        assertEquals("security", testRule.getCategory());
    }
    
    @Test
    void testGetSeverity() {
        assertEquals("error", testRule.getSeverity());
    }
    
    @Test
    void testGetEstimatedExecutionTime() {
        assertEquals(200L, testRule.getEstimatedExecutionTime());
    }
    
    @Test
    void testIsApplicable_AllConditionsTrue() {
        when(mockConfig.isSecurityValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-security-rule")).thenReturn(true);
        testRule.setSecurityApplicable(true);
        
        assertTrue(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_SecurityValidationDisabled() {
        when(mockConfig.isSecurityValidationEnabled()).thenReturn(false);
        when(mockConfig.isRuleEnabled("test-security-rule")).thenReturn(true);
        testRule.setSecurityApplicable(true);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_RuleDisabled() {
        when(mockConfig.isSecurityValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-security-rule")).thenReturn(false);
        testRule.setSecurityApplicable(true);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_SecurityNotApplicable() {
        when(mockConfig.isSecurityValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-security-rule")).thenReturn(true);
        testRule.setSecurityApplicable(false);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_AllConditionsFalse() {
        when(mockConfig.isSecurityValidationEnabled()).thenReturn(false);
        when(mockConfig.isRuleEnabled("test-security-rule")).thenReturn(false);
        testRule.setSecurityApplicable(false);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    /**
     * Concrete implementation of AbstractSecurityRule for testing.
     */
    private static class TestSecurityRule extends AbstractSecurityRule {
        private boolean securityApplicable = true;
        
        public TestSecurityRule() {
            super("test-security-rule", "Test security rule", "error");
        }
        
        @Override
        protected boolean isSecurityApplicable(ValidationContext context, ValidationConfig config) {
            return securityApplicable;
        }
        
        @Override
        public ValidationRule.RuleResult validate(ValidationContext context, ValidationConfig config) {
            return ValidationRule.RuleResult.pass("test-security-rule", 0);
        }
        
        public void setSecurityApplicable(boolean applicable) {
            this.securityApplicable = applicable;
        }
    }
}
