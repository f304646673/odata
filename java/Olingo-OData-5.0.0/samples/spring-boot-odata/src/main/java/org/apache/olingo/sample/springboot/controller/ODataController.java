package org.apache.olingo.sample.springboot.controller;

import java.io.IOException;

import org.apache.olingo.sample.springboot.service.ODataSpringBootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/cars.svc")
public class ODataController {

    @Autowired
    private ODataSpringBootService odataService;

    @RequestMapping(value = {"", "/", "/**"}, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public void handleODataRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request);
        odataService.processODataRequest(wrapper, response);
    }
    
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        public HttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }
        
        @Override
        public String getServletPath() {
            return "/cars.svc";
        }
        
        @Override
        public String getPathInfo() {
            String requestUri = getRequestURI();
            String contextPath = getContextPath();
            
            String basePath = contextPath + "/cars.svc";
            if (requestUri.startsWith(basePath)) {
                String pathInfo = requestUri.substring(basePath.length());
                if (pathInfo.isEmpty()) {
                    return null;
                }
                return pathInfo;
            }
            return null;
        }
        
        @Override
        public String getRequestURI() {
            return super.getRequestURI();
        }
        
        @Override
        public StringBuffer getRequestURL() {
            return super.getRequestURL();
        }
    }
}
