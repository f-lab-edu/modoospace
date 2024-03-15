package com.modoospace.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.data.controller.dto.AddressResponse;
import com.modoospace.data.controller.dto.SpaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class DataController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${spring.kakao.apikey}")
    private String key;

    @GetMapping("/space/{spaceId}")
    public SpaceResponse getSpace(@PathVariable String spaceId) throws IOException, InterruptedException {
        HttpRequest spaceRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://new-api.spacecloud.kr/spaces/" + spaceId))
                .build();
        HttpResponse<String> httpSpaceResponse = client.send(spaceRequest, HttpResponse.BodyHandlers.ofString());
        String body = httpSpaceResponse.body();
        return objectMapper.readValue(body, SpaceResponse.class);
    }

    @GetMapping("/address")
    public AddressResponse getAddress(String address) throws IOException, InterruptedException {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        HttpRequest addressRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://dapi.kakao.com/v2/local/search/address?query="+encodedAddress))
                .header("Authorization", "KakaoAK " + key)
                .build();
        HttpResponse<String> httpAddressResponse = client.send(addressRequest, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(httpAddressResponse.body(), AddressResponse.class);
    }
}
