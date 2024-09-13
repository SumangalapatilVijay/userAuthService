package com.example.authservice.controllers;

import com.example.authservice.dtos.*;
import com.example.authservice.exception.UserNotFoundException;
import com.example.authservice.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/sign_up")
    public ResponseEntity<SignUpResponseDto> signUp(@RequestBody SignUpRequestDto reqDto) {
        SignUpResponseDto resDto = new SignUpResponseDto();
        String email = reqDto.getEmail();
        String password = reqDto.getPassword();
        try {
            if (authService.signUp(email, password))
                resDto.setRequestStatus(RequestStatus.SUCCESS);
            else
                resDto.setRequestStatus(RequestStatus.FAILURE);
            return new ResponseEntity<>(resDto, HttpStatus.OK);
        } catch (Exception e) {
            resDto.setRequestStatus(RequestStatus.FAILURE);
            System.out.println(e.getMessage());
            return new ResponseEntity<>(resDto, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PostMapping("/sign_in")
    public ResponseEntity<SignInResponseDto> signIn(@RequestBody SignInRequestDto reqDto) {
        SignInResponseDto resDto = new SignInResponseDto();
        String email = reqDto.getEmail();
        String password = reqDto.getPassword();
        String token;
        try {
            token = authService.signIn(email, password);
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
        resDto.setRequestStatus(RequestStatus.SUCCESS);
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        headers.add("AUTH_TOKEN",token);
        return new ResponseEntity<>(resDto, headers, HttpStatus.OK);

    }
    @GetMapping("/validate")
    public boolean validate(@RequestParam("token") String token) {
        return authService.validate(token);
    }
    @GetMapping("/logout")
    public void logout(@RequestParam("token") String token) {
        try {
            authService.logout(token);
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
