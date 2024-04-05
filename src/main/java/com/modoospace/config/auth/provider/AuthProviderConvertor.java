package com.modoospace.config.auth.provider;

import com.modoospace.config.auth.dto.OAuthAttributes;
import java.util.Map;

@FunctionalInterface
public interface AuthProviderConvertor {

    OAuthAttributes convert(String userNameAttributeName, Map<String, Object> attributes);
}
