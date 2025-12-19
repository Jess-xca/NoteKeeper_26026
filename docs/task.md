# Notekeeper Frontend Enhancements Task List

## 🔍 Analysis & Planning
- [x] Explore project structure
- [x] Review existing components and pages
- [x] Identify backend API endpoints
- [x] Review current authentication flow
- [x] Create detailed implementation plan

## 🎯 Critical Fixes & Features

### Notifications System
- [x] Add mark as read/unread functionality (API endpoints added)
- [x] Implement tick (✓) and x (✗) icons for read status
- [x] Add proper notification API endpoints
- [x] Update notification UI component
- [x] Persist notification state in backend (already exists)

### Tags System
- [x] Debug tags functionality (created Tags page)
- [x] Ensure tags can be created
- [x] Ensure tags can be assigned to pages
- [x] Implement markdown rendering in PageEditor
- [x] Add syntax highlighting for code blocks
- [x] Add markdown preview toggle (Edit/Preview/Split modes)
- [x] Test all markdown features

### Archive Functionality
- [x] Add archive button to pages
- [x] Create Archive page
- [x] Implement archive/unarchive toggle
- [x] Update routing for archive page
- [x] Test archive filtering

### Rwanda Location Hierarchy
- [x] Research Rwanda administrative divisions
- [x] Create hierarchical location data
- [x] Implement cascading dropdowns (Province → District → Sector)
- [x] Add backend location seeding script
- [x] Update Register component
- [x] Implement location filtering logic

### Registration Fix
- [x] Debug registration endpoint (added logging)
- [/] Check backend validation errors (need to test)
- [/] Test registration flow end-to-end
- [x] Fix any frontend validation issues
- [x] Add better error messaging

### Google Sign-In
- [ ] Install Google OAuth library
- [ ] Set up Google Cloud Console project
- [ ] Add Google Sign-In button to Login page
- [ ] Implement OAuth callback handler
- [ ] Add backend Google authentication endpoint
- [ ] Test Google authentication flow

## 📋 Academic Requirements

### Entities (Minimum 5)
- [x] User
- [x] Page
- [x] Workspace
- [x] Tag
- [x] Location
- [x] UserProfile
- [x] WorkspaceMember
*Already satisfied: 7 entities*

### Pages (Minimum 5, excluding auth pages)
- [x] Dashboard
- [x] Pages List
- [x] Page Editor
- [x] Workspaces List
- [x] Profile Settings (to be completed)
- [ ] Archive Page (to be created)
- [ ] Tags Management (to be created)
- [ ] Notifications Page (to be created)
- [ ] Settings Page (to be created)

### Dashboard Business Summary
- [x] Total pages count
- [x] Total workspaces count
- [x] Favorites count
- [x] Recent pages
- [ ] Activity overview chart (to be implemented)

### Pagination
- [x] Backend pagination endpoints exist
- [ ] Implement frontend pagination UI
- [ ] Add pagination to Pages List
- [ ] Add pagination to Workspaces List
- [ ] Add pagination to Users List (admin)
- [ ] Add pagination to Tags List

### Password Reset via Email
- [x] Frontend forgot password page exists
- [x] Frontend reset password page exists
- [ ] Integrate email service in backend
- [ ] Test password reset flow
- [ ] Add email template

### Two-Factor Authentication
- [ ] Research 2FA implementation (TOTP or Email)
- [ ] Add 2FA setup page
- [ ] Add QR code generation for TOTP
- [ ] Implement 2FA verification on login
- [ ] Add backup codes
- [ ] Test 2FA flow

### Global Search
- [ ] Create global search component
- [ ] Implement search across pages, workspaces, tags
- [ ] Add search results page
- [ ] Add keyboard shortcut (Cmd/Ctrl + K)
- [ ] Debounce search input
- [ ] Highlight search results

### Table Search
- [ ] Add search input to table components
- [ ] Review and ensure consistency
- [ ] Add more reusable components as needed

## 🧪 Testing & Verification
- [ ] Test all new features
- [ ] Verify all requirements are met
- [ ] Cross-browser testing
- [ ] Mobile responsiveness check
- [ ] Performance optimization
