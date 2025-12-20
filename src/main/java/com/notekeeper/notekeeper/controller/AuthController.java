System.out.println("📧 2FA PIN for " + user.getEmail());
            System.out.println("🔑 PIN CODE: " + code.getCode());
            System.out.println("========================================");
            
            try {
                emailService.send2FACode(user.getEmail(), code.getCode());
            } catch (Exception e) {
                System.out.println("❌ Failed to send 2FA email: " + e.getMessage());
            }
=======
            try {
                emailService.send2FACode(user.getEmail(), code.getCode());
            } catch (Exception e) {
                // Log error for monitoring but don't expose to user
                System.err.println("Failed to send 2FA email: " + e.getMessage());
            }
