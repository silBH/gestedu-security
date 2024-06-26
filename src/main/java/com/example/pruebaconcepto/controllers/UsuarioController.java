package com.example.pruebaconcepto.controllers;

import com.example.pruebaconcepto.dtos.*;
import com.example.pruebaconcepto.models.Usuario;
import com.example.pruebaconcepto.services.EmailService;
import com.example.pruebaconcepto.services.TokenPassService;
import com.example.pruebaconcepto.services.UserDetailsServiceImpl;
import com.example.pruebaconcepto.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    EmailService emailService;

    @Autowired
    TokenPassService tokenPassService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String emailFrom;


    @PostMapping("/registro") //invitado
    public ResponseEntity<AuthResponse> registrarEstudiante(@RequestBody @Valid CrearUsuarioRequest crearUsuarioRequest) {
        crearUsuarioRequest.setTipoUsuario(TipoUsuario.ESTUDIANTE);
        return new ResponseEntity<>(this.userDetailsService.registrarUsuario(crearUsuarioRequest), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROL_ADMINISTRADOR')")
    @PostMapping("/registro/admin")
    public ResponseEntity<AuthResponse> registrarUsuario(@RequestBody @Valid CrearUsuarioRequest crearUsuarioRequest) {
        return new ResponseEntity<>(this.userDetailsService.registrarUsuario(crearUsuarioRequest), HttpStatus.CREATED);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> sendEmailResetPassword(@RequestBody EmailValuesDTO dto) {

        Optional<Usuario> usuario = usuarioService.getByEmail(dto.getMailTo());

        if(usuario.isEmpty()) {
            return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
        }

        Usuario user = usuario.get();
        String token = tokenPassService.crearTokenPassword(user);
        dto.setMailFrom(emailFrom);
        dto.setMailTo(user.getEmail());
        dto.setMailSubject("Recuperar de contraseña");
        dto.setTokenPassword(token);
        emailService.sendEmailResetPass(dto);
        return new ResponseEntity<>("Correo enviado con éxito", HttpStatusCode.valueOf(200));
    }

    @PostMapping("/cambiarPassword")
    public ResponseEntity<?> cambiarPassword(@Valid @RequestBody ChangePasswordDTO dto, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>("Datos incorrectos", HttpStatus.BAD_REQUEST);
        }
        if(!dto.getPassword().equals(dto.getConfirmPassword())){
            return new ResponseEntity<>("Las contraseñas no coinciden", HttpStatus.BAD_REQUEST);
        }

        Optional<Usuario> usuario = tokenPassService.validarToken(dto.getTokenPassword());
        if(usuario.isEmpty()){
            return new ResponseEntity<>("Token no encontrado", HttpStatus.NOT_FOUND);
        }
        Usuario user = usuario.get();
        String newPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(newPassword);
        usuarioService.updateUsuario(user);

        tokenPassService.invalidarToken(dto.getTokenPassword());

        return new ResponseEntity<>("Contraseña actualizada.", HttpStatus.OK);
    }

}
