-- Script to delete users by email
-- Usage: Replace 'email_to_delete@example.com' with the actual email

DELETE FROM users WHERE email = 'email_to_delete@example.com';

-- To delete multiple users
DELETE FROM users WHERE email IN ('user1@example.com', 'user2@example.com');

-- To delete users who haven't logged in for a while (if last_login exists)
-- DELETE FROM users WHERE last_login < '2023-01-01';
