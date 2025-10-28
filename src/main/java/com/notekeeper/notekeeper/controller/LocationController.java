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
    public ResponseEntity<?> createLocation(@RequestBody Location location) {
        try {
            String result = locationService.createLocation(location);

            if (result.equals("code exists")) {
                return new ResponseEntity<>("Location code already exists", HttpStatus.CONFLICT);
            } else {
                Location createdLocation = locationService.getLocationById(result);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(dtoMapper.toLocationDTO(createdLocation));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create location: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/with-parent")
    public ResponseEntity<?> createLocationWithParent(
            @RequestParam(required = false) String parentCode,
            @RequestBody Location childLocation) {
        try {
            String result = locationService.createLocationWithParent(parentCode, childLocation);

            if (result.equals("code exists")) {
                return new ResponseEntity<>("Location code already exists", HttpStatus.CONFLICT);
            } else if (result.equals("parent not found")) {
                return new ResponseEntity<>("Parent location not found", HttpStatus.NOT_FOUND);
            } else {
                Location createdLocation = locationService.getLocationById(result);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(dtoMapper.toLocationDTO(createdLocation));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create location: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLocations() {
        try {
            List<Location> locations = locationService.getAllLocations();
            List<LocationDTO> locationDTOs = locations.stream()
                    .map(dtoMapper::toLocationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(locationDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch locations: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLocationById(@PathVariable String id) {
        try {
            Location location = locationService.getLocationById(id);
            if (location == null) {
                return new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toLocationDTO(location));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch location: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getLocationByCode(@PathVariable String code) {
        try {
            Location location = locationService.getLocationByCode(code);
            if (location == null) {
                return new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toLocationDTO(location));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch location: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/provinces")
    public ResponseEntity<?> getAllProvinces() {
        try {
            List<Location> provinces = locationService.getAllProvinces();
            List<LocationDTO> locationDTOs = provinces.stream()
                    .map(dtoMapper::toLocationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(locationDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch provinces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getLocationsByType(@PathVariable LocationType type) {
        try {
            List<Location> locations = locationService.getLocationsByType(type);
            List<LocationDTO> locationDTOs = locations.stream()
                    .map(dtoMapper::toLocationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(locationDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch locations: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<?> getChildren(@PathVariable String parentId) {
        try {
            List<Location> children = locationService.getChildrenByParentId(parentId);
            List<LocationDTO> locationDTOs = children.stream()
                    .map(dtoMapper::toLocationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(locationDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch children: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchLocations(@RequestParam String keyword) {
        try {
            List<Location> locations = locationService.searchLocations(keyword);
            List<LocationDTO> locationDTOs = locations.stream()
                    .map(dtoMapper::toLocationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(locationDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to search locations: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLocation(@PathVariable String id, @RequestBody Location location) {
        try {
            String result = locationService.updateLocation(id, location);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND);
            } else {
                Location updatedLocation = locationService.getLocationById(id);
                return ResponseEntity.ok(dtoMapper.toLocationDTO(updatedLocation));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update location: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable String id) {
        try {
            String result = locationService.deleteLocation(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND);
            } else if (result.equals("has children")) {
                return new ResponseEntity<>("Cannot delete location with children", HttpStatus.CONFLICT);
            } else if (result.equals("has users")) {
                return new ResponseEntity<>("Cannot delete location with users", HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>("Location deleted successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete location: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
