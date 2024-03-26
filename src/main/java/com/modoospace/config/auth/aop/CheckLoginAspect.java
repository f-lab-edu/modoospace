package com.modoospace.config.auth.aop;

import com.modoospace.common.exception.PermissionDeniedException;
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

        String email = (String) httpSession.getAttribute("member");

        if (email == null) {
            throw new PermissionDeniedException();
        }
    }
}
