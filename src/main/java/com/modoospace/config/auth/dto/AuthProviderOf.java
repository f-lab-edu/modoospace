package com.modoospace.config.auth.dto;

import java.util.Map;

@FunctionalInterface
public interface AuthProviderOf {

  OAuthAttributes of(String userNameAttributeName, Map<String, Object> attributes);
}
