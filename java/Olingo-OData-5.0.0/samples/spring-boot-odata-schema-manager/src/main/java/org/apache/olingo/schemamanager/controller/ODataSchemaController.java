package org.apache.olingo.schemamanager.controller;

import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.schemamanager.loader.ODataXmlLoader;
import org.apache.olingo.schemamanager.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
