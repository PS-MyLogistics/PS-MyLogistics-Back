package com.mylogisticcba.iam.security.auth.controllers;

import com.mylogisticcba.iam.security.auth.dtos.response.AuthResponse;
import com.mylogisticcba.iam.security.auth.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class VerificationViewController {

    private final AuthService authService;

    @GetMapping(value = "/verifyRegisterTenantAndOwner")
    public String verifyRegisterTenantAndOwner(@RequestParam String token, @RequestParam String idTenant, Model model) {
        try {
            AuthResponse response = authService.verifyRegistrationTenantAndOwner(token, idTenant);

            // Configurar datos para la página de éxito
            model.addAttribute("message", response.getMessage());
            model.addAttribute("redirectUrl", "http://localhost:4200/login");
            model.addAttribute("redirectSeconds", 5);

            return "email-verified-success";
        } catch (Exception e) {
            // Configurar datos para la página de error
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("loginUrl", "http://localhost:4200/login");

            return "email-verification-error";
        }
    }

    @GetMapping(value = "/verifyRegisterUserInTenant")
    public String verifyRegisterUserInTenant(@RequestParam String token, @RequestParam String idTenant, Model model) {
        try {
            AuthResponse response = authService.verifyRegistrationInTenant(token, idTenant);

            // Configurar datos para la página de éxito
            model.addAttribute("message", response.getMessage());
            model.addAttribute("redirectUrl", "http://localhost:4200/login");
            model.addAttribute("redirectSeconds", 5);

            return "email-verified-success";
        } catch (Exception e) {
            // Configurar datos para la página de error
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("loginUrl", "http://localhost:4200/login");

            return "email-verification-error";
        }
    }

    @GetMapping(value = "/unfreeze")
    public String verifyAfterFreeze(@RequestParam String token, @RequestParam String tenantName,
                                   @RequestParam String username, Model model) {
        try {
            AuthResponse response = authService.verifyAfterFreeze(token, tenantName, username);

            // Configurar datos para la página de éxito
            model.addAttribute("message", "Tu cuenta ha sido desbloqueada exitosamente");
            model.addAttribute("redirectUrl", "http://localhost:4200/login");
            model.addAttribute("redirectSeconds", 5);

            return "email-verified-success";
        } catch (Exception e) {
            // Configurar datos para la página de error
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("loginUrl", "http://localhost:4200/login");

            return "email-verification-error";
        }
    }
}
