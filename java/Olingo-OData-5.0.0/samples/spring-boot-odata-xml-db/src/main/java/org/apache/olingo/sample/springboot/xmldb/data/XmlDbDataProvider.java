/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.sample.springboot.xmldb.data;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.sample.springboot.xmldb.entity.CarEntity;
import org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity;
import org.apache.olingo.sample.springboot.xmldb.service.XmlDbDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XmlDbDataProvider {

    @Autowired
    private XmlDbDataService dataService;

    public EntityCollection getCars() {
        EntityCollection entityCollection = new EntityCollection();
        List<CarEntity> cars = dataService.getAllCars();
        
        for (CarEntity carEntity : cars) {
            entityCollection.getEntities().add(convertCarToODataEntity(carEntity));
        }
        
        return entityCollection;
    }

    public Entity getCar(int carId) {
        CarEntity carEntity = dataService.getCarById(carId);
        return carEntity != null ? convertCarToODataEntity(carEntity) : null;
    }

    public EntityCollection getManufacturers() {
        EntityCollection entityCollection = new EntityCollection();
        List<ManufacturerEntity> manufacturers = dataService.getAllManufacturers();
        
        for (ManufacturerEntity manufacturerEntity : manufacturers) {
            entityCollection.getEntities().add(convertManufacturerToODataEntity(manufacturerEntity));
        }
        
        return entityCollection;
    }

    public Entity getManufacturer(int manufacturerId) {
        ManufacturerEntity manufacturerEntity = dataService.getManufacturerById(manufacturerId);
        return manufacturerEntity != null ? convertManufacturerToODataEntity(manufacturerEntity) : null;
    }

    private Entity convertCarToODataEntity(CarEntity carEntity) {
        Entity entity = new Entity();
        
        entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, carEntity.getId()));
        entity.addProperty(new Property(null, "Model", ValueType.PRIMITIVE, carEntity.getModel()));
        entity.addProperty(new Property(null, "ModelYear", ValueType.PRIMITIVE, carEntity.getModelYear()));
        entity.addProperty(new Property(null, "Price", ValueType.PRIMITIVE, carEntity.getPrice()));
        entity.addProperty(new Property(null, "Currency", ValueType.PRIMITIVE, carEntity.getCurrency()));
        entity.addProperty(new Property(null, "ManufacturerID", ValueType.PRIMITIVE, carEntity.getManufacturerId()));
        
        entity.setId(createId("Cars", carEntity.getId()));
        
        return entity;
    }

    private Entity convertManufacturerToODataEntity(ManufacturerEntity manufacturerEntity) {
        Entity entity = new Entity();
        
        entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, manufacturerEntity.getId()));
        entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, manufacturerEntity.getName()));
        entity.addProperty(new Property(null, "Founded", ValueType.PRIMITIVE, manufacturerEntity.getFounded()));
        entity.addProperty(new Property(null, "Headquarters", ValueType.PRIMITIVE, manufacturerEntity.getHeadquarters()));
        
        entity.setId(createId("Manufacturers", manufacturerEntity.getId()));
        
        return entity;
    }

    private java.net.URI createId(String entitySetName, Object id) {
        try {
            return new java.net.URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (Exception e) {
            throw new RuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }
}
