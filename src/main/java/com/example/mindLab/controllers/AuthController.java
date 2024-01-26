package com.example.mindLab.controllers;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import com.example.mindLab.payload.request.LoginRequest;
import com.example.mindLab.payload.request.RegisterRequest;
import com.example.mindLab.payload.response.JwtResponse;
import com.example.mindLab.payload.response.MessageResponse;
import com.example.mindLab.security.jwt.AuthTokenFilter;
import com.example.mindLab.services.EmailService;
import com.example.mindLab.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.mindLab.models.ERole;
import com.example.mindLab.models.Role;
import com.example.mindLab.models.User;
import com.example.mindLab.repositories.RoleRepository;
import com.example.mindLab.repositories.UserRepository;
import com.example.mindLab.security.jwt.JwtUtils;
import com.example.mindLab.imp.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthTokenFilter authTokenFilter;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    private final Set<String> tokenBlacklist = new HashSet<>();


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // After successful authentication, no need to check isActive here

        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }



    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) throws UnsupportedEncodingException {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account

        String verification = UUID.randomUUID().toString();

        User user = new User( signUpRequest.getFirstname(), signUpRequest.getLastname(),signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()), signUpRequest.isActive(), signUpRequest.getVerificationCode());
        emailService.sendVerificationEmail(user);

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // Get the authentication token from the request
        String jwt = authTokenFilter.parseJwt(request);

        if (jwt != null) {
            // Invalidate the token by adding it to the blacklist
            tokenBlacklist.add(jwt);
        }

        // Clear authentication from SecurityContext
        SecurityContextHolder.clearContext();

        // Optionally, you can also clear any cookies or other session-related data

        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }


    @GetMapping("/verifyjwt")
    public Optional<User> verifyJWT(Principal principal) {
        return userRepository. findByUsername(principal.getName());
    }



}