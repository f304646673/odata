package org.apache.olingo.schemamanager.controller;

import org.apache.olingo.schemamanager.analyzer.ODataSchemaAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OData Schema分析REST控制器
 */
@RestController
@RequestMapping("/api/odata/analyzer")
public class ODataAnalyzerController {
    
    @Autowired
    private ODataSchemaAnalyzer analyzer;
    
    /**
     * 分析指定目录下的OData XML文件
     */
    @PostMapping("/analyze")
    public ResponseEntity<ODataSchemaAnalyzer.AnalysisResult> analyzeDirectory(@RequestParam String directoryPath) {
        try {
            ODataSchemaAnalyzer.AnalysisResult result = analyzer.analyzeDirectory(directoryPath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取EntityType详细信息
     */
    @GetMapping("/entitytype/{fullQualifiedName}")
    public ResponseEntity<ODataSchemaAnalyzer.TypeDetailInfo> getEntityTypeDetail(
            @PathVariable String fullQualifiedName) {
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getEntityTypeDetail(fullQualifiedName);
        if (detail != null) {
            return ResponseEntity.ok(detail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取ComplexType详细信息
     */
    @GetMapping("/complextype/{fullQualifiedName}")
    public ResponseEntity<ODataSchemaAnalyzer.TypeDetailInfo> getComplexTypeDetail(
            @PathVariable String fullQualifiedName) {
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getComplexTypeDetail(fullQualifiedName);
        if (detail != null) {
            return ResponseEntity.ok(detail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取EnumType详细信息
     */
    @GetMapping("/enumtype/{fullQualifiedName}")
    public ResponseEntity<ODataSchemaAnalyzer.TypeDetailInfo> getEnumTypeDetail(
            @PathVariable String fullQualifiedName) {
        ODataSchemaAnalyzer.TypeDetailInfo detail = analyzer.getEnumTypeDetail(fullQualifiedName);
        if (detail != null) {
            return ResponseEntity.ok(detail);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 搜索类型
     */
    @GetMapping("/search")
    public ResponseEntity<List<ODataSchemaAnalyzer.TypeDetailInfo>> searchTypes(@RequestParam String namePattern) {
        List<ODataSchemaAnalyzer.TypeDetailInfo> results = analyzer.searchTypes(namePattern);
        return ResponseEntity.ok(results);
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = analyzer.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
