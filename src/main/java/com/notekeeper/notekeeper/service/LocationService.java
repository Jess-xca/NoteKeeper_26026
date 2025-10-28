package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.LocationType;
import com.notekeeper.notekeeper.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    // CREATE
    public String createLocation(Location location) {
        if (locationRepository.existsByCode(location.getCode())) {
            return "code exists";
        }
        Location saved = locationRepository.save(location);
        return saved.getId();
    }

    public String createLocationWithParent(String parentCode, Location childLocation) {
        if (parentCode != null) {
            Optional<Location> parent = locationRepository.findByCode(parentCode);

            if (!parent.isPresent()) {
                return "parent not found";
            }

            childLocation.setParent(parent.get());
        }

        if (locationRepository.existsByCode(childLocation.getCode())) {
            return "code exists";
        }

        Location saved = locationRepository.save(childLocation);
        return saved.getId();
    }

    // READ
    public Location getLocationById(String id) {
        Optional<Location> location = locationRepository.findById(id);
        return location.orElse(null);
    }

    public Location getLocationByCode(String code) {
        Optional<Location> location = locationRepository.findByCode(code);
        return location.orElse(null);
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
    public String updateLocation(String id, Location locationDetails) {
        Optional<Location> locationOpt = locationRepository.findById(id);

        if (!locationOpt.isPresent()) {
            return "not found";
        }

        Location location = locationOpt.get();
        location.setName(locationDetails.getName());
        location.setCode(locationDetails.getCode());
        location.setType(locationDetails.getType());
        locationRepository.save(location);
        return "success";
    }

    // DELETE
    public String deleteLocation(String id) {
        Optional<Location> locationOpt = locationRepository.findById(id);

        if (!locationOpt.isPresent()) {
            return "not found";
        }

        Location location = locationOpt.get();

        if (!location.getChildren().isEmpty()) {
            return "has children";
        }
        if (!location.getUsers().isEmpty()) {
            return "has users";
        }

        locationRepository.delete(location);
        return "success";
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