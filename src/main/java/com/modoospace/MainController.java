package com.modoospace;

import com.modoospace.config.auth.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Controller
public class MainController {

    @GetMapping({"", "/"})
    public String index(Model model, HttpSession session) {
        SessionMember member = (SessionMember) session.getAttribute("member");
        if (member != null) {
            model.addAttribute("userName", member.getEmail());
        }
        return "index";
    }
}
