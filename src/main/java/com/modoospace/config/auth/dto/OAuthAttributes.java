package com.modoospace.config.auth.dto;

import com.modoospace.config.auth.provider.AuthProvider;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name,
            String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
    }

    /**
     * OAuth2User 사용자정보Map 변환
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
            Map<String, Object> attributes) {
        AuthProvider provider = AuthProvider.findProvider(registrationId);
        return provider.convert(userNameAttributeName, attributes);
    }

    /**
     * UserEntity 생성 (처음 가입 시점)
     */
    public Member toEntity() {
        return Member.builder()
                .name(name)
                .email(email)
                .role(Role.VISITOR)
                .build();
    }
}

