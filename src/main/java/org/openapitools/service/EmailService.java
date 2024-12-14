package org.openapitools.service;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${email.host}")
    private String host;

    @Value("${email.port}")
    private int port;

    @Value("${email.username:}")
    private String username;

    @Value("${email.password:}")
    private String password;

    @Value("${email.from}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String token) throws EmailException {
        String resetUrl = "http://gui:8080/streamhub/reset-password?token=" + token;

        Email email = new SimpleEmail();
        email.setHostName(host);
        email.setSmtpPort(port);
        email.setAuthenticator(new DefaultAuthenticator(username, password));
        email.setSSLOnConnect(false);
        email.setFrom(fromEmail);
        email.setSubject("Password Reset Request");
        email.setMsg("Click the link below to reset your password:\n" + resetUrl);
        email.addTo(toEmail);
        email.send();
    }
}
