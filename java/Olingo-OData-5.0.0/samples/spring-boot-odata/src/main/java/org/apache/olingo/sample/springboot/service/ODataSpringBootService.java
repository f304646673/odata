package org.apache.olingo.sample.springboot.service;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.olingo.sample.springboot.data.SpringBootDataProvider;
import org.apache.olingo.sample.springboot.edm.SpringBootEdmProvider;
import org.apache.olingo.sample.springboot.processor.SpringBootCarsProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class ODataSpringBootService {

    public void processODataRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(true);
            SpringBootDataProvider dataProvider = (SpringBootDataProvider) session.getAttribute(
                SpringBootDataProvider.class.getName());
            
            if (dataProvider == null) {
                dataProvider = new SpringBootDataProvider();
                session.setAttribute(SpringBootDataProvider.class.getName(), dataProvider);
            }

            OData odata = OData.newInstance();
            ServiceMetadata serviceMetadata = odata.createServiceMetadata(
                new SpringBootEdmProvider(), 
                new ArrayList<>()
            );

            ODataHttpHandler handler = odata.createHandler(serviceMetadata);
            handler.register(new SpringBootCarsProcessor(dataProvider));
            
            handler.process(request, response);
                
        } catch (RuntimeException e) {
            throw new ServletException("OData processing failed", e);
        }
    }
}
