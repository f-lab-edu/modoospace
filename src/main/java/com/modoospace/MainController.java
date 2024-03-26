package com.modoospace;

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
        String email = (String) session.getAttribute("member");
        if (email != null) {
            model.addAttribute("userName", email);
        }
        return "index";
    }
}
