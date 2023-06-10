package com.modoospace.config.auth;

import com.modoospace.config.auth.dto.SessionMember;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

  private final HttpSession httpSession;

  /**
   * 컨트롤러 메서드의 특정 파라미터를 지원하는지 판단
   *
   * @param parameter
   * @return
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    boolean isLoginUserAnnotation =
        parameter.getParameterAnnotation(LoginUser.class) != null; // 파라미터에 @LoginUser 어노테이션 유무 확인
    boolean isUserClass = SessionMember.class
        .equals(parameter.getParameterType()); // 파라미터 클래스 타입 SessionUser인지 확인

    return isLoginUserAnnotation && isUserClass;
  }

  /**
   * 파라미터에 전달할 객체 생성
   *
   * @param parameter
   * @param mavContainer
   * @param webRequest
   * @param binderFactory
   * @return
   * @throws Exception
   */
  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    return httpSession.getAttribute("member"); // 세션에서 객체를 가져와 전달
  }
}
