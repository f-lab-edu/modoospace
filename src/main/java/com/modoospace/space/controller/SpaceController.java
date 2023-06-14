package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.SpaceCreateDto;
import com.modoospace.space.controller.dto.SpaceReadDto;
import com.modoospace.space.sevice.SpaceService;
import java.net.URI;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class SpaceController {

  private final SpaceService spaceService;

  @PostMapping("/space")
  public ResponseEntity<Void> createSpace(@RequestBody @Valid SpaceCreateDto createDto,
      @LoginEmail String loginEmail) {
    Long spaceId = spaceService.createSpace(createDto, loginEmail);
    return ResponseEntity.created(URI.create("/space/" + spaceId)).build();
  }

  @GetMapping("/space/{spaceId}")
  public ResponseEntity<SpaceReadDto> findSpace(@PathVariable Long spaceId) {
    SpaceReadDto spaceReadDto = spaceService.findSpaceById(spaceId);
    return ResponseEntity.ok().body(spaceReadDto);
  }
}
