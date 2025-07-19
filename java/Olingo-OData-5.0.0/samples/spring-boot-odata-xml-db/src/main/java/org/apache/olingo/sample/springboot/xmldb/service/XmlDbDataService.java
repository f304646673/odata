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
package org.apache.olingo.sample.springboot.xmldb.service;

import org.apache.olingo.sample.springboot.xmldb.entity.CarEntity;
import org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity;
import org.apache.olingo.sample.springboot.xmldb.repository.CarRepository;
import org.apache.olingo.sample.springboot.xmldb.repository.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class XmlDbDataService {
    
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    
    @PostConstruct
    public void initializeData() {
        // Clear existing data
        carRepository.deleteAll();
        manufacturerRepository.deleteAll();
        
        // Initialize with sample data
        createSampleData();
    }
    
    private void createSampleData() {
        // Create manufacturers
        ManufacturerEntity bmw = new ManufacturerEntity("BMW", 1916, "Munich, Germany");
        ManufacturerEntity audi = new ManufacturerEntity("Audi", 1909, "Ingolstadt, Germany");
        ManufacturerEntity toyota = new ManufacturerEntity("Toyota", 1937, "Toyota, Japan");
        
        bmw = manufacturerRepository.save(bmw);
        audi = manufacturerRepository.save(audi);
        toyota = manufacturerRepository.save(toyota);
        
        // Create cars
        CarEntity car1 = new CarEntity("X5", 2021, new BigDecimal("65000.00"), "USD", bmw.getId());
        CarEntity car2 = new CarEntity("A4", 2022, new BigDecimal("45000.00"), "USD", audi.getId());
        CarEntity car3 = new CarEntity("Camry", 2023, new BigDecimal("35000.00"), "USD", toyota.getId());
        CarEntity car4 = new CarEntity("320i", 2021, new BigDecimal("40000.00"), "USD", bmw.getId());
        
        carRepository.saveAll(Arrays.asList(car1, car2, car3, car4));
    }
    
    // Car operations
    public List<CarEntity> getAllCars() {
        return carRepository.findAll();
    }
    
    public CarEntity getCarById(Integer id) {
        return carRepository.findById(id).orElse(null);
    }
    
    public List<CarEntity> getCarsByManufacturer(Integer manufacturerId) {
        return carRepository.findByManufacturerId(manufacturerId);
    }
    
    public CarEntity saveCar(CarEntity car) {
        return carRepository.save(car);
    }
    
    public void deleteCar(Integer id) {
        carRepository.deleteById(id);
    }
    
    // Manufacturer operations
    public List<ManufacturerEntity> getAllManufacturers() {
        return manufacturerRepository.findAll();
    }
    
    public ManufacturerEntity getManufacturerById(Integer id) {
        return manufacturerRepository.findById(id).orElse(null);
    }
    
    public ManufacturerEntity saveManufacturer(ManufacturerEntity manufacturer) {
        return manufacturerRepository.save(manufacturer);
    }
    
    public void deleteManufacturer(Integer id) {
        manufacturerRepository.deleteById(id);
    }
}
