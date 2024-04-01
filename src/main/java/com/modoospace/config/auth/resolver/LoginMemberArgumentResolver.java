package com.modoospace.config.auth.resolver;

import com.modoospace.common.exception.UnAuthenticatedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;

    /**
     * 컨트롤러 메서드의 특정 파라미터를 지원하는지 판단
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class) && parameter.getParameterType().equals(Member.class);
    }


    /**
     * 파라미터에 전달할 객체 생성
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        try {
            return memberService.findMemberByEmail(getLoginEmail(webRequest));
        } catch (RuntimeException e) {
            throw new UnAuthenticatedException();
        }
    }

    private String getLoginEmail(NativeWebRequest webRequest) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpSession session = request.getSession(false);
        return (String) session.getAttribute("member");
    }
}
