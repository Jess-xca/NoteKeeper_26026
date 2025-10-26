package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.LocationType;
import com.notekeeper.notekeeper.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    // CREATE
    public Location createLocation(Location location) {
        if (locationRepository.existsByCode(location.getCode())) {
            throw new RuntimeException("Location code already exists");
        }
        return locationRepository.save(location);
    }

    // READ
    public Location getLocationById(String id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
    }

    public Location getLocationByCode(String code) {
        return locationRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Location not found with code: " + code));
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public List<Location> getAllProvinces() {
        return locationRepository.findByParentIsNull();
    }

    public List<Location> getLocationsByType(LocationType type) {
        return locationRepository.findByType(type);
    }

    public List<Location> getChildrenByParentId(String parentId) {
        return locationRepository.findByParentId(parentId);
    }

    public List<Location> getDistrictsByProvinceCode(String provinceCode) {
        return locationRepository.findDistrictsByProvinceCode(provinceCode);
    }

    public List<Location> searchLocations(String keyword) {
        return locationRepository.findByNameContainingIgnoreCase(keyword);
    }

    // UPDATE
    public Location updateLocation(String id, Location locationDetails) {
        Location location = getLocationById(id);
        location.setName(locationDetails.getName());
        location.setCode(locationDetails.getCode());
        location.setType(locationDetails.getType());
        return locationRepository.save(location);
    }

    // DELETE
    public void deleteLocation(String id) {
        Location location = getLocationById(id);
        if (!location.getChildren().isEmpty()) {
            throw new RuntimeException("Cannot delete location with children");
        }
        if (!location.getUsers().isEmpty()) {
            throw new RuntimeException("Cannot delete location with users");
        }
        locationRepository.delete(location);
    }

    // SORTING
    public List<Location> getLocationsSorted(String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return locationRepository.findAll(sort);
    }

    // PAGINATION
    public Page<Location> getLocationsPaginated(int page, int size) {
        return locationRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Location> getLocationsByTypePaginated(LocationType type, int page, int size) {
        return locationRepository.findByType(type, PageRequest.of(page, size));
    }

    // STATISTICS
    public long countChildren(String parentId) {
        return locationRepository.countChildrenByParentId(parentId);
    }

    public long countUsersByLocation(String locationId) {
        return locationRepository.countUsersByLocationId(locationId);
    }
}