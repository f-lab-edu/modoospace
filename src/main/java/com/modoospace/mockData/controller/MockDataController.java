package com.modoospace.mockData.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.common.exception.EmptyResponseException;
import com.modoospace.config.auth.aop.CheckLogin;
import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import com.modoospace.mockData.controller.dto.MockAddressResponse;
import com.modoospace.mockData.controller.dto.MockSpaceResponse;
import com.modoospace.mockData.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/mock-data")
@Slf4j
public class MockDataController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private final MockDataService mockDataService;

    @Value("${spring.kakao.apikey}")
    private String key;

    @GetMapping("/space/{spaceId}")
    public ResponseEntity<MockSpaceResponse> getSpace(@PathVariable String spaceId) throws IOException, InterruptedException {
        return ResponseEntity.ok(getMockSpace(spaceId));
    }

    private MockSpaceResponse getMockSpace(String spaceId) throws IOException, InterruptedException {
        HttpRequest spaceRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://new-api.spacecloud.kr/spaces/" + spaceId))
                .build();
        HttpResponse<String> httpSpaceResponse = client.send(spaceRequest, HttpResponse.BodyHandlers.ofString());
        if (isResponseSpaceEmpty(httpSpaceResponse.body())) {
            throw new EmptyResponseException("Space", spaceId);
        }
        return objectMapper.readValue(httpSpaceResponse.body(), MockSpaceResponse.class);
    }

    private boolean isResponseSpaceEmpty(String responseBody) {
        return "{}".equals(responseBody.trim());
    }

    @GetMapping("/address")
    public ResponseEntity<MockAddressResponse> getAddress(String address) throws IOException, InterruptedException {
        return ResponseEntity.ok(getMockAddress(address));
    }

    private MockAddressResponse getMockAddress(String address) throws IOException, InterruptedException {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        HttpRequest addressRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://dapi.kakao.com/v2/local/search/address?query=" + encodedAddress))
                .header("Authorization", "KakaoAK " + key)
                .build();
        HttpResponse<String> httpAddressResponse = client.send(addressRequest, HttpResponse.BodyHandlers.ofString());
        MockAddressResponse addressResponse = objectMapper.readValue(httpAddressResponse.body(), MockAddressResponse.class);
        if (addressResponse.getDocuments().isEmpty()) {
            throw new EmptyResponseException("Address", address);
        }
        return addressResponse;
    }

    @CheckLogin
    @PostMapping("/space/{spaceId}")
    public ResponseEntity<Void> saveSpace(@PathVariable String spaceId, @LoginMember Member loginMember) throws IOException, InterruptedException {
        MockSpaceResponse spaceResponse = getMockSpace(spaceId);
        MockAddressResponse addressResponse = getMockAddress(spaceResponse.getLocation().getAddress());
        Long entitySpaceId = mockDataService.saveEntity(spaceResponse, addressResponse, loginMember);
        return ResponseEntity.created(URI.create("/api/v1/spaces/" + entitySpaceId)).build();
    }
}
