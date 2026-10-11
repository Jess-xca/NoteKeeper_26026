package com.notekeeper.notekeeper.config;

import com.notekeeper.notekeeper.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

/**
 * Many endpoints take the person's id in the URL ("/api/pages/user/{userId}",
 * "/api/notifications/user/{userId}", "/api/workspaces/owner/{ownerId}", ...). Without a check, any
 * signed-in person could put somebody else's id there and read that person's data.
 *
 * This interceptor looks at every request that has a {userId} or {ownerId} path variable and only lets
 * it through when the id is the caller's own, or the caller is an administrator.
 *
 * Not covered on purpose: "/api/workspaces/{workspaceId}/members/{userId}" (there {userId} is the
 * member being managed, and the controller already checks that the caller owns the workspace).
 */
@Configuration
public class OwnDataInterceptor implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                Map<String, String> variables =
                        (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                if (variables == null) {
                    return true;
                }
                String requested = variables.containsKey("userId") ? variables.get("userId") : variables.get("ownerId");
                if (requested == null || request.getRequestURI().contains("/members")) {
                    return true;
                }

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                    boolean admin = authentication.getAuthorities().stream()
                            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
                    if (admin || requested.equals(principal.getId())) {
                        return true;
                    }
                }
                throw new AccessDeniedException("You can only use your own data");
            }
        });
    }
}
