package org.apache.olingo.compliance.engine.rule.semantic;

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
 * Unit tests for AbstractSemanticRule class.
 * Ensures 100% code coverage for all methods and branches.
 */
class AbstractSemanticRuleTest {

    @Mock
    private ValidationContext mockContext;
    
    @Mock
    private ValidationConfig mockConfig;
    
    private TestSemanticRule testRule;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testRule = new TestSemanticRule();
    }
    
    @Test
    void testGetName() {
        assertEquals("test-semantic-rule", testRule.getName());
    }
    
    @Test
    void testGetDescription() {
        assertEquals("Test semantic rule", testRule.getDescription());
    }
    
    @Test
    void testGetCategory() {
        assertEquals("semantic", testRule.getCategory());
    }
    
    @Test
    void testGetSeverity() {
        assertEquals("warning", testRule.getSeverity());
    }
    
    @Test
    void testGetEstimatedExecutionTime() {
        assertEquals(150L, testRule.getEstimatedExecutionTime());
    }
    
    @Test
    void testIsApplicable_AllConditionsTrue() {
        when(mockConfig.isSemanticValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-semantic-rule")).thenReturn(true);
        testRule.setSemanticApplicable(true);
        
        assertTrue(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_SemanticValidationDisabled() {
        when(mockConfig.isSemanticValidationEnabled()).thenReturn(false);
        when(mockConfig.isRuleEnabled("test-semantic-rule")).thenReturn(true);
        testRule.setSemanticApplicable(true);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_RuleDisabled() {
        when(mockConfig.isSemanticValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-semantic-rule")).thenReturn(false);
        testRule.setSemanticApplicable(true);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_SemanticNotApplicable() {
        when(mockConfig.isSemanticValidationEnabled()).thenReturn(true);
        when(mockConfig.isRuleEnabled("test-semantic-rule")).thenReturn(true);
        testRule.setSemanticApplicable(false);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    @Test
    void testIsApplicable_AllConditionsFalse() {
        when(mockConfig.isSemanticValidationEnabled()).thenReturn(false);
        when(mockConfig.isRuleEnabled("test-semantic-rule")).thenReturn(false);
        testRule.setSemanticApplicable(false);
        
        assertFalse(testRule.isApplicable(mockContext, mockConfig));
    }
    
    /**
     * Concrete implementation of AbstractSemanticRule for testing.
     */
    private static class TestSemanticRule extends AbstractSemanticRule {
        private boolean semanticApplicable = true;
        
        public TestSemanticRule() {
            super("test-semantic-rule", "Test semantic rule", "warning");
        }
        
        @Override
        protected boolean isSemanticApplicable(ValidationContext context, ValidationConfig config) {
            return semanticApplicable;
        }
        
        @Override
        public ValidationRule.RuleResult validate(ValidationContext context, ValidationConfig config) {
            return ValidationRule.RuleResult.pass("test-semantic-rule", 0);
        }
        
        public void setSemanticApplicable(boolean applicable) {
            this.semanticApplicable = applicable;
        }
    }
}
