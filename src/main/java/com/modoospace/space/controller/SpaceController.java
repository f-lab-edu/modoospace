package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateDto;
import com.modoospace.space.controller.dto.space.SpaceReadDetailDto;
import com.modoospace.space.controller.dto.space.SpaceReadDto;
import com.modoospace.space.sevice.SpaceService;
import java.net.URI;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/spaces")
public class SpaceController {

  private final SpaceService spaceService;

  @PostMapping("/category/{categoryId}")
  public ResponseEntity<Void> create(@PathVariable Long categoryId,
      @RequestBody @Valid SpaceCreateUpdateDto createDto,
      @LoginEmail String loginEmail) {
    Long spaceId = spaceService.createSpace(categoryId, createDto, loginEmail);
    return ResponseEntity.created(URI.create("/api/v1/spaces/" + spaceId)).build();
  }

  @GetMapping("/category/{categoryId}")
  public ResponseEntity<List<SpaceReadDto>> findByCategory(@PathVariable Long categoryId) {
    List<SpaceReadDto> spaceReadDtos = spaceService.findSpaceByCategory(categoryId);
    return ResponseEntity.ok().body(spaceReadDtos);
  }

  @GetMapping("/host/{hostId}")
  public ResponseEntity<Page<SpaceReadDto>> findByHost(@PathVariable Long hostId,
      @PageableDefault Pageable pageable) {
    Page<SpaceReadDto> spaceReadDtos = spaceService.findSpaceByHost(hostId, pageable);
    return ResponseEntity.ok().body(spaceReadDtos);
  }

  @GetMapping("/{spaceId}")
  public ResponseEntity<SpaceReadDetailDto> find(@PathVariable Long spaceId) {
    SpaceReadDetailDto spaceReadDto = spaceService.findSpace(spaceId);
    return ResponseEntity.ok().body(spaceReadDto);
  }

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
