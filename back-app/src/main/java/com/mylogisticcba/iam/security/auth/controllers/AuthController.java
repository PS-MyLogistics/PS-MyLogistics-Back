package com.mylogisticcba.iam.security.auth.controllers;


import com.mylogisticcba.iam.security.auth.dtos.req.*;
import com.mylogisticcba.iam.security.auth.dtos.response.AuthResponse;
import com.mylogisticcba.iam.security.auth.dtos.response.EncodedMailResponse;
import com.mylogisticcba.iam.security.auth.dtos.response.LoginResponse;
import com.mylogisticcba.iam.security.auth.services.AuthService;
import com.mylogisticcba.iam.security.auth.services.impls.ResetPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")

public class AuthController {


    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;
    public AuthController(AuthService a, ResetPasswordService r) {
        authService= a;
        resetPasswordService = r;
    }

    @PostMapping(value="login")
    public ResponseEntity<AuthResponse> login( @RequestBody LoginRequest req){

        System.out.println("Login endpoint: recibí request para user = " + req.getUsername());

        LoginResponse loginResponse = authService.login(req);
            ResponseCookie cookie = generateCookie(loginResponse);
            AuthResponse body = AuthResponse.builder()
                    .success(true)
                    .message(loginResponse.getMessage())
                    .token(loginResponse.getAccesToken())
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(body);

    }
    @PostMapping(value="register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterOwnerRequest req){

        return ResponseEntity.ok(authService.registerTenantAndOwner(req));
    }

    //TODO redireccionar a login del frentend si o pagina estatica que diga que la cuenta fue verificada o el token es invalido
    //TODO crear endpoint para que el usuario pueda solicitar un nuevo email de verificacion
    @GetMapping(value="verifyRegisterTenantAndOwner")
    public ResponseEntity<AuthResponse> verifyRegister(@RequestParam String token,@RequestParam String idTenant ){

        return ResponseEntity.ok(authService.verifyRegistrationTenantAndOwner(token,idTenant));
    }

    //TODO redireccionar a login del frontend
    @GetMapping(value="verifyRegisterUserInTenant")
    public ResponseEntity<AuthResponse> verifyRegister2(@RequestParam String token,@RequestParam String idTenant ){

        return ResponseEntity.ok(authService.verifyRegistrationInTenant(token,idTenant));
    }
    //TODO redireccionar a login del frontend
    @GetMapping(value="unfreeze")
    public ResponseEntity<AuthResponse> verifyAfterFreeze(@RequestParam String token,@RequestParam String tenantName,@RequestParam String username ){

        return ResponseEntity.ok(authService.verifyAfterFreeze(token,tenantName,username));
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue(name = "refreshToken") String refreshTokenCookie) {

        // validate  and rotate token
        LoginResponse loginResponse = authService.rotateRefreshToken(refreshTokenCookie);
        // create coockie with de new refresh token
        ResponseCookie cookie = generateCookie(loginResponse);

        AuthResponse body = AuthResponse.builder()
                .success(true)
                .message(loginResponse.getMessage())
                .token(loginResponse.getAccesToken())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    @PostMapping("/reset-password/masked-email")
    public ResponseEntity<EncodedMailResponse> maskEmailRequest(@Valid @RequestBody MaskEmailRequest request) {

        EncodedMailResponse body = resetPasswordService.getMaskEmail(request.getUsername(),request.getTenantName());

        return ResponseEntity.ok().body(body);

    }
    @PostMapping("/reset-password/request-token")
    public ResponseEntity<Void> resetPasswordToken(@Valid @RequestBody SoliciteResetPassRequest request) {
         resetPasswordService.requestResetToken(request);
        return ResponseEntity.accepted().build();

    }
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<EncodedMailResponse> solicitResetPassword(@Valid @RequestBody ConfirmResetPassRequest request) {

        resetPasswordService.confirmResetPassword(request);
        return ResponseEntity.noContent().build();

    }



    private ResponseCookie generateCookie(LoginResponse refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken.getRefreshToken().getToken().toString())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshToken.getRefreshToken().getMaxAgeInSeconds())
                .sameSite("Strict")
                .build();
    }


}