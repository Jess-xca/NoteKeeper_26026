package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.UserDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            String result = userService.createUser(user);
            
            if (result.equals("username exists")) {
                return new ResponseEntity<>("Username already taken", HttpStatus.CONFLICT);
            } else if (result.equals("email exists")) {
                return new ResponseEntity<>("Email already registered", HttpStatus.CONFLICT);
            } else {
                User createdUser = userService.getUserById(result);
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(dtoMapper.toUserDTO(createdUser));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create user: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream()
                    .map(dtoMapper::toUserDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch users: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toUserDTO(user));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch user: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toUserDTO(user));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch user: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-location")
    public ResponseEntity<?> getUsersByLocation(@RequestParam String locationName) {
        try {
            List<User> users = userService.getUsersByLocationName(locationName);
            List<UserDTO> userDTOs = users.stream()
                    .map(dtoMapper::toUserDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch users by location: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-province-code")
    public ResponseEntity<?> getUsersByProvinceCode(@RequestParam String code) {
        try {
            List<User> users = userService.getUsersByProvinceCode(code);
            List<UserDTO> userDTOs = users.stream()
                    .map(dtoMapper::toUserDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch users: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword);
            List<UserDTO> userDTOs = users.stream()
                    .map(dtoMapper::toUserDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to search users: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User user) {
        try {
            String result = userService.updateUser(id, user);
            
            if (result.equals("not found")) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            } else if (result.equals("email exists")) {
                return new ResponseEntity<>("Email already taken by another user", HttpStatus.CONFLICT);
            } else {
                User updatedUser = userService.getUserById(id);
                return ResponseEntity.ok(dtoMapper.toUserDTO(updatedUser));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update user: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            String result = userService.deleteUser(id);
            
            if (result.equals("not found")) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            } else if (result.equals("has pages")) {
                return new ResponseEntity<>("Cannot delete user with existing pages", HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete user: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/sorted")
    public ResponseEntity<?> getUsersSorted(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            List<User> users = userService.getUsersSorted(sortBy, direction);
            List<UserDTO> userDTOs = users.stream()
                    .map(dtoMapper::toUserDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to sort users: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<User> users = userService.getUsersPaginated(page, size);
            Page<UserDTO> userDTOs = users.map(dtoMapper::toUserDTO);
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch paginated users: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-location/paginated")
    public ResponseEntity<?> getUsersByLocationPaginated(
            @RequestParam String locationName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<User> users = userService.getUsersByLocationPaginated(locationName, page, size);
            Page<UserDTO> userDTOs = users.map(dtoMapper::toUserDTO);
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch users: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countUsers() {
        try {
            long count = userService.countUsers();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count users: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean available = userService.isUsernameAvailable(username);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to check username: " + e.getMessage(), 
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}