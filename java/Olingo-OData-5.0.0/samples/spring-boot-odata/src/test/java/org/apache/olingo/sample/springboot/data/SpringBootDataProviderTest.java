package org.apache.olingo.sample.springboot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for SpringBootDataProvider
 * 
 * Tests the core functionality of the data provider including:
 * - CRUD operations on car data
 * - Thread safety
 * - Data integrity
 * - Sample data initialization
 */
class SpringBootDataProviderTest {

    private SpringBootDataProvider dataProvider;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        dataProvider = new SpringBootDataProvider();
    }

    @Test
    @DisplayName("Should initialize with sample data")
    void shouldInitializeWithSampleData() {
        List<Map<String, Object>> cars = dataProvider.getAllCars();
        
        assertNotNull(cars);
        assertEquals(5, cars.size()); // Expected 5 sample cars
        assertTrue(dataProvider.getCarCount() > 0);
    }

    @Test
    @DisplayName("Should create car with all required fields")
    void shouldCreateCarWithAllRequiredFields() {
        String brand = "Toyota";
        String model = "Camry";
        String color = "Blue";
        int year = 2023;
        double price = 30000.0;

        Map<String, Object> car = dataProvider.createCar(brand, model, color, year, price);

        assertNotNull(car);
        assertNotNull(car.get("Id"));
        assertEquals(brand, car.get("Brand"));
        assertEquals(model, car.get("Model"));
        assertEquals(color, car.get("Color"));
        assertEquals(year, car.get("Year"));
        assertEquals(price, car.get("Price"));
    }

    @Test
    @DisplayName("Should assign unique IDs to new cars")
    void shouldAssignUniqueIdsToNewCars() {
        Map<String, Object> car1 = dataProvider.createCar("Honda", "Civic", "Red", 2022, 25000.0);
        Map<String, Object> car2 = dataProvider.createCar("Ford", "Focus", "Green", 2023, 27000.0);

        assertNotNull(car1.get("Id"));
        assertNotNull(car2.get("Id"));
        assertNotEquals(car1.get("Id"), car2.get("Id"));
    }

    @Test
    @DisplayName("Should retrieve car by ID")
    void shouldRetrieveCarById() {
        Map<String, Object> createdCar = dataProvider.createCar("Mazda", "CX-5", "Gray", 2023, 32000.0);
        int carId = (Integer) createdCar.get("Id");

        Map<String, Object> retrievedCar = dataProvider.getCarById(carId);

        assertNotNull(retrievedCar);
        assertEquals(carId, retrievedCar.get("Id"));
        assertEquals("Mazda", retrievedCar.get("Brand"));
        assertEquals("CX-5", retrievedCar.get("Model"));
    }

    @Test
    @DisplayName("Should return null for non-existent car ID")
    void shouldReturnNullForNonExistentCarId() {
        Map<String, Object> car = dataProvider.getCarById(99999);
        assertNull(car);
    }

    @Test
    @DisplayName("Should update existing car")
    void shouldUpdateExistingCar() {
        Map<String, Object> createdCar = dataProvider.createCar("Nissan", "Altima", "White", 2022, 28000.0);
        int carId = (Integer) createdCar.get("Id");

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("Brand", "Nissan");
        updatedData.put("Model", "Altima");
        updatedData.put("Color", "Black"); // Changed color
        updatedData.put("Year", 2023);     // Changed year
        updatedData.put("Price", 30000.0); // Changed price

        Map<String, Object> updatedCar = dataProvider.updateCar(carId, updatedData);

        assertNotNull(updatedCar);
        assertEquals(carId, updatedCar.get("Id")); // ID should be preserved
        assertEquals("Black", updatedCar.get("Color"));
        assertEquals(2023, updatedCar.get("Year"));
        assertEquals(30000.0, updatedCar.get("Price"));
    }

    @Test
    @DisplayName("Should return null when updating non-existent car")
    void shouldReturnNullWhenUpdatingNonExistentCar() {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("Brand", "Test");
        updatedData.put("Model", "Test");
        updatedData.put("Color", "Test");
        updatedData.put("Year", 2023);
        updatedData.put("Price", 1000.0);

        Map<String, Object> result = dataProvider.updateCar(99999, updatedData);
        assertNull(result);
    }

    @Test
    @DisplayName("Should delete existing car")
    void shouldDeleteExistingCar() {
        Map<String, Object> createdCar = dataProvider.createCar("Subaru", "Outback", "Blue", 2023, 35000.0);
        int carId = (Integer) createdCar.get("Id");

        boolean deleted = dataProvider.deleteCar(carId);
        assertTrue(deleted);

        Map<String, Object> retrievedCar = dataProvider.getCarById(carId);
        assertNull(retrievedCar);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent car")
    void shouldReturnFalseWhenDeletingNonExistentCar() {
        boolean deleted = dataProvider.deleteCar(99999);
        assertFalse(deleted);
    }

    @Test
    @DisplayName("Should maintain correct car count")
    void shouldMaintainCorrectCarCount() {
        int initialCount = dataProvider.getCarCount();

        // Add a car
        dataProvider.createCar("Hyundai", "Elantra", "Silver", 2023, 24000.0);
        assertEquals(initialCount + 1, dataProvider.getCarCount());

        // Add another car
        Map<String, Object> car2 = dataProvider.createCar("Kia", "Sorento", "Red", 2023, 33000.0);
        assertEquals(initialCount + 2, dataProvider.getCarCount());

        // Delete a car
        int carId = (Integer) car2.get("Id");
        dataProvider.deleteCar(carId);
        assertEquals(initialCount + 1, dataProvider.getCarCount());
    }

    @Test
    @DisplayName("Should be thread-safe for concurrent operations")
    void shouldBeThreadSafeForConcurrentOperations() throws InterruptedException {
        int numThreads = 10;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Clear initial data for consistent testing
        SpringBootDataProvider testProvider = new SpringBootDataProvider();

        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        // Create, read, update, delete operations
                        Map<String, Object> car = testProvider.createCar(
                            "Brand" + threadId + j, 
                            "Model" + threadId + j, 
                            "Color" + threadId + j, 
                            2020 + (j % 5), 
                            20000.0 + (j * 1000)
                        );
                        
                        int carId = (Integer) car.get("Id");
                        
                        // Read the car
                        testProvider.getCarById(carId);
                        
                        // Update the car
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("Brand", "Updated" + threadId + j);
                        updatedData.put("Model", "UpdatedModel" + threadId + j);
                        updatedData.put("Color", "UpdatedColor" + threadId + j);
                        updatedData.put("Year", 2023);
                        updatedData.put("Price", 25000.0);
                        testProvider.updateCar(carId, updatedData);
                        
                        // Sometimes delete the car
                        if (j % 3 == 0) {
                            testProvider.deleteCar(carId);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Verify data integrity
        List<Map<String, Object>> allCars = testProvider.getAllCars();
        assertNotNull(allCars);
        assertTrue(testProvider.getCarCount() >= 0);
        
        // All remaining cars should have valid data
        for (Map<String, Object> car : allCars) {
            assertNotNull(car.get("Id"));
            assertNotNull(car.get("Brand"));
            assertNotNull(car.get("Model"));
            assertNotNull(car.get("Color"));
            assertNotNull(car.get("Year"));
            assertNotNull(car.get("Price"));
        }
    }

    @Test
    @DisplayName("Should handle edge cases for car data")
    void shouldHandleEdgeCasesForCarData() {
        // Test with empty strings (should be allowed)
        Map<String, Object> car1 = dataProvider.createCar("", "", "", 0, 0.0);
        assertNotNull(car1);
        assertEquals("", car1.get("Brand"));
        
        // Test with very large price
        Map<String, Object> car2 = dataProvider.createCar("Luxury", "Model", "Gold", 2023, Double.MAX_VALUE);
        assertNotNull(car2);
        assertEquals(Double.MAX_VALUE, car2.get("Price"));
        
        // Test with negative year (edge case)
        Map<String, Object> car3 = dataProvider.createCar("Vintage", "Classic", "Brown", -1900, 50000.0);
        assertNotNull(car3);
        assertEquals(-1900, car3.get("Year"));
    }
}
