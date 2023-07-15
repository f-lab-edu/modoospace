package com.modoospace;

import com.modoospace.config.auth.LoginEmail;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class MainController {

  @PersistenceContext
  private EntityManager entityManager;

  @Bean
  public JPAQueryFactory queryFactory() {
    return new JPAQueryFactory(entityManager);
  }

  @GetMapping({"", "/"})
  public String index(Model model, @LoginEmail String loginEmail) {
    if (loginEmail != null) {
      model.addAttribute("userName", loginEmail);
    }
    return "index";
  }
}
