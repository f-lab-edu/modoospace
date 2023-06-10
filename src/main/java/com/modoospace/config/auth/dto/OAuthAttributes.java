package com.modoospace.config.auth.dto;

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
   *
   * @param registrationId
   * @param userNameAttributeName
   * @param attributes
   * @return
   */
  public static OAuthAttributes of(String registrationId, String userNameAttributeName,
      Map<String, Object> attributes) {
    if ("naver".equals(registrationId)) {
      return ofNaver("id", attributes);
    }
    return ofGoogle(userNameAttributeName, attributes);
  }

  private static OAuthAttributes ofGoogle(String userNameAttributeName,
      Map<String, Object> attributes) {
    return OAuthAttributes.builder()
        .name((String) attributes.get("name"))
        .email((String) attributes.get("email"))
        .attributes(attributes)
        .nameAttributeKey(userNameAttributeName)
        .build();
  }

  private static OAuthAttributes ofNaver(String userNameAttributeName,
      Map<String, Object> attributes) {

    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    return OAuthAttributes.builder()
        .name((String) response.get("name"))
        .email((String) response.get("email"))
        .attributes(response)
        .nameAttributeKey(userNameAttributeName)
        .build();
  }

  /**
   * UserEntity 생성 (처음 가입 시점)
   *
   * @return
   */
  public Member toEntity() {
    return Member.builder()
        .name(name)
        .email(email)
        .role(Role.VISITOR)
        .build();
  }
}

