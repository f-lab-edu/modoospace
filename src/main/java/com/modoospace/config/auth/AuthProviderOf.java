package com.modoospace.config.auth;

import com.modoospace.config.auth.dto.OAuthAttributes;
import java.util.Map;

@FunctionalInterface
public interface AuthProviderOf {

  OAuthAttributes of(String userNameAttributeName, Map<String, Object> attributes);
}
