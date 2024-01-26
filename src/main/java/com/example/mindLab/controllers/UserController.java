package com.example.mindLab.controllers;


import com.example.mindLab.models.Event;
import com.example.mindLab.models.User;
import com.example.mindLab.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {

        String response = userService.forgotPassword(email);

        if (!response.startsWith("Invalid")) {
            response = "http://localhost:8081/api/reset-password";
        }
        return response;
    }

    @PutMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password) {



        return userService.resetPassword(token, encoder.encode(password));
    }



    @PutMapping("/verify-registration")
    public ResponseEntity<String> verifyUser(@RequestParam(required = false) String verificationCode) {
        if (verificationCode == null) {
            // Verification code is null, return an error response
            return ResponseEntity.badRequest().body("Verification code is null");
        }

        // Call the service to activate the account
        String activationResult = userService.activateAccount(verificationCode);

        // You can customize the response based on the result from the service
        return ResponseEntity.ok(activationResult);
    }



    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUserDetails(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> existingUser = userService.getUserById(id);

        if (existingUser.isPresent()) {
            User currentUser = existingUser.get();

            currentUser.setFirstname(updatedUser.getFirstname());
            currentUser.setLastname(updatedUser.getLastname());




            User updatedUserEntity = userService.saveUser(currentUser);
            return new ResponseEntity<>(updatedUserEntity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}




