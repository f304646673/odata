package org.apache.olingo.schemamanager.controller;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OData Schema管理REST控制器
 */
@RestController
@RequestMapping("/api/odata/schema")
public class ODataSchemaController {
    
    @Autowired
    private ODataXmlLoader xmlLoader;
    
    @Autowired
    private SchemaRepository repository;
    
    /**
     * 从目录加载OData XML文件
     */
    @PostMapping("/load")
    public ODataXmlLoader.LoadResult loadFromDirectory(@RequestParam String directoryPath) {
        return xmlLoader.loadFromDirectory(directoryPath);
    }
    
    /**
     * 获取所有已加载的Schema
     */
    @GetMapping("/schemas")
    public Map<String, CsdlSchema> getAllSchemas() {
        return repository.getAllSchemas();
    }
    
    /**
     * 根据namespace获取Schema
     */
    @GetMapping("/schemas/{namespace}")
    public CsdlSchema getSchema(@PathVariable String namespace) {
        return repository.getSchema(namespace);
    }
    
    /**
     * 获取所有namespace
     */
    @GetMapping("/namespaces")
    public java.util.Set<String> getAllNamespaces() {
        return repository.getAllNamespaces();
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public SchemaRepository.RepositoryStatistics getStatistics() {
        return repository.getStatistics();
    }
    
    /**
     * 清理所有数据
     */
    @DeleteMapping("/clear")
    public void clearAll() {
        xmlLoader.clear();
        repository.clear();
    }
}
