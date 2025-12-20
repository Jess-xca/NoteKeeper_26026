package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.UserDTO;
import com.notekeeper.notekeeper.dto.ChangePasswordRequest;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserDTO created = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsersDTO();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        UserDTO user = userService.getUserByIdDTO(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsernameDTO(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/by-location")
    public ResponseEntity<List<UserDTO>> getUsersByLocation(@RequestParam String locationName) {
        List<UserDTO> users = userService.getUsersByLocationDTO(locationName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-province-code")
    public ResponseEntity<List<UserDTO>> getUsersByProvinceCode(@RequestParam String code) {
        List<UserDTO> users = userService.getUsersByProvinceCodeDTO(code);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String keyword) {
        List<UserDTO> users = userService.searchUsersDTO(keyword);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updated);
    }

    // Admin-specific update endpoint (selective fields)
    @PutMapping("/admin/{id}")
    public ResponseEntity<UserDTO> updateUserAdmin(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUserAdmin(id, user);
        return ResponseEntity.ok(dtoMapper.toUserDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id) {
        userService.deleteUserDTO(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<UserDTO>> getUsersPaginated(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDTO> users = userService.getUsersPaginatedDTO(page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<UserDTO>> getUsersSorted(@RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        List<UserDTO> users = userService.getUsersSorted(sortBy, direction).stream()
                .map(dtoMapper::toUserDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(userService.countUsers());
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(userService.isUsernameAvailable(username));
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable String id,
            @RequestBody ChangePasswordRequest request) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Current password is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("New password is required");
        }

        userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}