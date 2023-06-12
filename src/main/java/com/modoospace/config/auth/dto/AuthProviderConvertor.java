package com.modoospace.config.auth.dto;

import java.util.Map;

@FunctionalInterface
public interface AuthProviderConvertor {

  OAuthAttributes convert(String userNameAttributeName, Map<String, Object> attributes);
}
