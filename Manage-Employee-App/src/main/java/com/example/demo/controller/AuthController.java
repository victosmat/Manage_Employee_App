package com.example.demo.controller;

import com.example.demo.config.MailConfig;
import com.example.demo.component.UserSessionManager;
import com.example.demo.entity.Account;
import com.example.demo.entity.Structure;
import com.example.demo.entity.User;
import com.example.demo.payLoad.dto.UserDTOInSession;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.payLoad.Message;
import com.example.demo.payLoad.dto.*;
import com.example.demo.payLoad.mapper.IMapperRequestToDTO;
import com.example.demo.payLoad.mapper.MapperRequestToDTO;
import com.example.demo.payLoad.request.*;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.config.security.CustomerUserDetails;
import com.example.demo.service.AccessTokenService;
import com.example.demo.service.EmailService;
import com.example.demo.service.StructureService;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/auth")
@Slf4j
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    private final Map<String, ScheduledFuture<?>> anonymousUsers = new ConcurrentHashMap<>();

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private KafkaTemplate<String, UserDTOInSession> kafkaTemplate;
    @Autowired
    private StructureService structureService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MailConfig mailConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MapperRequestToDTO mapperRequestToDTO;
    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserSessionManager sessionManager;

    @Autowired
    private StructureRepository structureRepository;

    //    @GetMapping("/test")
//    public List<User> test() {
//        User user = userRepository.findByEmail("letuanminh1892001@gmail.com");
//        System.out.println(user.getFullName());
//        return userRepository.findAll();
//    }
    @GetMapping("/getUser")
    public List<User> getUser() {
        return userRepository.findAll();
    }

    @GetMapping("/getUserByAddress")
    public List<User> getUserWithAddress() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            Hibernate.initialize(user.getAddress());
        });
        return users;
    }

    @GetMapping("/getUserByID/{id}")
    public Message<UserDTO> getUserByID(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return new Message<>("User not found!", HttpStatus.NOT_FOUND, null);
        return new Message<>("success", HttpStatus.OK, IMapperRequestToDTO.INSTANCE.mapperUserToDTO(user));
    }


    @GetMapping("/dashboard")
    public Message<LoginResponse> dashboard(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().contains(new SimpleGrantedAuthority("ANONYMOUS"))) {
            // tạo token cho người dùng ẩn danh
            log.info("dashboard with anonymous");
            String token = tokenProvider.generateAnonymousToken();
            anonymousUsers.put(token, taskScheduler.schedule(() -> {
                anonymousUsers.get(token).cancel(true);
                anonymousUsers.remove(token);
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                restTemplate.exchange(
                        "http://localhost:8080/api/auth/dashboardOffline",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);

            }, new CronTrigger("0/10 * * * * *")));
            log.info("add token: " + token);
            return new Message<>("dashboard with anonymous", HttpStatus.OK, new LoginResponse(token, null));
        }
        log.info("dashboard with user");
        return new Message<>("dashboard with user", HttpStatus.OK, new LoginResponse(null, null));
    }


    @GetMapping("dashboardOffline")
    public Message<LoginResponse> dashboardOffline(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization").substring(7);
        log.info("remove token: " + token);
        log.info("dashboard offline");
        return new Message<>("dashboard offline", HttpStatus.OK, new LoginResponse(token, null));
    }

    @PostMapping("/login")
    public Message<LoginResponse> authenticationUser(@RequestBody LoginRequest loginRequest) {
        // xác thực từ username và password
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // set  thông tin authentication vào Security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // trả về jwt cho người dùng
        // getPrincipal() trả về đối tượng người dùng đã được xác thực,
        // có thể là một đối tượng của lớp CustomerUserDetails hoặc một lớp tương tự
        String jwtAccessToken = tokenProvider.generateAccessToken((CustomerUserDetails) authentication.getPrincipal());
        String jwtRefreshToken = tokenProvider.generateRefreshToken((CustomerUserDetails) authentication.getPrincipal());
        if (jwtAccessToken == null) return new Message<>("Login fail!", HttpStatus.UNAUTHORIZED, null);
        else {
            Account account = accountRepository.findByUsername(loginRequest.getUsername());
            User user = userRepository.findByAccount(account);
            sessionManager.createSession(user);

            UserDTOInSession userDTOInSession = mapperRequestToDTO.mapUserToUserDTOInSession(user);
            kafkaTemplate.send("add-user-in-session", userDTOInSession)
                    .addCallback(new ListenableFutureCallback<SendResult<String, UserDTOInSession>>() {
                        @Override
                        public void onFailure(Throwable ex) {
                            log.info("Unable to send message=[{}] due to : {}", userDTOInSession.toString(), ex.getMessage());
                        }

                        @Override
                        public void onSuccess(SendResult<String, UserDTOInSession> result) {
                            log.info("Sent message=[{}] with offset=[{}]", userDTOInSession.toString(), result.getRecordMetadata().offset());
                        }
                    });
            return new Message<>("login successful!", HttpStatus.OK, new LoginResponse(jwtAccessToken, jwtRefreshToken));
        }
    }

    @PostMapping("/login/google/{jwtToken}")
    public Message<LoginResponse> loginGoogle(@PathVariable String jwtToken) throws GeneralSecurityException, IOException, MessagingException {
        return userService.google(jwtToken);
    }

    @PostMapping("/register")
    public Message<UserDTO> registryUser(@RequestBody UserRegistryRequest userRegistryRequest) {
        return userService.registryUser(userRegistryRequest);
    }

    @PostMapping("/sign-out")
    public Message<?> logoutUser(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization").substring(7);
        Long userId = tokenProvider.getUserIdFromJWT(token);
        sessionManager.invalidateSession(userId);
        accessTokenService.delete(userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new Message<>("User not found!", HttpStatus.NOT_FOUND, null);
        UserDTOInSession userDTOInSession = mapperRequestToDTO.mapUserToUserDTOInSession(user);

        // kafka
        kafkaTemplate.send("remove-user-in-session", userDTOInSession)
                .addCallback(new ListenableFutureCallback<SendResult<String, UserDTOInSession>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.info("Unable to send message=[{}] due to : {}", userDTOInSession.toString(), ex.getMessage());
                    }

                    @Override
                    public void onSuccess(SendResult<String, UserDTOInSession> result) {
                        log.info("Sent message=[{}] with offset=[{}]", userDTOInSession.toString(), result.getRecordMetadata().offset());
                    }
                });
        log.info("Send message=[{}] to topic=[{}]", userId, "remove-user-in-session");

        return new Message<>("You've been signed out!", HttpStatus.OK, null);
    }

    @PostMapping("/validateToken/{jwtToken}")
    public Message<AuthDTO> validateToken(@PathVariable String jwtToken) {
        return userService.validateToken(jwtToken);
    }

    @PostMapping("/refresh")
    public Message<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshTokenResponse refreshTokenResponse = tokenProvider.refreshAccessToken(refreshTokenRequest);
        if (!refreshTokenResponse.getMessageRefreshToken().equals("VALID"))
            return new Message<>("refresh token fail!", HttpStatus.UNAUTHORIZED, refreshTokenResponse);
        else return new Message<>("refresh token success!", HttpStatus.OK, refreshTokenResponse);
    }

    @PutMapping("/updatePassword")
    public Message<PasswordResponse> updatePassword(@RequestBody PasswordRequest passwordRequest) {
        return userService.updatePassword(passwordRequest);
    }

    @DeleteMapping("/deleteAddress/{addressID}")
    public Message<?> deleteAddress(@PathVariable Long addressID) {
        return userService.deleteAddress(addressID);
    }

    @GetMapping("/getUserByEmail/{email}")
    public Message<UserDTO> getUserByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return new Message<>("User not found!", HttpStatus.NOT_FOUND, null);
        return new Message<>("success", HttpStatus.OK, IMapperRequestToDTO.INSTANCE.mapperUserToDTO(user));
    }
}
