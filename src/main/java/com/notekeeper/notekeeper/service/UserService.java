// package com.notekeeper.notekeeper.service;

// import com.notekeeper.notekeeper.model.User;
// import com.notekeeper.notekeeper.model.UserProfile;
// import com.notekeeper.notekeeper.model.Workspace;
// import com.notekeeper.notekeeper.repository.UserRepository;
// import com.notekeeper.notekeeper.repository.WorkspaceRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Sort;
// import org.springframework.stereotype.Service;

// import java.util.List;

// @Service
// public class UserService {

// @Autowired
// private UserRepository userRepository;

// @Autowired
// private WorkspaceRepository workspaceRepository;

// // CREATE
// public User createUser(User user) {
// if (userRepository.existsByUsername(user.getUsername())) {
// throw new RuntimeException("Username already exists");
// }
// if (userRepository.existsByEmail(user.getEmail())) {
// throw new RuntimeException("Email already exists");
// }

// User savedUser = userRepository.save(user);

// // Create default profile
// UserProfile profile = new UserProfile();
// profile.setUser(savedUser);
// savedUser.setProfile(profile);

// // Create default Inbox workspace
// Workspace inbox = new Workspace();
// inbox.setName("Inbox");
// inbox.setDescription("Quick capture notes");
// inbox.setIcon("📥");
// inbox.setOwner(savedUser);
// inbox.setIsDefault(true);
// workspaceRepository.save(inbox);

// return userRepository.save(savedUser);
// }

// // READ
// public User getUserById(String id) {
// return userRepository.findById(id)
// .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
// }

// public User getUserByUsername(String username) {
// return userRepository.findByUsername(username)
// .orElseThrow(() -> new RuntimeException("User not found"));
// }

// public List<User> getAllUsers() {
// return userRepository.findAll();
// }

// public List<User> getUsersByLocationName(String locationName) {
// return userRepository.findByLocationName(locationName);
// }

// public List<User> getUsersByProvinceCode(String provinceCode) {
// return userRepository.findByLocationCodePrefix(provinceCode);
// }

// public List<User> searchUsers(String keyword) {
// return userRepository.searchUsers(keyword);
// }

// // UPDATE
// public User updateUser(String id, User userDetails) {
// User user = getUserById(id);
// user.setFirstName(userDetails.getFirstName());
// user.setLastName(userDetails.getLastName());
// user.setEmail(userDetails.getEmail());
// user.setLocation(userDetails.getLocation());
// return userRepository.save(user);
// }

// // DELETE
// public void deleteUser(String id) {
// User user = getUserById(id);
// userRepository.delete(user);
// }

// // SORTING
// public List<User> getUsersSorted(String sortBy, String direction) {
// Sort sort = direction.equalsIgnoreCase("asc")
// ? Sort.by(sortBy).ascending()
// : Sort.by(sortBy).descending();
// return userRepository.findAll(sort);
// }

// // PAGINATION
// public Page<User> getUsersPaginated(int page, int size) {
// return userRepository.findAll(PageRequest.of(page, size));
// }

// public Page<User> getUsersByLocationPaginated(String locationName, int page,
// int size) {
// return userRepository.findByLocationNamePaginated(locationName,
// PageRequest.of(page, size));
// }

// // STATISTICS
// public long countUsers() {
// return userRepository.count();
// }

// public boolean isUsernameAvailable(String username) {
// return !userRepository.existsByUsername(username);
// }
// }