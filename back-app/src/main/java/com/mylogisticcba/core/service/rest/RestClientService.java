package com.mylogisticcba.core.service.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class RestClientService {

    private final WebClient webClient;

    public RestClientService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Bloqueante: GET
    public <T> T get(String url, Class<T> responseType) {
        try {
            return webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error en llamada GET a: " + url + " -> " + e.getMessage(), e);
        }
    }

    // Bloqueante: GET con headers
    public <T> T getWithHeaders(String url, Map<String, String> headers, Class<T> responseType) {
        try {
            return webClient.get()
                    .uri(url)
                    .headers(h -> h.setAll(headers == null ? Map.of() : headers))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error en llamada GET a: " + url + " -> " + e.getMessage(), e);
        }
    }

    // Bloqueante: POST
    public <T> T post(String url, Object body, Class<T> responseType) {
        try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error en llamada POST a: " + url + " -> " + e.getMessage(), e);
        }
    }

    // Bloqueante: POST con headers
    public <T> T postWithHeaders(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        try {
            return webClient.post()
                    .uri(url)
                    .headers(h -> h.setAll(headers == null ? Map.of() : headers))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error en llamada POST a: " + url + " -> " + e.getMessage(), e);
        }
    }

    // Reactivo: GET
    public <T> Mono<T> getReactive(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    // Reactivo: GET con headers
    public <T> Mono<T> getReactiveWithHeaders(String url, Map<String, String> headers, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .headers(h -> h.setAll(headers == null ? Map.of() : headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseType);
    }

    // Reactivo: POST
    public <T> Mono<T> postReactive(String url, Object body, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }

    // Reactivo: POST con headers
    public <T> Mono<T> postReactiveWithHeaders(String url, Object body, Map<String, String> headers, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .headers(h -> h.setAll(headers == null ? Map.of() : headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType);
    }
}
