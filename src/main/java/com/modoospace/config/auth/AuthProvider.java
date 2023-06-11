package com.modoospace.config.auth;

import com.modoospace.config.auth.dto.OAuthAttributes;
import java.util.Arrays;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public enum AuthProvider {
  NAVER("naver", (userNameAttributeName, attributes) -> {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    return OAuthAttributes.builder()
        .name((String) response.get("name"))
        .email((String) response.get("email"))
        .attributes(response)
        .nameAttributeKey("id")
        .build();
  }),
  KAKAO("kakao", (userNameAttributeName, attributes) -> {
    Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");

    return OAuthAttributes.builder()
        .name((String) ((Map<String, Object>) account.get("profile")).get("nickname"))
        .email((String) account.get("email"))
        .attributes(attributes)
        .nameAttributeKey("id")
        .build();
  }),
  GOOGLE("google", (userNameAttributeName, attributes) -> {
    return OAuthAttributes.builder()
        .name((String) attributes.get("name"))
        .email((String) attributes.get("email"))
        .attributes(attributes)
        .nameAttributeKey(userNameAttributeName)
        .build();
  });

  private String name;
  private AuthProviderOf authProviderOf;

  private AuthProvider(String name, AuthProviderOf authProviderOf) {
    this.name = name;
    this.authProviderOf = authProviderOf;
  }

  public static AuthProvider findProvider(String registrationId) {
    return Arrays.stream(AuthProvider.values())
        .filter(authProvider -> authProvider.isName(registrationId))
        .findFirst()
        .orElseThrow(
            () -> new OAuth2AuthenticationException("Unsupported Login Type: " + registrationId));
  }

  private boolean isName(String name) {
    return this.name.equals(name);
  }

  public OAuthAttributes of(String userNameAttributeName, Map<String, Object> attributes) {
    return authProviderOf.of(userNameAttributeName, attributes);
  }
}
