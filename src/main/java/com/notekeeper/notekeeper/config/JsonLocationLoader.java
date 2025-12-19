package com.notekeeper.notekeeper.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.LocationType;
import com.notekeeper.notekeeper.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JsonLocationLoader implements CommandLineRunner {

    @Autowired
    private LocationRepository locationRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        boolean hasData = locationRepository.count() > 0;
        boolean hasVillages = locationRepository.existsByType(LocationType.VILLAGE);

        if (hasData && !hasVillages) {
            System.out.println("⚠️ Detected incomplete location data (missing Villages).");
            System.out.println("♻️ Cleaning up old locations to reload full hierarchy...");
            try {
                // Warning: This deletes all locations. 
                // If users have foreign keys to these locations, this might fail unless ON DELETE SET NULL is configured,
                // or if we break the links first. For now, we attempt to delete.
                locationRepository.deleteAllInBatch(); 
                System.out.println("✅ Old locations deleted.");
                hasData = false;
            } catch (Exception e) {
                System.err.println("❌ Failed to delete old locations: " + e.getMessage());
                System.err.println("👉 Please manually clear the 'locations' table to enable reloading.");
            }
        }

        if (!hasData) {
            System.out.println("Processing Location.json (Full Hierarchy)...");
            long startTime = System.currentTimeMillis();
            loadLocationsFromJson();
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Locations loaded successfully in " + duration + "ms.");
        }
    }

    private void loadLocationsFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("Location.json").getInputStream();
        List<Map<String, Object>> rawLocations = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>(){});

        // 1. Load Provinces
        // Use Maps to prevent duplicates before saving
        Map<String, Location> provinceMap = new HashMap<>();
        
        for (Map<String, Object> entry : rawLocations) {
            String code = String.valueOf(entry.get("province_code"));
            if (!provinceMap.containsKey(code)) {
                String name = (String) entry.get("province_name");
                provinceMap.put(code, new Location(name, code, LocationType.PROVINCE));
            }
        }
        List<Location> savedProvinces = locationRepository.saveAll(provinceMap.values());
        // Re-map by code with the SAVED entities (now having IDs)
        provinceMap.clear();
        for (Location loc : savedProvinces) {
            provinceMap.put(loc.getCode(), loc);
        }
        System.out.println("Loaded " + savedProvinces.size() + " Provinces");

        // 2. Load Districts
        Map<String, Location> districtMap = new HashMap<>();
        for (Map<String, Object> entry : rawLocations) {
            String code = String.valueOf(entry.get("district_code"));
            if (!districtMap.containsKey(code)) {
                String name = (String) entry.get("district_name");
                Location district = new Location(name, code, LocationType.DISTRICT);
                
                // Set Parent
                String parentCode = String.valueOf(entry.get("province_code"));
                district.setParent(provinceMap.get(parentCode));
                
                districtMap.put(code, district);
            }
        }
        List<Location> savedDistricts = locationRepository.saveAll(districtMap.values());
        districtMap.clear();
        for (Location loc : savedDistricts) {
            districtMap.put(loc.getCode(), loc);
        }
        System.out.println("Loaded " + savedDistricts.size() + " Districts");

        // 3. Load Sectors
        Map<String, Location> sectorMap = new HashMap<>();
        for (Map<String, Object> entry : rawLocations) {
            String code = (String) entry.get("sector_code");
            if (!sectorMap.containsKey(code)) {
                String name = (String) entry.get("sector_name");
                Location sector = new Location(name, code, LocationType.SECTOR);
                
                String parentCode = String.valueOf(entry.get("district_code"));
                sector.setParent(districtMap.get(parentCode));
                
                sectorMap.put(code, sector);
            }
        }
        List<Location> savedSectors = locationRepository.saveAll(sectorMap.values());
        // Update map for next level
        sectorMap.clear();
        for (Location loc : savedSectors) {
            sectorMap.put(loc.getCode(), loc);
        }
        System.out.println("Loaded " + savedSectors.size() + " Sectors");
        
        // 4. Load Cells
        Map<String, Location> cellMap = new HashMap<>();
        for (Map<String, Object> entry : rawLocations) {
            Object cellCodeObj = entry.get("cell_code");
            String code = String.valueOf(cellCodeObj); // can be Integer or String in JSON
            
            if (!cellMap.containsKey(code)) {
                String name = (String) entry.get("cell_name");
                Location cell = new Location(name, code, LocationType.CELL);
                
                String parentCode = (String) entry.get("sector_code");
                cell.setParent(sectorMap.get(parentCode));
                
                cellMap.put(code, cell);
            }
        }
        List<Location> savedCells = locationRepository.saveAll(cellMap.values());
        // Update map for next level
        cellMap.clear();
        for (Location loc : savedCells) {
            cellMap.put(loc.getCode(), loc);
        }
        System.out.println("Loaded " + savedCells.size() + " Cells");

        // 5. Load Villages
        // Villages might be numerous (~15k). efficient map is needed.
        Map<String, Location> villageMap = new HashMap<>();
        for (Map<String, Object> entry : rawLocations) {
            Object villageCodeObj = entry.get("village_code");
            String code = String.valueOf(villageCodeObj); 
            
            if (!villageMap.containsKey(code)) {
                String name = (String) entry.get("village_name");
                Location village = new Location(name, code, LocationType.VILLAGE);
                
                String parentCode = String.valueOf(entry.get("cell_code"));
                village.setParent(cellMap.get(parentCode));
                
                villageMap.put(code, village);
            }
        }
        // Save in chunks if needed, but 15k is okay for saveAll usually
        locationRepository.saveAll(villageMap.values());
        System.out.println("Loaded " + villageMap.size() + " Villages");
    }
}
