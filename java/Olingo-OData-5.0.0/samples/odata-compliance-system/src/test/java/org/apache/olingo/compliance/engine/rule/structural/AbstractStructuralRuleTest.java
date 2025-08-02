package org.apache.olingo.compliance.engine.rule.structural;

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
 * Unit tests for AbstractStructuralRule class.
 * Ensures 100% code coverage for all methods and branches.
 */
class AbstractStructuralRuleTest {

    @Mock
    private ValidationContext mockContext;
    
    @Mock
    private ValidationConfig mockConfig;
    
    private TestStructuralRule testRule;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testRule = new TestStructuralRule();
    }
    
    @Test
    void testGetName() {
        assertEquals("test-rule", testRule.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Test structural rule", testRule.getDescription());
    }
    
    @Test
    void testGetCategory() {
        assertEquals("structural", testRule.getCategory());
    }
    
    @Test
    void testGetSeverity() {
        assertEquals("error", testRule.getSeverity());
    }
    
    @Test
    void testGetEstimatedExecutionTime() {
        assertEquals(100L, testRule.getEstimatedExecutionTime());
    }
    
    @Test
    void testIsApplicable_AllConditionsTrue() {
        when(mockConfig.isStructuralValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-rule")).thenReturn(true);
        testRule.setStructurallyApplicable(true);
        
        assertTrue(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_StructuralValidationDisabled() {
        when(mockConfig.isStructuralValidationEnabled()).thenReturn(false);
        when(mockConfig.isRuleEnabled("test-rule")).thenReturn(true);
        testRule.setStructurallyApplicable(true);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_RuleDisabled() {
        when(mockConfig.isStructuralValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-rule")).thenReturn(false);
        testRule.setStructurallyApplicable(true);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_StructurallyNotApplicable() {
        when(mockConfig.isStructuralValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-rule")).thenReturn(true);
        testRule.setStructurallyApplicable(false);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_AllConditionsFalse() {
        when(mockConfig.isStructuralValidationEnabled()).thenReturn(false);
        when(mockConfig.isRuleEnabled("test-rule")).thenReturn(false);
        testRule.setStructurallyApplicable(false);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    /**
     * Concrete implementation of AbstractStructuralRule for testing.
     */
    private static class TestStructuralRule extends AbstractStructuralRule {
        private boolean structurallyApplicable = true;
        
        public TestStructuralRule() {
            super("test-rule", "Test structural rule", "error");
        }
        
        @Override
        protected boolean isStructurallyApplicable(ValidationContext context, ValidationConfig config) {
            return structurallyApplicable;
        }
        
        @Override
        public ValidationRule.RuleResult validate(ValidationContext context, ValidationConfig config) {
            return ValidationRule.RuleResult.pass("test-rule", 0);
        }
        
        public void setStructurallyApplicable(boolean applicable) {
            this.structurallyApplicable = applicable;
        }
    }
}
