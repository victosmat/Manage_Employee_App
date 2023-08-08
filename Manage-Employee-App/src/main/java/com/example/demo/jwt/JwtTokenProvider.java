package com.example.demo.jwt;

import antlr.Token;
import com.example.demo.component.TokenValidationStatus;
import com.example.demo.config.security.CustomerUserDetails;
import com.example.demo.component.AccessToken;
import com.example.demo.entity.User;
import com.example.demo.payLoad.dto.RefreshTokenResponse;
import com.example.demo.payLoad.request.RefreshTokenRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AccessTokenService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Autowired
    private AccessTokenService accessTokenService;
    @Value("${spring.security.jwt_secret}")
    private String JWT_SECRET;

    @Autowired

    private UserRepository userRepository;


    // Tạo ra jwt từ thông tin user
    public String generateRefreshToken(CustomerUserDetails userDetails) {
        Date now = new Date();
        long JWT_EXPIRATION_REFRESH_TOKEN = 864000000L;
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_REFRESH_TOKEN);
        // Tạo chuỗi json web token từ id của user.
        return Jwts.builder()
                .setSubject(Long.toString(userDetails.getUser().getID()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }

    public String generateAccessToken(CustomerUserDetails userDetails) {
        Date now = new Date();
        long JWT_EXPIRATION_ACCESS_TOKEN = 3600000L;
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_ACCESS_TOKEN);
        // Tạo chuỗi json web token từ id của user.
        String accessToken = Jwts.builder()
                .setSubject(Long.toString(userDetails.getUser().getID()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
        Long userID = userDetails.getUser().getID();
        String accessTokenInCache = accessTokenService.findById(userID);
        AccessToken token = new AccessToken(userID, accessToken);
        if (accessTokenInCache == null) accessTokenService.save(token);
        else accessTokenService.update(token);
        return accessToken;
    }

    // Lấy thông tin user ID từ jwt
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();

            Date expirationDate = claims.getExpiration();
            // Kiểm tra xem thời gian hết hạn của token có lớn hơn thời gian hiện tại không.
            return expirationDate.after(new Date());
        } catch (Exception ex) {
            // Xảy ra lỗi khi giải mã token hoặc token đã hết hạn.
            log.error("Expired JWT token");
            return false;
        }
    }

    public TokenValidationStatus validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);
            return TokenValidationStatus.VALID;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return TokenValidationStatus.INVALID_SIGNATURE;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return TokenValidationStatus.INVALID_TOKEN;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return TokenValidationStatus.TOKEN_EXPIRED;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            return TokenValidationStatus.UNSUPPORTED_TOKEN;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return TokenValidationStatus.EMPTY_CLAIMS;
        }
    }

    public boolean IsJwtInCache(String accessToken) {
        Long userID = getUserIdFromJWT(accessToken);
        return accessTokenService.findById(userID).equals(accessToken);
    }

    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest refreshTokenRequest) {
        TokenValidationStatus tokenValidationStatus = validateToken(refreshTokenRequest.getRefreshToken());
        if (tokenValidationStatus != TokenValidationStatus.VALID)
            return new RefreshTokenResponse(null, refreshTokenRequest.getRefreshToken(), tokenValidationStatus.toString());
        Long userID = getUserIdFromJWT(refreshTokenRequest.getRefreshToken());
        User user = userRepository.findById(userID).get();
        CustomerUserDetails userDetails = new CustomerUserDetails(user);
        String accessToken = generateAccessToken(userDetails);
        accessTokenService.update(new AccessToken(userID, accessToken));
        return new RefreshTokenResponse(accessToken, refreshTokenRequest.getRefreshToken(), tokenValidationStatus.toString());
    }

    public String generateAnonymousToken() {
        return Jwts.builder()
                .setSubject("anonymous")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 60000L))
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }
}
