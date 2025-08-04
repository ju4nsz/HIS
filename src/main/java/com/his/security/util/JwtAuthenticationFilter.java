package com.his.security.util;

import com.his.security.exception.ApiException;
import com.his.security.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

// El 'OncePerRequestFilter' asegura que el filtro se ejecute una sola vez por petición.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 2️⃣ Validar el token

        if (jwtUtil.isRefreshToken(token))
            throw new ApiException(Constants.Mensajes.REFRESH_TOKEN_INVALIDO, HttpStatus.UNAUTHORIZED);

        String email = jwtUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            var userDetails = userDetailsService.loadUserByUsername(email);

            if (jwtUtil.isTokenValid(token, userDetails)) {
                Set<SimpleGrantedAuthority> authorities = jwtUtil.extractPermisos(token).stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

}
