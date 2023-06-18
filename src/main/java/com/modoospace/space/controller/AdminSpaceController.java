package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.SpaceCreateDto;
import com.modoospace.space.controller.dto.SpaceUpdateDto;
import com.modoospace.space.sevice.SpaceService;
import java.net.URI;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin")
public class AdminSpaceController {

  private final SpaceService spaceService;

  @PostMapping("/space")
  public ResponseEntity<Void> createSpace(@RequestBody @Valid SpaceCreateDto createDto,
      @LoginEmail String loginEmail) {
    Long spaceId = spaceService.createSpace(createDto, loginEmail);
    return ResponseEntity.created(URI.create("/space/" + spaceId)).build();
  }

  @PutMapping("/space")
  public ResponseEntity<Void> updateSpace(@RequestBody @Valid SpaceUpdateDto updateDto,
      @LoginEmail String loginEmail) {
    spaceService.updateSpace(updateDto, loginEmail);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/space/{spaceId}")
  public ResponseEntity<Void> deleteSpace(@PathVariable Long spaceId,
      @LoginEmail String loginEmail) {
    spaceService.deleteSpace(spaceId, loginEmail);
    return ResponseEntity.noContent().build();
  }

}
