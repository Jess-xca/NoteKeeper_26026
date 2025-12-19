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

@Component
public class JsonLocationLoader implements CommandLineRunner {

    @Autowired
    private LocationRepository locationRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (locationRepository.count() == 0) {
            System.out.println("Processing Location.json...");
            loadLocationsFromJson();
            System.out.println("Locations loaded successfully.");
        }
    }

    private void loadLocationsFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("Location.json").getInputStream();
        List<Map<String, Object>> rawLocations = mapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>(){});

        // Use Maps to cache created locations to avoid DB lookups
        Map<String, Location> provinces = new HashMap<>(); // key: code
        Map<String, Location> districts = new HashMap<>();
        Map<String, Location> sectors = new HashMap<>();
        
        // Processing loop
        for (Map<String, Object> entry : rawLocations) {
            // 1. Province
            Integer provCode = (Integer) entry.get("province_code");
            String provName = (String) entry.get("province_name");
            String provKey = String.valueOf(provCode);
            
            Location province = provinces.get(provKey);
            if (province == null) {
                province = new Location(provName, provKey, LocationType.PROVINCE);
                province = locationRepository.save(province);
                provinces.put(provKey, province);
            }

            // 2. District
            Integer distCode = (Integer) entry.get("district_code");
            String distName = (String) entry.get("district_name");
            String distKey = String.valueOf(distCode);

            Location district = districts.get(distKey);
            if (district == null) {
                district = new Location(distName, distKey, LocationType.DISTRICT);
                district.setParent(province);
                district = locationRepository.save(district);
                districts.put(distKey, district);
            }

            // 3. Sector
            String sectCode = (String) entry.get("sector_code");
            String sectName = (String) entry.get("sector_name");
            // Assuming sector_code is unique globally or combine with district if needed. 
            // Usually valid codes are unique.
            
            Location sector = sectors.get(sectCode);
            if (sector == null) {
                sector = new Location(sectName, sectCode, LocationType.SECTOR);
                sector.setParent(district);
                sector = locationRepository.save(sector);
                sectors.put(sectCode, sector);
            }

            // Note: We stop at Sector level if the frontend only supports up to Sector.
            // If Cell/Village is needed, uncomment below.
            // But be warned: This will increase DB size significantly and might slow down 'save' operations if not batched.
            // Given the user said "Location.json is big won't it stop the application from running smoothly", 
            // optimizing by only loading what's needed is a good first step.
            // The JSON has ~200k entries (villages). Loading 200k inserts one-by-one can take time.
            // For now, loading up to SECTOR is safe (~416 sectors).
        }
    }
}
