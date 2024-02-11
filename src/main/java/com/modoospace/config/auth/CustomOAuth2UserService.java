package com.modoospace.config.auth;

import com.modoospace.config.auth.dto.OAuthAttributes;
import com.modoospace.config.auth.dto.SessionMember;
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

    //4. SessionUser : 세션에 사용자 정보를 저장하기 위한 Dto클래스
    // 왜 Member를 사용하지않고 SessionMember를 사용 ? Member 클래스가 엔티티이기 때문.
    // 엔티티 클래스를 직렬화한다면, 의존관계를 갖는 다른 엔티티들까지 직렬화할 가능성이 있어 성능이 느려질 수 있다.
    Member member = saveOrUpdate(attributes);
    httpSession.setAttribute("member", new SessionMember(member));

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
