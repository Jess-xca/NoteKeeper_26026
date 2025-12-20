package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.LocationDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.LocationType;
import com.notekeeper.notekeeper.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<LocationDTO> createLocation(@RequestBody Location location) {
        String resultId = locationService.createLocation(location);
        Location createdLocation = locationService.getLocationById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dtoMapper.toLocationDTO(createdLocation));
    }

    @PostMapping("/with-parent")
    public ResponseEntity<LocationDTO> createLocationWithParent(
            @RequestParam(required = false) String parentCode,
            @RequestBody Location childLocation) {
        String resultId = locationService.createLocationWithParent(parentCode, childLocation);
        Location createdLocation = locationService.getLocationById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dtoMapper.toLocationDTO(createdLocation));
    }

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();
        List<LocationDTO> locationDTOs = locations.stream()
                .map(dtoMapper::toLocationDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable String id) {
        Location location = locationService.getLocationById(id);
        return ResponseEntity.ok(dtoMapper.toLocationDTO(location));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<LocationDTO> getLocationByCode(@PathVariable String code) {
        Location location = locationService.getLocationByCode(code);
        return ResponseEntity.ok(dtoMapper.toLocationDTO(location));
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<LocationDTO>> getAllProvinces() {
        List<Location> provinces = locationService.getAllProvinces();
        List<LocationDTO> locationDTOs = provinces.stream()
                .map(dtoMapper::toLocationDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<LocationDTO>> getLocationsByType(@PathVariable LocationType type) {
        List<Location> locations = locationService.getLocationsByType(type);
        List<LocationDTO> locationDTOs = locations.stream()
                .map(dtoMapper::toLocationDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<LocationDTO>> getChildren(@PathVariable String parentId) {
        List<Location> children = locationService.getChildrenByParentId(parentId);
        List<LocationDTO> locationDTOs = children.stream()
                .map(dtoMapper::toLocationDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationDTO>> searchLocations(@RequestParam String keyword) {
        List<Location> locations = locationService.searchLocations(keyword);
        List<LocationDTO> locationDTOs = locations.stream()
                .map(dtoMapper::toLocationDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(locationDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDTO> updateLocation(@PathVariable String id, @RequestBody Location location) {
        locationService.updateLocation(id, location);
        Location updatedLocation = locationService.getLocationById(id);
        return ResponseEntity.ok(dtoMapper.toLocationDTO(updatedLocation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, String>> deleteLocation(@PathVariable String id) {
        locationService.deleteLocation(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Location deleted successfully"));
    }

    @GetMapping("/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<LocationDTO>> getLocationsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<Location> locations = locationService.getLocationsPaginated(page,
                size);
        org.springframework.data.domain.Page<LocationDTO> locationDTOs = locations.map(dtoMapper::toLocationDTO);
        return ResponseEntity.ok(locationDTOs);
    }

    @GetMapping("/type/{type}/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<LocationDTO>> getLocationsByTypePaginated(
            @PathVariable LocationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Page<Location> locations = locationService.getLocationsByTypePaginated(type,
                page, size);
        org.springframework.data.domain.Page<LocationDTO> locationDTOs = locations.map(dtoMapper::toLocationDTO);
        return ResponseEntity.ok(locationDTOs);
    }
}
