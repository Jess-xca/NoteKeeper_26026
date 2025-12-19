# Notekeeper Frontend Enhancement Implementation Plan

## Overview

This plan addresses comprehensive enhancements to the Notekeeper frontend application to fix critical bugs, complete missing features, and satisfy all academic project requirements. The backend already has most necessary entities and endpoints, so the focus is primarily on frontend development.

## User Review Required

> [!IMPORTANT]
> **Google Sign-In Configuration Needed**
> - Google OAuth requires a Google Cloud Console project with client ID and secret
> - Please confirm if you already have a Google Cloud project set up or if I should create one with mock credentials for now
> - If yes, please provide the client ID (can be added later to .env file)

> [!IMPORTANT]
> **Email Service for Password Reset**
> - Password reset requires email service configuration (SMTP)
> - Should I configure using Gmail SMTP or another provider?
> - Credentials will need to be added to backend application.properties

> [!WARNING]
> **Registration Issue Investigation**
> - Need to test registration endpoint to identify the exact issue
> - Will add verbose error logging to help debug

---

## Proposed Changes

### Frontend Components

#### [MODIFY] [endpoints.js](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/api/endpoints.js)
- Add notification API endpoints (mark as read, mark as unread, get notifications)
- Add location hierarchy endpoints (get children by parent)
- Add archive-related page endpoints
- Add global search endpoint
- Add password reset and 2FA endpoints
- Add Google OAuth endpoint

---

### Notifications System

#### [MODIFY] [Navbar.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/components/layout/Navbar.jsx)
- Replace hardcoded notification UI with dynamic data
- Fetch notifications from API for current user
- Add mark as read (✓) and delete (✗) icons to each notification
- Display unread count badge
- Add "Mark all as read" functionality
- Show notification time in human-readable format

#### [NEW] [Notifications.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/Notifications.jsx)
- Create dedicated notifications page
- Display all notifications with filtering (All, Unread, Read)
- Implement bulk actions
- Add pagination for notifications list

---

### Tags System

#### [NEW] [TagsList.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/tags/TagsList.jsx)
- Create tags management page
- Display all user tags with usage count
- Add create/edit/delete tag functionality
- Implement tag search and filtering

#### [MODIFY] [PageEditor.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/pages/PageEditor.jsx#L9-L287)
- Add tags multi-select dropdown
- Allow adding/removing tags from pages
- Display selected tags as chips

---

### Cover Images Enhancement

#### [NEW] Default Cover Images
- Add 4 default cover images (gradients and patterns) to public/covers

#### [MODIFY] [PageEditor.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/pages/PageEditor.jsx#L180-L252)
- Add cover image selector with default options
- Display image gallery for selection
- Keep external URL input as option
- Fix Pinterest/external URL image loading (add proxy handling if needed)
- Add error handling for failed image loads

---

### Profile Settings

#### [MODIFY] [Profile.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/settings/Profile.jsx)
- Complete the Profile Settings page (currently minimal)
- Add user information update form
- Add password change section  
- Add profile picture upload functionality
- Add theme preferences
- Add notification preferences
- Display user statistics

---

### Dashboard Activity Overview

#### [MODIFY] [Dashboard.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/Dashboard.jsx#L380-L405)
- Install chart library (recharts)
- Replace placeholder activity section with actual chart
- Create line/bar chart showing page creation over time
- Add date range selector (week, month, year)
- Fetch activity data from backend

#### Backend Activity Endpoint
- Create `/api/pages/activity/{userId}` endpoint returning page stats by date

---

### Footer Enhancements

#### [MODIFY] [Footer.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/components/layout/Footer.jsx)
- Add social media icons (Twitter, LinkedIn, Facebook, Instagram)
- Ensure all links use proper routing
- Add hover effects for social icons

#### [NEW] Static Pages
- Create About, Help, Privacy, Terms pages
- Add routes for all static pages in App.jsx

---

### Markdown Rendering

#### [MODIFY] package.json
- Add `react-markdown`, `react-syntax-highlighter`, `remark-gfm` dependencies

#### [MODIFY] [PageEditor.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/pages/PageEditor.jsx#L254-L279)
- Add split-pane view toggle (Edit / Preview / Split)
- Render markdown preview using react-markdown
- Add syntax highlighting for code blocks
- Support common markdown features

#### [NEW] PageView.jsx
- Create dedicated page viewer for read-only markdown rendering
- Add print and export functionality

---

### Archive Functionality

#### [NEW] Archive.jsx
- Create archive page listing all archived pages
- Add unarchive and permanent delete functionality
- Add search within archived pages

#### [MODIFY] PagesList.jsx & Sidebar.jsx
- Add "Archive" button and navigation link

---

### Rwanda Location Hierarchy

#### [MODIFY] [Register.jsx](file:///c:/Users/user/Documents/AUCA/Sem%207/Web%20Tec/notekeeper-frontend/src/pages/Register.jsx#L304-L321)
- Replace single location dropdown with cascading selects
- Province → District → Sector dropdowns
- Implement location filtering (Kigali shows only Gasabo, Kicukiro, Nyarugenge)
- Each selection triggers fetch for child locations

#### [NEW] LocationSelector.jsx
- Reusable cascading location selector component

---

### Registration Debug & Fix

#### [MODIFY] Register.jsx
- Add detailed console logging for debugging
- Add better error message display
- Validate all fields before submission

#### Backend Investigation
- Check UserController registration endpoint
- Verify password encoding and database constraints

---

### Google Sign-In

#### [MODIFY] Login.jsx & package.json
- Add `@react-oauth/google` dependency
- Add Google Sign-In button
- Implement OAuth callback handling

#### Backend Google Auth
- Create `/api/auth/google` endpoint to verify Google token

---

### Pagination, Search, 2FA, Roles

- Implement pagination on all list pages
- Add global search functionality (Ctrl+K)
- Create 2FA setup and verification pages
- Implement role-based routing and UI controls
- Create admin dashboard

---

## Verification Plan

### Automated Tests

1. **Backend Tests**
   ```bash
   cd notekeeper
   ./mvnw test
   ```

2. **Frontend Tests**
   ```bash
   cd notekeeper-frontend
   npm test
   ```

### Manual Verification

1. **Start Servers**
   - Backend: `cd notekeeper && ./mvnw spring-boot:run` (port 8080)
   - Frontend: `cd notekeeper-frontend && npm start` (port 3000)

2. **Test Registration with Rwanda Location**
   - Navigate to /register
   - Select Province: Kigali → District should show only Gasabo, Kicukiro, Nyarugenge
   - Select District: Gasabo → Sector dropdown populates
   - Complete registration and verify success

3. **Test Notifications**
   - Create a page → notification appears
   - Click ✓ to mark as read
   - Click ✗ to delete notification

4. **Test Tags, Cover Images, Markdown, Archive**
   - Create tags and assign to pages
   - Select default cover images and test Pinterest URL
   - Write markdown and toggle preview
   - Archive and unarchive pages

5. **Test Dashboard, Search, Pagination**
   - Verify activity chart displays
   - Use global search (Ctrl+K)
   - Verify pagination works on all lists

6. **Test Google Sign-In, 2FA, Roles**
   - Sign in with Google
   - Enable 2FA and verify on next login
   - Test admin role access

### Browser Testing
- Test in Chrome, Firefox, Edge
- Verify responsive design on mobile

---

## Dependencies to Install

### Frontend
```json
{
  "react-markdown": "^9.0.0",
  "remark-gfm": "^4.0.0",
  "react-syntax-highlighter": "^15.5.0",
  "@react-oauth/google": "^0.12.0",
  "recharts": "^2.10.0"
}
```

### Backend
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

---

## Implementation Order

1. **Phase 1** - Critical Fixes: Registration, Rwanda location, notifications, cover images
2. **Phase 2** - Core Features: Markdown, archive, profile, tags
3. **Phase 3** - Academic Requirements: Pagination, global search, table filtering, dashboard activity
4. **Phase 4** - Advanced: 2FA, Google Sign-In, email reset, role-based access
5. **Phase 5** - Polish: Static pages, social icons, admin dashboard
