package org.apache.olingo.schemamanager.service;

import org.apache.olingo.schemamanager.analyzer.ODataSchemaAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * OData Schema分析服务示例
 * 演示如何使用ODataSchemaAnalyzer进行Schema分析
 */
@Service
public class SchemaAnalysisService {
    
    @Autowired
    private ODataSchemaAnalyzer analyzer;
    
    /**
     * 完整分析流程示例
     */
    public void performCompleteAnalysis(String directoryPath) {
        System.out.println("=== OData Schema Analysis Demo ===");
        
        // 1. 分析目录
        System.out.println("1. Analyzing directory: " + directoryPath);
        ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);
        
        if (result.isSuccess()) {
            System.out.println("✓ Analysis completed successfully");
        } else {
            System.out.println("✗ Analysis failed with errors:");
            result.getErrors().forEach(error -> System.out.println("  - " + error));
        }
        
        // 2. 显示Import验证结果
        System.out.println("\n2. Import Validation:");
        ODataSchemaAnalyzer.ImportValidationResult importResult = result.getImportValidation();
        if (importResult.isValid()) {
            System.out.println("✓ All imports are valid");
        } else {
            System.out.println("✗ Import validation issues found:");
            importResult.getMissingImports().forEach(missing -> 
                System.out.println("  Missing: " + missing));
            importResult.getCircularDependencies().forEach(circular -> 
                System.out.println("  Circular: " + circular));
        }
        
        // 3. 显示统计信息
        System.out.println("\n3. Statistics:");
        Map<String, Object> stats = analyzer.getStatistics();
        stats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        
        // 4. 演示类型查询
        System.out.println("\n4. Type Query Examples:");
        demonstrateTypeQueries();
        
        // 5. 演示搜索功能
        System.out.println("\n5. Search Examples:");
        demonstrateSearch();
    }
    
    /**
     * 演示类型查询功能
     */
    private void demonstrateTypeQueries() {
        // 搜索一些常见的类型进行演示
        List<ODataSchemaAnalyzer.TypeDetailInfo> allTypes = analyzer.searchTypes("");
        
        if (!allTypes.isEmpty()) {
            // 取前几个类型进行详细分析
            for (int i = 0; i < Math.min(3, allTypes.size()); i++) {
                ODataSchemaAnalyzer.TypeDetailInfo typeInfo = allTypes.get(i);
                
                System.out.println("\n  Type: " + typeInfo.getFullQualifiedName());
                System.out.println("    Kind: " + typeInfo.getTypeKind());
                System.out.println("    Source: " + typeInfo.getSourceFile());
                System.out.println("    Direct Dependencies: " + typeInfo.getDirectDependencies().size());
                System.out.println("    All Dependencies: " + typeInfo.getAllDependencies().size());
                System.out.println("    Dependents: " + typeInfo.getDependents().size());
                
                // 显示依赖详情
                if (!typeInfo.getDirectDependencies().isEmpty()) {
                    System.out.println("    Direct Deps: " + typeInfo.getDirectDependencies());
                }
                
                if (!typeInfo.getDependents().isEmpty()) {
                    System.out.println("    Used by: " + typeInfo.getDependents());
                }
            }
        } else {
            System.out.println("  No types found in repository");
        }
    }
    
    /**
     * 演示搜索功能
     */
    private void demonstrateSearch() {
        // 搜索包含"Type"的类型
        List<ODataSchemaAnalyzer.TypeDetailInfo> searchResults = analyzer.searchTypes("Type");
        System.out.println("  Search for 'Type': " + searchResults.size() + " results");
        
        // 搜索包含"Entity"的类型
        searchResults = analyzer.searchTypes("Entity");
        System.out.println("  Search for 'Entity': " + searchResults.size() + " results");
        
        // 显示搜索结果的前几个
        if (!searchResults.isEmpty()) {
            System.out.println("  First few results:");
            for (int i = 0; i < Math.min(3, searchResults.size()); i++) {
                ODataSchemaAnalyzer.TypeDetailInfo info = searchResults.get(i);
                System.out.println("    - " + info.getFullQualifiedName() + 
                                 " (" + info.getTypeKind() + ")");
            }
        }
    }
    
    /**
     * 获取类型依赖关系报告
     */
    public String generateDependencyReport(String typeName) {
        StringBuilder report = new StringBuilder();
        
        // 尝试查找不同类型的定义
        ODataSchemaAnalyzer.TypeDetailInfo typeInfo = analyzer.getEntityTypeDetail(typeName);
        if (typeInfo == null) {
            typeInfo = analyzer.getComplexTypeDetail(typeName);
        }
        if (typeInfo == null) {
            typeInfo = analyzer.getEnumTypeDetail(typeName);
        }
        
        if (typeInfo == null) {
            return "Type not found: " + typeName;
        }
        
        report.append("=== Dependency Report for ").append(typeName).append(" ===\n");
        report.append("Type Kind: ").append(typeInfo.getTypeKind()).append("\n");
        report.append("Namespace: ").append(typeInfo.getNamespace()).append("\n");
        report.append("Source File: ").append(typeInfo.getSourceFile()).append("\n\n");
        
        // 直接依赖
        report.append("Direct Dependencies (").append(typeInfo.getDirectDependencies().size()).append("):\n");
        for (String dep : typeInfo.getDirectDependencies()) {
            report.append("  - ").append(dep).append("\n");
        }
        
        // 所有依赖
        report.append("\nAll Dependencies (").append(typeInfo.getAllDependencies().size()).append("):\n");
        for (String dep : typeInfo.getAllDependencies()) {
            report.append("  - ").append(dep).append("\n");
        }
        
        // 依赖者
        report.append("\nDependent Types (").append(typeInfo.getDependents().size()).append("):\n");
        for (String dependent : typeInfo.getDependents()) {
            report.append("  - ").append(dependent).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * 清理数据
     */
    public void clearData() {
        // 由于analyzer使用了repository，我们可以通过analyzer的repository清理
        // 这里可以添加清理逻辑
        System.out.println("Data cleared");
    }
}
