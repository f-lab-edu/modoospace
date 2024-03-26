package com.modoospace.config.auth.aop;

import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.config.auth.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpSession;

@Component
@Aspect
@RequiredArgsConstructor
public class CheckLoginAspect {

    private final HttpSession httpSession;

    @Before("@annotation(com.modoospace.config.auth.aop.CheckLogin)")
    public void checkLogin() throws HttpClientErrorException {

        SessionMember member = (SessionMember) httpSession.getAttribute("member");

        if (member == null) {
            throw new PermissionDeniedException();
        }
    }
}
