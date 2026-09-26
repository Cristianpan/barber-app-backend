package com.la_navaja.backend.infrastructure.config.security;

import com.la_navaja.backend.domain.models.AuthenticatedUser;
import com.la_navaja.backend.domain.models.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CookieAuthFilter extends OncePerRequestFilter {

  private static final String TOKEN_COOKIE = "token";

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    extractTokenCookie(request)
        .flatMap(jwtService::validateToken)
        .ifPresent(this::populateSecurityContext);

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractTokenCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(c -> TOKEN_COOKIE.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  private void populateSecurityContext(Claims claims) {
    Long userId = claims.get("userId", Long.class);
    String roleStr = claims.get("role", String.class);
    Role role = Role.valueOf(roleStr);

    AuthenticatedUser principal = new AuthenticatedUser(userId, role);
    List<SimpleGrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, authorities);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
