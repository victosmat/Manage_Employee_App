package com.example.demo.jwt;

import com.example.demo.component.TokenValidationStatus;
import com.example.demo.service.AccessTokenService;
import com.example.demo.service.UserService;
import io.jsonwebtoken.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNullApi;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;


// tạo một Filter extends OncePerRequestFilter.
// KHi có bất cứ request nào tới thì Filter này sẽ được thực thi và kiểm tra xem token có hợp lệ hay không.
// Nếu hợp lệ thì nó sẽ set Authentication trong Security Context để chỉ định rằng User đã được xác thực.
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private UserService customUserDetailsService;
    @Autowired
    private AccessTokenService accessTokenService;

    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String servletPath = request.getServletPath();
            if (servletPath.contains("/auth")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Lấy jwt từ request
            String jwt = getJwtFromRequest(request);
            TokenValidationStatus validationStatus = tokenProvider.validateToken(jwt);

            switch (validationStatus) {
                case VALID -> {
                    Long userId = tokenProvider.getUserIdFromJWT(jwt);
                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                    if (userDetails != null
                            && tokenProvider.IsJwtInCache(jwt)
                            && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
                case INVALID_SIGNATURE -> logger.info("Invalid JWT signature.");
                case INVALID_TOKEN -> logger.info("Invalid JWT token.");
                case TOKEN_EXPIRED -> logger.info("Expired JWT token.");
                case UNSUPPORTED_TOKEN -> logger.info("Unsupported JWT token.");
                case EMPTY_CLAIMS -> logger.info("JWT claims string is empty.");
                default -> logger.info("Unknown error.");
            }
        } catch (Exception ex) {
            log.error("Failed on set user authentication", ex);
        }
        filterChain.doFilter(request, response);
    }


    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Kiểm tra xem header Authorization có chứa thông tin jwt không
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            logger.info(bearerToken);
            return bearerToken.substring(7);
        }
        return null;
    }

}
