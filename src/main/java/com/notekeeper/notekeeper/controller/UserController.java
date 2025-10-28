package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/by-location")
    public ResponseEntity<List<User>> getUsersByLocation(@RequestParam String locationName) {
        List<User> users = userService.getUsersByLocationName(locationName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-province-code")
    public ResponseEntity<List<User>> getUsersByProvinceCode(@RequestParam String code) {
        List<User> users = userService.getUsersByProvinceCode(code);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<User>> getUsersSorted(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        List<User> users = userService.getUsersSorted(sortBy, direction);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<User>> getUsersPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.getUsersPaginated(page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-location/paginated")
    public ResponseEntity<Page<User>> getUsersByLocationPaginated(
            @RequestParam String locationName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.getUsersByLocationPaginated(locationName, page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        long count = userService.countUsers();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(available);
    }
}