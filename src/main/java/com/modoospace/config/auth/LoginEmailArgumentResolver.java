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
public class LoginEmailArgumentResolver implements HandlerMethodArgumentResolver {

  private final HttpSession httpSession;

  /**
   * 컨트롤러 메서드의 특정 파라미터를 지원하는지 판단
   *
   * @param parameter
   * @return
   */
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    boolean isLoginEmailAnnotation =
        parameter.getParameterAnnotation(LoginEmail.class) != null;
    boolean isStringClass = String.class
        .equals(parameter.getParameterType());

    return isLoginEmailAnnotation && isStringClass;
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
    SessionMember member = (SessionMember) httpSession.getAttribute("member");
    return member == null ? null : member.getEmail();
  }
}
