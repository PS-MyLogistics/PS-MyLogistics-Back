package com.mylogisticcba.core.service.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class NominatimGeoService {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeoService.class);
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String userAgent;

    public NominatimGeoService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${nominatim.user-agent:MyLogisticCBA/1.0 (mylogisticcba@gmail.com)}") String userAgent) {

        this.objectMapper = objectMapper;
        this.userAgent = userAgent;

        // WebClient específico para Nominatim con rate limiting
        this.webClient = webClientBuilder
                .baseUrl(NOMINATIM_URL)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }


    // Búsqueda estructurada (MÁS PRECISA)
    public LatLng getLatLngStructured(StructuredAddress address) {
        if (address == null || !address.isValid()) {
            throw new IllegalArgumentException("La dirección estructurada debe tener al menos un campo válido");
        }

        try {
            Thread.sleep(1000); // Rate limiting

            String uri = buildStructuredUri(address);

            String response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));

            return parseLatLng(response, address.toString());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupción durante geocodificación", e);
        } catch (WebClientResponseException e) {
            log.error("Error HTTP {} al geocodificar estructurado '{}': {}",
                    e.getStatusCode(), address, e.getMessage());
            throw new RuntimeException("Error al consultar Nominatim: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Error geocodificando estructurado '{}': {}", address, e.getMessage());
            throw new RuntimeException("Error obteniendo coordenadas: " + e.getMessage(), e);
        }
    }


    // Búsqueda estructurada reactiva
    public Mono<LatLng> getLatLngStructuredReactive(StructuredAddress address) {
        if (address == null || !address.isValid()) {
            return Mono.error(new IllegalArgumentException("La dirección estructurada debe tener al menos un campo válido"));
        }

        try {
            String uri = buildStructuredUri(address);

            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .delayElement(Duration.ofSeconds(1))
                    .map(response -> parseLatLng(response, address.toString()))
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                    .doOnError(e -> log.error("Error geocodificando reactivamente estructurado '{}': {}",
                            address, e.getMessage()));

        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error formando petición a Nominatim: " + e.getMessage(), e));
        }
    }





    public LatLng getLatLng(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }

        try {
            // Rate limiting: esperar 1 segundo entre llamadas
            Thread.sleep(1000);

            String encoded = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
            String url = "?q=" + encoded + "&format=json&limit=1&addressdetails=1";

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));

            return parseLatLng(response, address);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupción durante geocodificación", e);
        } catch (WebClientResponseException e) {
            log.error("Error HTTP {} al geocodificar '{}': {}",
                    e.getStatusCode(), address, e.getMessage());
            throw new RuntimeException("Error al consultar Nominatim: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Error geocodificando '{}': {}", address, e.getMessage());
            throw new RuntimeException("Error obteniendo coordenadas: " + e.getMessage(), e);
        }
    }

    public Mono<LatLng> getLatLngReactive(String address) {
        if (address == null || address.isBlank()) {
            return Mono.error(new IllegalArgumentException("La dirección no puede estar vacía"));
        }

        try {
            String encoded = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
            String url = "?q=" + encoded + "&format=json&limit=1&addressdetails=1";

            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .delayElement(Duration.ofSeconds(1)) // Rate limiting reactivo
                    .map(response -> parseLatLng(response, address))
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                    .doOnError(e -> log.error("Error geocodificando reactivamente '{}': {}",
                            address, e.getMessage()));

        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error formando petición a Nominatim: " + e.getMessage(), e));
        }
    }

    private LatLng parseLatLng(String response, String address) {
        try {
            if (response == null || response.isBlank()) {
                throw new GeocodingException("Sin respuesta de Nominatim para: " + address);
            }

            JsonNode root = objectMapper.readTree(response);

            if (!root.isArray() || root.isEmpty()) {
                throw new GeocodingException("No se encontraron coordenadas para: " + address);
            }

            JsonNode first = root.get(0);
            double lat = first.path("lat").asDouble();
            double lon = first.path("lon").asDouble();

            if (lat == 0.0 && lon == 0.0) {
                throw new GeocodingException("Coordenadas inválidas (0,0) para: " + address);
            }

            log.debug("Geocodificado '{}' -> [{}, {}]", address, lat, lon);
            return new LatLng(lat, lon);

        } catch (Exception e) {
            throw new GeocodingException("Error parseando respuesta de Nominatim: " + e.getMessage(), e);
        }
    }

    private String buildStructuredUri(StructuredAddress address) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .queryParam("addressdetails", 1);

        if (address.getStreet() != null && !address.getStreet().isBlank()) {
            builder.queryParam("street", address.getStreet().trim());
        }
        if (address.getCity() != null && !address.getCity().isBlank()) {
            builder.queryParam("city", address.getCity().trim());
        }
        if (address.getCounty() != null && !address.getCounty().isBlank()) {
            builder.queryParam("county", address.getCounty().trim());
        }
        if (address.getState() != null && !address.getState().isBlank()) {
            builder.queryParam("state", address.getState().trim());
        }
        if (address.getCountry() != null && !address.getCountry().isBlank()) {
            builder.queryParam("country", address.getCountry().trim());
        }
        if (address.getPostalCode() != null && !address.getPostalCode().isBlank()) {
            builder.queryParam("postalcode", address.getPostalCode().trim());
        }

        return builder.build().toUriString();
    }



    // Excepción personalizada
    public static class GeocodingException extends RuntimeException {
        public GeocodingException(String message) {
            super(message);
        }
        public GeocodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}