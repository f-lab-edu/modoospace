package com.modoospace;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.config.auth.LoginUser;
import com.modoospace.config.auth.dto.SessionMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class MainController {

  @GetMapping({"", "/"})
  public String index(Model model, @LoginEmail String loginEmail) {
    if (loginEmail != null) {
      model.addAttribute("userName", loginEmail);
    }
    return "index";
  }
}
