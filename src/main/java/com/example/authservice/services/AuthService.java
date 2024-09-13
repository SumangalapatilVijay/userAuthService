package com.example.authservice.services;

import com.example.authservice.Repositories.SessionRepository;
import com.example.authservice.Repositories.UserRepository;
import com.example.authservice.exception.UserAlreadyExistException;
import com.example.authservice.exception.UserNotFoundException;
import com.example.authservice.models.Session;
import com.example.authservice.models.SessionStatus;
import com.example.authservice.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    @Value("${jwtKey}")
    private String secret;


    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    public AuthService(SessionRepository sessionRepository,UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    public boolean signUp(String email, String password) throws UserAlreadyExistException {
        if(userRepository.findByEmail(email) != null) {
            throw new UserAlreadyExistException("User with emailId = "+email+" already exists in our system");
        }

        String hashedPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(email,hashedPassword,null);
        userRepository.save(user);
        return true;
    }

    public String signIn(String email, String password) throws UserNotFoundException {
        if(userRepository.findByEmail(email) == null) {
            throw new UserNotFoundException("User with emailId = "+email+" not exists in our system");
        }
        User user = userRepository.findByEmail(email);
        if(bCryptPasswordEncoder.matches(password,user.getPassword())) {
            System.out.println("hurrrrrrhhhaaa.....User with emailId = "+email+" exists in our system");
            Map<String,String> header = new HashMap<>();
            SecretKey key = Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8));
            System.out.println(secret);

            Calendar c= Calendar.getInstance();
            c.add(Calendar.DATE, 30);
            Date d=c.getTime();
            header.put("email", user.getEmail());
            String jws = Jwts.builder().expiration(d) //a java.util.Date
                    .issuedAt(new Date()).claims(header).signWith(key).compact();
            Session session = new Session();
            session.setSessionStatus(SessionStatus.ACTIVE);
            session.setUser(user);
            session.setToken(jws);
            session.setExpiryAt(d);

            sessionRepository.save(session);
            System.out.println(jws);
            return jws;
        } else {
            throw new UserNotFoundException("Invalid password for the emailId = "+email);
        }

    }
    public boolean validate(String token) {
    try {
        SecretKey key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
        System.out.println(secret);
        Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        claims.getPayload().getSubject();
        claims.getPayload().getExpiration();
    } catch (JwtException e) {
        throw new RuntimeException(e);
    }
    return true;
    }

    public void logout(String token) throws UserNotFoundException {
        Session session = sessionRepository.findByToken(token);
        if(session != null) {
            session.setSessionStatus(SessionStatus.INACTIVE);
            sessionRepository.save(session);
        } else {
            throw new UserNotFoundException("user does not exists");
        }
    }
}
