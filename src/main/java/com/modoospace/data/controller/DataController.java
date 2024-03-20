package com.modoospace.data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.common.exception.EmptyResponseException;
import com.modoospace.config.auth.LoginEmail;
import com.modoospace.data.controller.dto.AddressResponse;
import com.modoospace.data.controller.dto.SpaceResponse;
import com.modoospace.data.service.DataService;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.domain.SpaceIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
public class DataController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private final DataService dataService;

    @Value("${spring.kakao.apikey}")
    private String key;

    @GetMapping("/space/{spaceId}")
    public SpaceResponse getSpace(@PathVariable String spaceId) throws IOException, InterruptedException {
        HttpRequest spaceRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://new-api.spacecloud.kr/spaces/" + spaceId))
                .build();
        HttpResponse<String> httpSpaceResponse = client.send(spaceRequest, HttpResponse.BodyHandlers.ofString());
        if (isResponseSpaceEmpty(httpSpaceResponse.body())) {
            throw new EmptyResponseException("Space", spaceId);
        }
        log.info("exist space: {}", spaceId);
        return objectMapper.readValue(httpSpaceResponse.body(), SpaceResponse.class);
    }

    private boolean isResponseSpaceEmpty(String responseBody) {
        return "{}".equals(responseBody.trim());
    }

    @GetMapping("/address")
    public AddressResponse getAddress(String address) throws IOException, InterruptedException {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        HttpRequest addressRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://dapi.kakao.com/v2/local/search/address?query=" + encodedAddress))
                .header("Authorization", "KakaoAK " + key)
                .build();
        HttpResponse<String> httpAddressResponse = client.send(addressRequest, HttpResponse.BodyHandlers.ofString());
        AddressResponse addressResponse = objectMapper.readValue(httpAddressResponse.body(), AddressResponse.class);
        if (addressResponse.getDocuments().isEmpty()) {
            throw new EmptyResponseException("Address", address);
        }
        return addressResponse;
    }

    @PostMapping("/space/{spaceId}")
    public SpaceDetailResponse saveSpace(@PathVariable String spaceId, @LoginEmail String email) throws IOException, InterruptedException {
        SpaceResponse spaceResponse = getSpace(spaceId);
        AddressResponse addressResponse = getAddress(spaceResponse.getLocation().getAddress());
        return dataService.saveEntity(spaceResponse, addressResponse, email);
    }

    @PostMapping("/space-index/{spaceId}")
    public SpaceIndex saveSpaceIndex(@PathVariable Long spaceId) {
        return dataService.saveIndex(spaceId);
    }
}
