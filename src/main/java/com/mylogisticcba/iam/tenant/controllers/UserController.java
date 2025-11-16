package com.mylogisticcba.iam.tenant.controllers;


import com.mylogisticcba.iam.security.auth.dtos.response.AuthResponse;
import com.mylogisticcba.iam.security.auth.services.AuthService;
import com.mylogisticcba.iam.tenant.dtos.RegisterUserInTenantRequest;
import com.mylogisticcba.iam.tenant.dtos.UserDto;
import com.mylogisticcba.iam.tenant.services.impl.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/")
public class UserController {


    private final UserService service;
    private final AuthService authService;

    public UserController(UserService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(service.getUsersByTenant());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("/createInternalUser" )
    public ResponseEntity<UserDto> createUserInTenant(@RequestBody RegisterUserInTenantRequest req) {
        return ResponseEntity.ok(service.createUserInTenant(req));
    }
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@CookieValue(name = "refreshToken") String refreshTokenCookie) {
        AuthResponse response = authService.logoutSession(refreshTokenCookie);

        // eliminar la cookie en el navegador
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }



}
