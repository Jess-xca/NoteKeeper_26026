package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.LocationType;
import com.notekeeper.notekeeper.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        Location createdLocation = locationService.createLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
    }

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable String id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Location> getLocationByCode(@PathVariable String code) {
        Location location = locationService.getLocationByCode(code);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<Location>> getAllProvinces() {
        List<Location> provinces = locationService.getAllProvinces();
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Location>> getLocationsByType(@PathVariable LocationType type) {
        List<Location> locations = locationService.getLocationsByType(type);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<Location>> getChildren(@PathVariable String parentId) {
        List<Location> children = locationService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    @GetMapping("/provinces/{provinceCode}/districts")
    public ResponseEntity<List<Location>> getDistrictsByProvinceCode(@PathVariable String provinceCode) {
        List<Location> districts = locationService.getDistrictsByProvinceCode(provinceCode);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Location>> searchLocations(@RequestParam String keyword) {
        List<Location> locations = locationService.searchLocations(keyword);
        return ResponseEntity.ok(locations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable String id, @RequestBody Location location) {
        Location updatedLocation = locationService.updateLocation(id, location);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<Location>> getLocationsSorted(
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        List<Location> locations = locationService.getLocationsSorted(sortBy, direction);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Location>> getLocationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Location> locations = locationService.getLocationsPaginated(page, size);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/type/{type}/paginated")
    public ResponseEntity<Page<Location>> getLocationsByTypePaginated(
            @PathVariable LocationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Location> locations = locationService.getLocationsByTypePaginated(type, page, size);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{locationId}/count-children")
    public ResponseEntity<Long> countChildren(@PathVariable String locationId) {
        long count = locationService.countChildren(locationId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{locationId}/count-users")
    public ResponseEntity<Long> countUsers(@PathVariable String locationId) {
        long count = locationService.countUsersByLocation(locationId);
        return ResponseEntity.ok(count);
    }
}
