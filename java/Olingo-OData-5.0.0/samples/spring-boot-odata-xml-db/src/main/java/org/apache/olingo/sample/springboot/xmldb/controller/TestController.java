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
package org.apache.olingo.sample.springboot.xmldb.controller;

import java.math.BigDecimal;
import java.util.List;

import org.apache.olingo.sample.springboot.xmldb.entity.CarEntity;
import org.apache.olingo.sample.springboot.xmldb.entity.ManufacturerEntity;
import org.apache.olingo.sample.springboot.xmldb.service.XmlDbDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private XmlDbDataService dataService;

    @GetMapping("/cars")
    public List<CarEntity> getAllCars() {
        return dataService.getAllCars();
    }

    @GetMapping("/cars/{id}")
    public CarEntity getCar(@PathVariable Integer id) {
        return dataService.getCarById(id);
    }

    @GetMapping("/manufacturers")
    public List<ManufacturerEntity> getAllManufacturers() {
        return dataService.getAllManufacturers();
    }

    @GetMapping("/manufacturers/{id}")
    public ManufacturerEntity getManufacturer(@PathVariable Integer id) {
        return dataService.getManufacturerById(id);
    }

    @PostMapping("/cars")
    public CarEntity createCar(@RequestParam String model,
                               @RequestParam Integer modelYear,
                               @RequestParam BigDecimal price,
                               @RequestParam String currency,
                               @RequestParam Integer manufacturerId) {
        CarEntity car = new CarEntity(model, modelYear, price, currency, manufacturerId);
        return dataService.saveCar(car);
    }

    @PostMapping("/manufacturers")
    public ManufacturerEntity createManufacturer(@RequestParam String name,
                                                  @RequestParam Integer founded,
                                                  @RequestParam String headquarters) {
        ManufacturerEntity manufacturer = new ManufacturerEntity(name, founded, headquarters);
        return dataService.saveManufacturer(manufacturer);
    }

    @DeleteMapping("/cars/{id}")
    public String deleteCar(@PathVariable Integer id) {
        dataService.deleteCar(id);
        return "Car deleted successfully";
    }

    @DeleteMapping("/manufacturers/{id}")
    public String deleteManufacturer(@PathVariable Integer id) {
        dataService.deleteManufacturer(id);
        return "Manufacturer deleted successfully";
    }

    @GetMapping("/test")
    public String test() {
        return "Spring Boot XML DB OData API is running!";
    }
}
