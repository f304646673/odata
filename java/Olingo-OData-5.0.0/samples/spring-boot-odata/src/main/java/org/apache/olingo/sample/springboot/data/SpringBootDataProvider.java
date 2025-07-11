package org.apache.olingo.sample.springboot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Boot Data Provider
 * 
 * This class is inspired by the original DataProvider from the HttpServlet sample
 * but adapted for Spring Boot environment. It manages in-memory car data
 * with thread-safe operations.
 */
public class SpringBootDataProvider {

    private final Map<Integer, Map<String, Object>> cars;
    private final AtomicInteger nextId;

    public SpringBootDataProvider() {
        this.cars = new HashMap<>();
        this.nextId = new AtomicInteger(1);
        initializeSampleData();
    }

    /**
     * Initialize with sample car data
     */
    private void initializeSampleData() {
        createCar("BMW", "X5", "Blue", 2022, 75000.00);
        createCar("Audi", "Q7", "Black", 2023, 85000.00);
        createCar("Mercedes", "GLE", "White", 2021, 80000.00);
        createCar("Tesla", "Model S", "Red", 2023, 95000.00);
        createCar("Porsche", "Cayenne", "Silver", 2022, 120000.00);
    }

    /**
     * Create a new car
     */
    public Map<String, Object> createCar(String brand, String model, String color, int year, double price) {
        Map<String, Object> car = new HashMap<>();
        int id = nextId.getAndIncrement();
        
        car.put("Id", id);
        car.put("Brand", brand);
        car.put("Model", model);
        car.put("Color", color);
        car.put("Year", year);
        car.put("Price", price);
        
        synchronized (cars) {
            cars.put(id, car);
        }
        
        return car;
    }

    /**
     * Get all cars
     */
    public List<Map<String, Object>> getAllCars() {
        synchronized (cars) {
            return new ArrayList<>(cars.values());
        }
    }

    /**
     * Get car by ID
     */
    public Map<String, Object> getCarById(int id) {
        synchronized (cars) {
            return cars.get(id);
        }
    }

    /**
     * Update car
     */
    public Map<String, Object> updateCar(int id, Map<String, Object> updatedCar) {
        synchronized (cars) {
            if (cars.containsKey(id)) {
                updatedCar.put("Id", id); // Ensure ID is preserved
                cars.put(id, updatedCar);
                return updatedCar;
            }
            return null;
        }
    }

    /**
     * Delete car
     */
    public boolean deleteCar(int id) {
        synchronized (cars) {
            return cars.remove(id) != null;
        }
    }

    /**
     * Get total car count
     */
    public int getCarCount() {
        synchronized (cars) {
            return cars.size();
        }
    }
}
