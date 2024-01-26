package com.example.mindLab.services;




import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.example.mindLab.models.Event;
import com.example.mindLab.models.User;
import com.example.mindLab.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private static final long EXPIRE_TOKEN_AFTER_MINUTES = 30;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public String forgotPassword(String email) {

        Optional<User> userOptional = Optional
                .ofNullable(userRepository.findByEmail(email));

        if (!userOptional.isPresent()) {
            return "Invalid email id.";
        }

        User user = userOptional.get();
        user.setToken(generateToken());
        user.setTokenCreationDate(LocalDateTime.now());

        user = userRepository.save(user);

        // Send email with the reset password link
        String resetPasswordLink = "http://localhost:3000/reset-password?token=" + user.getToken();
        String emailSubject = "Reset Your Password";
        String emailBody = "Please click on the following link to reset your password: " + resetPasswordLink;
        emailService.sendEmail(email, emailSubject, emailBody);

        return "Reset password instructions have been sent to your email.";
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }


    public String activateAccount( String verificationCode) {

        Optional<User> userOptional = Optional
                .ofNullable(userRepository.findByVerificationCode(verificationCode));

        if (!userOptional.isPresent()) {
            return "Invalid token.";
        }


        User user = userOptional.get();


        user.setActive(true);
        user.setVerificationCode(null);

        userRepository.save(user);

        return "Your password has been successfully updated.";
    }

    public String resetPassword(String token, String password) {

        Optional<User> userOptional = Optional
                .ofNullable(userRepository.findByToken(token));

        if (!userOptional.isPresent()) {
            return "Invalid token.";
        }

        LocalDateTime tokenCreationDate = userOptional.get().getTokenCreationDate();

        if (isTokenExpired(tokenCreationDate)) {
            return "Token expired.";
        }

        User user = userOptional.get();

        if (!isStrongPassword(password)) {
            return "Password is not strong enough. It should contain at least 8 characters, including alphanumeric characters, special characters, and numbers.";
        }

        user.setPassword(password);
        user.setToken(null);
        user.setTokenCreationDate(null);

        userRepository.save(user);

        return "Your password has been successfully updated.";
    }

    private boolean isStrongPassword(String password) {
        // Check if the password meets the requirements for a strong password
        // Minimum 8 characters, alphanumeric characters, special characters, and numbers.
        String pattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return password.matches(pattern);
    }

    /**
     * Generate unique token. You may add multiple parameters to create a strong
     * token.
     *
     * @return unique token
     */
    private String generateToken() {
        StringBuilder token = new StringBuilder();

        return token.append(UUID.randomUUID().toString())
                .append(UUID.randomUUID().toString()).toString();
    }

    /**
     * Check whether the created token expired or not.
     *
     * @param tokenCreationDate
     * @return true or false
     */
    private boolean isTokenExpired(final LocalDateTime tokenCreationDate) {

        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(tokenCreationDate, now);

        return diff.toMinutes() >= EXPIRE_TOKEN_AFTER_MINUTES;
    }

}