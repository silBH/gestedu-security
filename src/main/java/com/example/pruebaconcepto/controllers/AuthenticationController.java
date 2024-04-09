package com.example.pruebaconcepto.controllers;

import com.example.pruebaconcepto.dtos.AuthLoginRequest;
import com.example.pruebaconcepto.dtos.AuthResponse;
import com.example.pruebaconcepto.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest userRequest) {
        return new ResponseEntity<>(this.userDetailsService.loginUser(userRequest), HttpStatus.OK);
    }

//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthCreateUser authCreateUser) {
//        return new ResponseEntity<>(this.userDetailsService.registerUser(userRequest), HttpStatus.CREATED);
//    }
}