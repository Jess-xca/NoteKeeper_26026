-- Cascading Delete Script for NoteKeeper Users
-- Replace 'alice@gmail.com' with the email of the user you want to delete.

SET @email_to_delete = 'alice@gmail.com';

-- 1. Get User ID
SELECT @user_id := id FROM users WHERE email = @email_to_delete;

-- If user exists, proceed with deletion of related data
-- (Note: In a safe production environment, you would use transactions)

-- 2. Delete Page Tags (for pages owned by user)
DELETE FROM page_tags WHERE page_id IN (SELECT id FROM pages WHERE user_id = @user_id);

-- 3. Delete Pages (owned by user)
DELETE FROM pages WHERE user_id = @user_id;

-- 4. Delete Workspace Memberships (where user is a member)
DELETE FROM workspace_members WHERE user_id = @user_id;

-- 5. Delete Workspace Memberships (for workspaces owned by user)
DELETE FROM workspace_members WHERE workspace_id IN (SELECT id FROM workspaces WHERE owner_id = @user_id);

-- 6. Delete Pages in Workspaces owned by user (if any remained that weren't owned by user directly, though usually they are)
DELETE FROM pages WHERE workspace_id IN (SELECT id FROM workspaces WHERE owner_id = @user_id);

-- 7. Delete Workspaces (owned by user)
DELETE FROM workspaces WHERE owner_id = @user_id;

-- 8. Delete User Profile
DELETE FROM user_profiles WHERE user_id = @user_id;

-- 9. Finally, Delete the User
DELETE FROM users WHERE id = @user_id;

-- Verification
SELECT * FROM users WHERE email = @email_to_delete;
