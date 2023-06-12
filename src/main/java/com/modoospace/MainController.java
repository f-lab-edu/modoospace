package com.modoospace;

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
  public String index(Model model, @LoginUser SessionMember member) {
    if (member != null) {
      model.addAttribute("userName", member.getName());
    }
    return "index";
  }
}
