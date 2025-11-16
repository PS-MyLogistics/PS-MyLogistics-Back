package com.mylogisticcba.config;
import  com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import  io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SpringDocConfig {

    @Value("${app.url}")
    private String url;
    @Value("${app.dev-name}")
    private String devName;
    @Value("${app.dev.email}")
    private String devEmail;

    @Bean
    public OpenAPI openApi(
            @Value("${app.name}") String appName ,
             @Value("${app.desc}") String appDescripcion,
             @Value("${app.version}") String appVersion){

        Info info = new Info()
                .title(appName)
                .version(appVersion)
                .description(appDescripcion)
                .contact(
                        new Contact()
                                .name(devName)
                                .email(devEmail)
                );
        Server server = new Server()
                .url(url)
                .description(appDescripcion);

        // 🔐 Agregar Bearer Auth
        Components components = new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );

        // 🔐 Requerir Bearer Auth globalmente
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");
        return  new OpenAPI()
                .components(components)         // ✅ ahora sí usamos components
                .addSecurityItem(securityRequirement) // ✅ esto activa el botón Authorize
                .info(info)
                .addServersItem(server);


    }
    @Bean
    public  ModelResolver modelResolver(ObjectMapper objectMapper){return  new ModelResolver(objectMapper);}


}