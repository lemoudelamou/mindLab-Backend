package com.example.mindLab.services;


import com.example.mindLab.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("cabiste81@live.fr");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendVerificationEmail(User user) throws UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "cabiste81@live.fr";
        String subject = "Please verify your registration";
        String content = "Dear " + user.getUsername() + ",\n"
                + "Please click the link below to verify your registration:\n"
                + "http://localhost:3000/verify-registration?verificationCode=" + user.getVerificationCode() + "\n"
                + "Thank you,\n"
                + "MindLab Team.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(content);

        emailSender.send(message);
    }


}



