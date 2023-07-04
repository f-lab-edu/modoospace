package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.sevice.SpaceService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/spaces")
public class AdminSpaceController {

  private final SpaceService spaceService;

  @PutMapping("/{spaceId}")
  public ResponseEntity<Void> update(@PathVariable Long spaceId,
      @RequestBody @Valid SpaceCreateUpdateDto updateDto,
      @LoginEmail String loginEmail) {
    spaceService.updateSpace(spaceId, updateDto, loginEmail);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{spaceId}")
  public ResponseEntity<Void> delete(@PathVariable Long spaceId,
      @LoginEmail String loginEmail) {
    spaceService.deleteSpace(spaceId, loginEmail);
    return ResponseEntity.noContent().build();
  }
}
