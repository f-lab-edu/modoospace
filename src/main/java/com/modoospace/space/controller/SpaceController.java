package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.SpaceCreateDto;
import com.modoospace.space.sevice.SpaceService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class SpaceController {

  private final SpaceService spaceService;

  @PostMapping("/space")
  @ResponseStatus(HttpStatus.CREATED)
  public Long createSpace(@RequestBody @Valid SpaceCreateDto createDto,
      @LoginEmail String loginEmail) {
    return spaceService.createSpace(createDto, loginEmail);
  }
}
