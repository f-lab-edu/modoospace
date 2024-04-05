package com.modoospace.config.auth.service;

import com.modoospace.config.auth.dto.OAuthAttributes;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.repository.MemberCacheRepository;
import java.util.Collections;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final MemberRepository memberRepository;
  private final MemberCacheRepository memberCacheRepository;
  private final HttpSession httpSession;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    //1. registrationId : 현재 로그인 진행중인 서비스 구분 코드 (구글, 네이버 등등)
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    //2. userNameAttributeName : 로그인 진행 시 키가 되는 필드값
    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
        .getUserInfoEndpoint().getUserNameAttributeName();

    //3. OAuthAttributes : OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담은 클래스
    OAuthAttributes attributes = OAuthAttributes
        .of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

    Member member = saveOrUpdate(attributes);
    httpSession.setAttribute("member", member.getEmail());

    return new DefaultOAuth2User(
        Collections.singleton(member.createGrantedAuthority()),
        attributes.getAttributes(),
        attributes.getNameAttributeKey()
    );
  }

  private Member saveOrUpdate(OAuthAttributes attributes) {
    Member member = memberRepository.findByEmail(attributes.getEmail())
        .map(entity -> entity.updateNameFromProvider(attributes.getName()))
        .orElse(attributes.toEntity());

    memberRepository.save(member);
    memberCacheRepository.save(member);
    return member;
  }
}
