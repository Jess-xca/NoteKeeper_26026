# Implementation Plan: Advanced Features (Phases 5-8)

## Overview

This plan covers implementing 4 major feature sets:
1. **Phase 5:** Role-Based Authentication & Admin Dashboard
2. **Phase 6:** File Attachments & Uploads
3. **Phase 7:** Export to PDF/Markdown
4. **Phase 8:** Real-time Collaboration (WebSocket)

**Total Estimated Time:** 12-16 hours

---

## Phase 5: Role-Based Authentication & Admin Dashboard

### Goals
- Implement 2+ user roles (ADMIN, USER, EDITOR)
- Create Admin Dashboard with user management
- Role-based UI rendering
- Protected admin routes

### Backend Changes

#### 1. Update User Model (Already Has Role Field ✅)
No changes needed - `User.role` already exists

#### 2. Add Admin Endpoints to UserController
```java
// Add these endpoints:
@GetMapping("/admin/all-users") - Admin only: Get all users
@PutMapping("/admin/{id}/role") - Admin only: Change user role
@DeleteMapping("/admin/{id}") - Admin only: Delete user
@GetMapping("/admin/stats") - Admin only: Get user statistics
```

#### 3. Create Role Enum (Optional)
```java
public enum UserRole {
    ADMIN, USER, EDITOR
}
```

### Frontend Changes

#### 1. Add Role Check Utility
**File:** `src/utils/roleUtils.js`
- `isAdmin(user)` - Check if user is admin
- `hasRole(user, role)` - Check specific role
- `canAccess(user, requiredRole)` - Permission check

#### 2. Create Admin Dashboard Page
**File:** `src/pages/admin/AdminDashboard.jsx`
- User statistics (total users, new users this week)
- Role distribution chart
- Recent registrations
- Quick admin actions

#### 3. Create User Management Page
**File:** `src/pages/admin/UserManagement.jsx`
- Table of all users
- Search and filter users
- Change user roles
- Delete users
- View user details

#### 4. Add Protected Admin Route
**File:** `src/components/routes/AdminRoute.jsx`
```jsx
const AdminRoute = ({ children }) => {
  const { user } = useAuth();
  if (!user || user.role !== 'ADMIN') {
    return <Navigate to="/dashboard" />;
  }
  return children;
};
```

#### 5. Update Sidebar Navigation
Add admin menu items (visible only to admins):
- Admin Dashboard
- User Management
- System Settings

#### 6. Role-Based UI Rendering
Update existing pages to show/hide features based on role:
- Delete buttons (admins can delete any page)
- User profile editing (admins can edit any user)
- Workspace permissions

---

## Phase 6: File Attachments & Uploads

### Goals
- Upload files to pages
- Support images, PDFs, documents
- File preview functionality
- Download attachments

### Backend Changes

#### 1. Create Attachment Entity
```java
@Entity
public class Attachment {
    @Id private String id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String filePath; // or S3 URL
    private LocalDateTime uploadedAt;
    
    @ManyToOne
    private Page page;
    
    @ManyToOne
    private User uploadedBy;
}
```

#### 2. Create AttachmentController
```java
@PostMapping("/api/attachments/upload")
@GetMapping("/api/attachments/{id}")
@GetMapping("/api/attachments/page/{pageId}")
@DeleteMapping("/api/attachments/{id}")
@GetMapping("/api/attachments/{id}/download")
```

#### 3. File Storage Configuration
**Option A:** Local storage (simple)
```properties
file.upload.dir=/uploads
file.max.size=10MB
```

**Option B:** Cloud storage (recommended for production)
- AWS S3
- Cloudinary
- Azure Blob Storage

#### 4. Add File Validation
- Max file size: 10MB
- Allowed types: images, PDFs, docs, text files
- Virus scanning (optional)

### Frontend Changes

#### 1. Create FileUpload Component
**File:** `src/components/common/FileUpload.jsx`
- Drag & drop area
- File selection
- Upload progress bar
- Preview thumbnails

#### 2. Update PageEditor
Add attachment section:
- Upload button
- List of attachments
- Preview modal
- Delete attachment

#### 3. Create Attachment Display Component
**File:** `src/components/pages/AttachmentList.jsx`
- Display file icon based on type
- Show file name, size, date
- Download button
- Preview for images/PDFs

#### 4. Add File Preview Modal
**File:** `src/components/common/FilePreview.jsx`
- Image viewer
- PDF viewer (using react-pdf)
- Document viewer
- Download option

---

## Phase 7: Export to PDF/Markdown

### Goals
- Export individual pages to PDF
- Export pages to Markdown format
- Export workspace (all pages)
- Download exported files

### Backend Changes

#### 1. Add Export Dependencies
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>
```

#### 2. Create ExportController
```java
@GetMapping("/api/export/page/{id}/pdf")
@GetMapping("/api/export/page/{id}/markdown")
@GetMapping("/api/export/workspace/{id}/zip")
```

#### 3. Create Export Service
**File:** `ExportService.java`
- `exportPageToPDF(pageId)` - Generate PDF
- `exportPageToMarkdown(pageId)` - Generate MD file
- `exportWorkspaceToZip(workspaceId)` - Zip all pages

### Frontend Changes

#### 1. Add Export Buttons to PageEditor
```jsx
<button onClick={exportToPDF}>Export PDF</button>
<button onClick={exportToMarkdown}>Export Markdown</button>
```

#### 2. Create Export Utility
**File:** `src/utils/exportUtils.js`
```javascript
exportToPDF(pageId)
exportToMarkdown(pageId)
downloadFile(blob, filename)
```

#### 3. Client-side Export (Alternative)
For Markdown (no backend needed):
```javascript
// Use existing page content
const markdownContent = page.content;
downloadAsFile(markdownContent, `${page.title}.md`);
```

For PDF (using jsPDF):
```javascript
import jsPDF from 'jspdf';
const doc = new jsPDF();
doc.text(page.content, 10, 10);
doc.save(`${page.title}.pdf`);
```

#### 4. Add Export Options Modal
**File:** `src/components/pages/ExportModal.jsx`
- Choose format (PDF, Markdown, HTML)
- Include attachments option
- Template selection

---

## Phase 8: Real-time Collaboration

### Goals
- Multiple users can edit same page simultaneously
- Show active collaborators
- Real-time cursor positions
- Conflict resolution

### Backend Changes

#### 1. Add WebSocket Dependencies
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

#### 2. Create WebSocket Configuration
**File:** `WebSocketConfig.java`
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
```

#### 3. Create Collaboration Controller
```java
@MessageMapping("/page/{pageId}/edit")
@SendTo("/topic/page/{pageId}")
public PageUpdate handlePageUpdate(PageUpdate update) {
    return update;
}

@MessageMapping("/page/{pageId}/cursor")
@SendTo("/topic/page/{pageId}/cursors")
public CursorPosition handleCursorUpdate(CursorPosition cursor) {
    return cursor;
}
```

#### 4. Create Page Lock Mechanism
```java
@Entity
public class PageLock {
    @Id private String id;
    private String pageId;
    private String userId;
    private LocalDateTime acquiredAt;
    private LocalDateTime expiresAt;
}
```

### Frontend Changes

#### 1. Install WebSocket Library
```bash
npm install sockjs-client @stomp/stompjs
```

#### 2. Create WebSocket Service
**File:** `src/services/websocketService.js`
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export class WebSocketService {
    connect(pageId, onMessage)
    sendUpdate(pageId, content)
    sendCursorPosition(pageId, position)
    disconnect()
}
```

#### 3. Update PageEditor for Collaboration
**File:** `src/pages/pages/PageEditor.jsx`
- Connect to WebSocket on mount
- Show active users badge
- Display remote cursors
- Merge remote changes
- Send updates on edit

#### 4. Create Active Users Component
**File:** `src/components/collaboration/ActiveUsers.jsx`
- Display avatars of active users
- Show user count
- Tooltip with user names

#### 5. Create Cursor Component
**File:** `src/components/collaboration/RemoteCursor.jsx`
- Show other users' cursor positions
- Different colors per user
- Username label

#### 6. Add Conflict Resolution
**File:** `src/utils/conflictResolver.js`
- Operational Transformation (OT)
- Last-write-wins strategy
- Merge conflicts gracefully

---

## Database Migrations

### New Tables Required

```sql
-- Attachments
CREATE TABLE attachments (
    id VARCHAR(255) PRIMARY KEY,
    file_name VARCHAR(255),
    file_type VARCHAR(100),
    file_size BIGINT,
    file_path TEXT,
    uploaded_at TIMESTAMP,
    page_id VARCHAR(255),
    uploaded_by VARCHAR(255),
    FOREIGN KEY (page_id) REFERENCES pages(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

-- Page Locks (for collaboration)
CREATE TABLE page_locks (
    id VARCHAR(255) PRIMARY KEY,
    page_id VARCHAR(255),
    user_id VARCHAR(255),
    acquired_at TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (page_id) REFERENCES pages(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Dependencies to Add

### Backend (pom.xml)
```xml
<!-- File uploads -->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.5</version>
</dependency>

<!-- PDF export -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
</dependency>

<!-- WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### Frontend (package.json)
```json
{
  "jspdf": "^2.5.1",
  "html2canvas": "^1.4.1",
  "file-saver": "^2.0.5",
  "sockjs-client": "^1.6.1",
  "@stomp/stompjs": "^7.0.0",
  "react-pdf": "^7.5.1"
}
```

---

## Implementation Order

### Priority 1: Role-Based Auth & Admin Dashboard (2-3 hours)
**Why First:** Foundation for other features, security requirement
1. Add role utils
2. Create AdminRoute component
3. Build AdminDashboard page
4. Build UserManagement page
5. Add admin endpoints
6. Update sidebar

### Priority 2: File Attachments (3-4 hours)
**Why Second:** Enhances core functionality, users expect this
1. Create Attachment entity
2. Create AttachmentController
3. Set up file storage
4. Build FileUpload component
5. Update PageEditor
6. Add file preview

### Priority 3: Export Functionality (2-3 hours)
**Why Third:** Easier to implement, high user value
1. Add export dependencies
2. Create client-side export utils
3. Add export buttons
4. Implement PDF export
5. Implement Markdown export
6. Test downloads

### Priority 4: Real-time Collaboration (4-6 hours)
**Why Last:** Most complex, requires all other features stable
1. Add WebSocket dependencies
2. Configure WebSocket
3. Create collaboration controller
4. Build WebSocket service
5. Update PageEditor
6. Add active users display
7. Handle conflicts

---

## Testing Strategy

### Phase 5 Tests
- [ ] Admin can access admin dashboard
- [ ] Regular user cannot access admin routes
- [ ] Admin can change user roles
- [ ] Admin can delete users
- [ ] User management search works
- [ ] Role-based UI shows/hides correctly

### Phase 6 Tests
- [ ] Upload file to page
- [ ] File appears in attachment list
- [ ] Download file
- [ ] Preview image file
- [ ] Preview PDF file
- [ ] Delete attachment
- [ ] File size validation
- [ ] File type validation

### Phase 7 Tests
- [ ] Export page to PDF
- [ ] Export page to Markdown
- [ ] Downloaded files open correctly
- [ ] Markdown preserves formatting
- [ ] PDF renders correctly
- [ ] Export includes attachments (if selected)

### Phase 8 Tests
- [ ] Two users connect to same page
- [ ] Both users see each other online
- [ ] Edits sync in real-time
- [ ] Cursor positions show
- [ ] No conflicts when editing different sections
- [ ] Conflicts resolved when editing same section
- [ ] Connection handles disconnect/reconnect

---

## Verification Plan

### Automated Tests
```bash
# Backend
mvn test

# Frontend
npm test
```

### Manual Tests
1. Create ADMIN user manually in database
2. Login as admin → verify admin dashboard
3. Login as regular user → verify no admin access
4. Upload file → verify download
5. Export page → verify PDF/Markdown
6. Open same page in 2 browsers → verify real-time sync

---

## Risks & Mitigations

### Risk 1: File Storage Size
**Mitigation:** Set max file size, add storage quotas per user

### Risk 2: WebSocket Performance
**Mitigation:** Throttle updates, use debouncing, limit active editors

### Risk 3: Concurrent Edit Conflicts
**Mitigation:** Implement operational transformation or CRDTs

### Risk 4: Security (File Uploads)
**Mitigation:** Validate file types, scan for malware, limit extensions

---

## Success Criteria

- ✅ At least 2 roles with different permissions
- ✅ Admin can manage all users
- ✅ Files can be uploaded and downloaded
- ✅ Pages can be exported to PDF and Markdown
- ✅ Real-time collaboration works for 2+ users
- ✅ No data loss during concurrent edits
- ✅ All features work with existing functionality

---

**Estimated Total Effort:** 12-16 hours  
**Phases:** 4  
**New Components:** 15+  
**New Backend Endpoints:** 20+  
**New Database Tables:** 2

Ready to implement! 🚀
