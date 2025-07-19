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
package org.apache.olingo.sample.springboot.xmldb.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "manufacturers")
public class ManufacturerEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    private Integer founded;
    
    private String headquarters;
    
    @OneToMany(mappedBy = "manufacturer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CarEntity> cars;
    
    // Constructors
    public ManufacturerEntity() {}
    
    public ManufacturerEntity(String name, Integer founded, String headquarters) {
        this.name = name;
        this.founded = founded;
        this.headquarters = headquarters;
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getFounded() {
        return founded;
    }
    
    public void setFounded(Integer founded) {
        this.founded = founded;
    }
    
    public String getHeadquarters() {
        return headquarters;
    }
    
    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }
    
    public List<CarEntity> getCars() {
        return cars;
    }
    
    public void setCars(List<CarEntity> cars) {
        this.cars = cars;
    }
}
