package com.example.demo.service;

import com.example.demo.component.AccessToken;
import com.example.demo.component.MailComponent;
import com.example.demo.component.TokenValidationStatus;
import com.example.demo.component.UserSessionManager;
import com.example.demo.entity.*;
import com.example.demo.jwt.JwtTokenProvider;
import com.example.demo.payLoad.mapper.MapperRequestToDTO;
import com.example.demo.payLoad.Message;
import com.example.demo.payLoad.dto.*;
import com.example.demo.payLoad.request.*;
import com.example.demo.repository.*;
import com.example.demo.config.security.CustomerUserDetails;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private MapperRequestToDTO mapperRequestToDTO;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StructureRepository structureRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private CheckTimeRequestRepository checkTimeRequestRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private JobDetailsRepository jobDetailsRepository;

    @Autowired
    private UserSessionManager sessionManager;

    private static final String CLIENT_ID = "603561517127-7lvevfcrrcirk2l8e55b1inaqvg94c4v.apps.googleusercontent.com";

    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new UsernameNotFoundException("User not found with id: " + userId);
        return new CustomerUserDetails(user);
    }

    @Transactional
    public Message<LoginResponse> google(String idTokenString) throws GeneralSecurityException, IOException, MessagingException {
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(CLIENT_ID))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

        // (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            User user = userRepository.findByEmail(email);
            if (user == null) {
                String username = email.split("@")[0];
                Account account = new Account(null, username, null);
                LocalTime checkIn = LocalTime.of(8, 0, 0);
                LocalTime checkOut = LocalTime.of(17, 0, 0);
                Time time = new Time(null, String.valueOf(checkIn), String.valueOf(checkOut));
                User userCheck = new User(name, null, email, account, User.AuthProvider.GOOGLE, null, time);
                Set<Role> roles = new HashSet<>();
                Role role = roleRepository.findByNoteRole(Role.NoteRole.EMPLOYEE);
                roles.add(role);
                userCheck.setRoles(roles);
                user = userRepository.save(userCheck);
                CustomerUserDetails customerUserDetails = new CustomerUserDetails(userCheck);
                String jwtTokenAccess = tokenProvider.generateAccessToken(customerUserDetails);
                String jwtTokenRefresh = tokenProvider.generateRefreshToken(customerUserDetails);
                String dateTimeNow = String.valueOf(LocalDateTime.now());
                UserDTO userDTO = new UserDTO(user.getID(), user.getFullName(), null, user.getEmail(), user.getRoles().toString(), user.getAccount().getUsername());
                String content = "You have successfully registered at " + dateTimeNow + " with ID = " + user.getID() + " and information:" + userDTO;
                MailComponent mailDTO = new MailComponent(email, "successful registration!", content);
                emailService.sendMail(mailDTO);
                sessionManager.createSession(user);
                return new Message<>("successful registration!", HttpStatus.OK, new LoginResponse(jwtTokenAccess, jwtTokenRefresh));
            } else {
                if (user.getAuthProvider().equals(User.AuthProvider.GOOGLE)) {
                    CustomerUserDetails customerUserDetails = new CustomerUserDetails(user);
                    String jwtTokenAccess = tokenProvider.generateAccessToken(customerUserDetails);
                    String jwtTokenRefresh = tokenProvider.generateRefreshToken(customerUserDetails);
                    sessionManager.createSession(user);
                    return new Message<>("Logged in successfully!", HttpStatus.OK, new LoginResponse(jwtTokenAccess, jwtTokenRefresh));
                } else return new Message<>("Email already exists!", HttpStatus.BAD_REQUEST, null);
            }
        } else return new Message<>("Invalid ID token", HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    @Transactional
    public Message<UserDTO> registryUser(UserRegistryRequest userRegistryRequest) {
        Account account = accountRepository.findByUsername(userRegistryRequest.getUsername());
        User user = userRepository.findByEmail(userRegistryRequest.getEmail());
        List<String> messages = new ArrayList<>();
        if (account != null) messages.add("username already exists");
        if (user != null) messages.add("email already exists");
        if (messages.isEmpty()) {
            String fullName = userRegistryRequest.getFullName();
            String email = userRegistryRequest.getEmail();
            String username = userRegistryRequest.getUsername();
            String password = passwordEncoder.encode(userRegistryRequest.getPassword());
            account = new Account(null, username, password);

            LocalTime checkIn = LocalTime.of(8, 0, 0);
            LocalTime checkOut = LocalTime.of(17, 0, 0);
            Time time = new Time(null, String.valueOf(checkIn), String.valueOf(checkOut));
            user = new User(fullName, null, email, account, User.AuthProvider.LOCAL, null, time);
            Set<Role> roles = new HashSet<>();
            Role role = roleRepository.findByNoteRole(Role.NoteRole.EMPLOYEE);
            roles.add(role);
            String dateTimeNow = String.valueOf(LocalDateTime.now());

            List<AddressRequest> addressRequests = userRegistryRequest.getAddress();
            List<Address> addresses = new ArrayList<>();
            User finalUser = user;
            addressRequests.forEach(addressRequest -> {
                addresses.add(new Address(null, addressRequest.getStreet(), addressRequest.getCity(), finalUser));
            });
            user.setRoles(roles);
            user.setAddress(addresses);
            user = userRepository.save(user);

            UserDTO userDTO = mapperRequestToDTO.mapperUserRegistryRequestToDTO(userRegistryRequest);
            userDTO.setRole(user.getRoles()
                    .stream()
                    .map(Role::getNoteRole)
                    .map(Enum::toString)
                    .collect(Collectors.joining(", ")));
            String content = "You have successfully registered at " +
                    dateTimeNow + " with ID = " + user.getID() + " and information:" +
                    userDTO.toString();
            MailComponent mailDTO = new MailComponent(email, "successful registration!", content);
            emailService.sendMail(mailDTO);
            return new Message<>("successful registration!", HttpStatus.OK, userDTO);
        } else
            return new Message<>(String.join(" or ", messages), HttpStatus.BAD_REQUEST,
                    mapperRequestToDTO.mapperUserRegistryRequestToDTO(userRegistryRequest));
    }

    public AuthDTO validateToken(String token) {
        TokenValidationStatus tokenValidationStatus = tokenProvider.validateToken(token);
        if (tokenValidationStatus != TokenValidationStatus.VALID) throw new RuntimeException("Invalid JWT token");
        Optional<User> userOptional = userRepository.findById(tokenProvider.getUserIdFromJWT(token));
        if (userOptional.isEmpty()) throw new RuntimeException("User not found");
        User user = userOptional.get();
        return new AuthDTO(user.getID(), user.getAccount().getUsername(), token);
    }

    @Transactional
    public Message<CheckTimeRequest> updateTime(TimeRequest timeRequest, String token) {
        Long userID = tokenProvider.getUserIdFromJWT(token);
        CheckTimeRequest checkTimeRequest = checkTimeRequestRepository.findByUserID(userID);
        if (checkTimeRequest != null) {
            checkTimeRequest.setCheckIn(timeRequest.getCheckIn());
            checkTimeRequest.setCheckOut(timeRequest.getCheckOut());
            checkTimeRequestRepository.save(checkTimeRequest);
            return new Message<>("Update time success!", HttpStatus.OK, checkTimeRequest);
        }
        checkTimeRequest = new CheckTimeRequest(null, userID, timeRequest.getCheckIn(), timeRequest.getCheckOut(), false);
        LocalTime timeCheckIn = LocalTime.parse(timeRequest.getCheckIn());
        LocalTime timeCheckOut = LocalTime.parse(timeRequest.getCheckOut());
        if (timeCheckIn.isBefore(LocalTime.of(8, 0)) || timeCheckOut.isAfter(LocalTime.of(18, 0)))
            return new Message<>("you are updating outside the default time!", HttpStatus.OK, null);
        checkTimeRequestRepository.save(checkTimeRequest);
        return new Message<>("Update time success!", HttpStatus.OK, checkTimeRequest);
    }

    @Transactional
    public Message<UserDTO> updateRole(UpdateRoleRequest updateRoleRequest) {
        Long userID = updateRoleRequest.getUserID();
        List<String> rolesStr = updateRoleRequest.getRoles();
        User user = userRepository.findById(userID).orElse(null);
        if (user != null) {
            Set<Role> roles = new HashSet<>();
            rolesStr.forEach(roleStr -> {
                roles.add(roleRepository.findByNoteRole(Role.NoteRole.valueOf(roleStr)));
            });
            user.setRoles(roles);
            user = userRepository.save(user);
            UserDTO userDTO = mapperRequestToDTO.mapperUserToDTO(user);
            userDTO.setRole(user.getRoles()
                    .stream()
                    .map(Role::getNoteRole)
                    .map(Enum::toString)
                    .collect(Collectors.joining(", ")));
            userDTO.setUsername(user.getAccount().getUsername());
            return new Message<>("Update role success!", HttpStatus.OK, userDTO);
        }
        return new Message<>("User not found!", HttpStatus.BAD_REQUEST, null);
    }

    public Message<List<TotalJobByUser>> getTotalJobByUser() {
        List<TotalJobByUser> totalJobByUsers = jobDetailsRepository.getTotalJobByUser();
        if (totalJobByUsers.isEmpty()) return new Message<>("No data!", HttpStatus.OK, null);
        return new Message<>("Get total job by user success!", HttpStatus.OK, totalJobByUsers);
    }

    @Transactional
    public Message<PasswordResponse> updatePassword(PasswordRequest passwordRequest) {
        Long userID = passwordRequest.getUserID();
        String oldPassword = passwordRequest.getOldPassword();
        String newPassword = passwordRequest.getNewPassword();
        User user = userRepository.findById(userID).orElse(null);
        if (user != null) {
            Account account = user.getAccount();
            if (passwordEncoder.matches(oldPassword, account.getPassword())) {
                account.setPassword(passwordEncoder.encode(newPassword));
                accountRepository.save(account);
                String accessToken = tokenProvider.generateAccessToken(new CustomerUserDetails(user));
                accessTokenService.update(new AccessToken(userID, accessToken));
                return new Message<>("Update password success!", HttpStatus.OK, new PasswordResponse(accessToken));
            }
            return new Message<>("Old password is incorrect!", HttpStatus.BAD_REQUEST, null);
        }
        return new Message<>("User not found!", HttpStatus.BAD_REQUEST, null);
    }

    @Transactional
    public Message<?> deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            List<Address> addresses = user.getAddress();
            user.getRoles().removeAll(user.getRoles());
            user.getJobDetails().removeAll(user.getJobDetails());
            userRepository.delete(user);
//            Address address = addresses.get(10);
//            log.info("address: {}", address);
            return new Message<>("Delete user success!", HttpStatus.OK, null);
        }
        return new Message<>("User not found!", HttpStatus.BAD_REQUEST, null);
    }

    public Message<?> deleteAddress(Long addressID) {
        Address address = addressRepository.findById(addressID).orElse(null);
        if (address != null) {
//            User user = address.getUser();
//            user.getAddress().remove(address);
//            addressRepository.deleteById(addressID);
            addressRepository.delete(address);
            return new Message<>("Delete address success!", HttpStatus.OK, null);
        }
        return new Message<>("Address not found!", HttpStatus.BAD_REQUEST, null);
    }

    public Message<List<IUserDTO>> getAllUsers() {
        List<IUserDTO> userDTOS = userRepository.findAllUsers(Sort.by("fullName"));
        if (userDTOS.isEmpty()) return new Message<>("No data!", HttpStatus.OK, null);
        return new Message<>("Get all users success!", HttpStatus.OK, userDTOS);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Message<Page<UserDTO>> getAllUsersByPage(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<User> users = userRepository.findAllByPage(pageable);
        Page<UserDTO> userDTOS = users.map(user -> mapperRequestToDTO.mapperUserToDTO(user));
        if (userDTOS.isEmpty()) return new Message<>("No data!", HttpStatus.OK, null);
        return new Message<>(String.format("Get all successful users in the page as %d and size as %d!", page, size), HttpStatus.OK, userDTOS);
    }

    public Message<Slice<UserDTO>> getAllUsersBySlice(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Slice<User> users = userRepository.findAllBySlice(pageable);
        Slice<UserDTO> userDTOS = users.map(user -> mapperRequestToDTO.mapperUserToDTO(user));
        if (userDTOS.isEmpty()) return new Message<>("No data!", HttpStatus.OK, null);
        return new Message<>(String.format("Get all successful users in the page as %d and size as %d!", page, size), HttpStatus.OK, userDTOS);
    }
}
