package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.LoginRequest;
import com.tbank.ttravels_backend.dto.RegisterRequest;
import com.tbank.ttravels_backend.entity.PasswordCredential;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.repository.UserRepository;
import com.tbank.ttravels_backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return "Phone already exists";
        }


        User user = new User();
        user.setPhone(request.getPhone());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        //user.setRole(Role.USER);

        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        passwordCredential.setUser(user);

        user.setPassword(passwordCredential);

        userRepository.save(user);
        return "User registered successfully!";
    }

    @GetMapping("/test-users")
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{id}")
    public String DeleteUser(@PathVariable Long id){
        if(!userRepository.existsById(id)){
            return "User not found";
        }
        userRepository.deleteById(id);
        return "User deleted";
    }

    @PostMapping("/login")
    public String Login(@RequestBody LoginRequest request){
        logger.info("Phone: {}", request.getPhone());
        logger.info("Password: {}", request.getPassword());

        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if(userOpt.isEmpty()){
            logger.error("User not found for phone: {}", request.getPhone());
            return "User not found";
        }

        User user = userOpt.get();
        logger.info("User found: {} (ID: {})", user.getName(), user.getId());

        if(user.getPassword() == null || !passwordEncoder.
                matches(request.getPassword(), user.getPassword().getPasswordHash())){
            logger.error("Password is NULL for user: {}", user.getPhone());
            return "Invalid password";
        }

        logger.info("Password hash from DB: {}", user.getPassword().getPasswordHash());
        logger.info("Raw password from request: {}", request.getPassword());


        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        logger.info("Login successful! JWT token generated");
        return token;
    }
}
