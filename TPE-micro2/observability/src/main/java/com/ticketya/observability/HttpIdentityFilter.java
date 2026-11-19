package com.ticketya.observability;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Se agrega después de BearerTokenAuthenticationFilter y antes de autorización. */
public final class HttpIdentityFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String user = HttpEvents.user(SecurityContextHolder.getContext().getAuthentication());
        request.setAttribute(HttpEvents.USER_ATTRIBUTE, user);
        String previous = MDC.get("usuario");
        MDC.put("usuario", user);
        try { chain.doFilter(request, response); }
        finally { if (previous == null) MDC.remove("usuario"); else MDC.put("usuario", previous); }
    }
}
