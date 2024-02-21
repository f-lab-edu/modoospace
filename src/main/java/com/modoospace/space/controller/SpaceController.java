package com.modoospace.space.controller;

import com.modoospace.config.auth.LoginEmail;
import com.modoospace.space.controller.dto.space.SpaceCreateUpdateRequest;
import com.modoospace.space.controller.dto.space.SpaceDetailResponse;
import com.modoospace.space.controller.dto.space.SpaceResponse;
import com.modoospace.space.controller.dto.space.SpaceSearchRequest;
import com.modoospace.space.sevice.SpaceService;
import java.net.URI;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        @RequestBody @Valid SpaceCreateUpdateRequest createRequest,
        @LoginEmail String loginEmail) {
        Long spaceId = spaceService.createSpace(categoryId, createRequest, loginEmail);
        return ResponseEntity.created(URI.create("/api/v1/spaces/" + spaceId)).build();
    }

    @GetMapping()
    public ResponseEntity<Page<SpaceResponse>> search(SpaceSearchRequest searchRequest,
        Pageable pageable) {
        searchRequest.updateTimeRange();
        Page<SpaceResponse> spaces = spaceService.searchSpace(searchRequest, pageable);
        return ResponseEntity.ok().body(spaces);
    }

    @GetMapping("/{spaceId}")
    public ResponseEntity<SpaceDetailResponse> find(@PathVariable Long spaceId) {
        SpaceDetailResponse space = spaceService.findSpace(spaceId);
        return ResponseEntity.ok().body(space);
    }

    @PutMapping("/{spaceId}")
    public ResponseEntity<Void> update(@PathVariable Long spaceId,
        @RequestBody @Valid SpaceCreateUpdateRequest updateRequest,
        @LoginEmail String loginEmail) {
        spaceService.updateSpace(spaceId, updateRequest, loginEmail);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{spaceId}")
    public ResponseEntity<Void> delete(@PathVariable Long spaceId,
        @LoginEmail String loginEmail) {
        spaceService.deleteSpace(spaceId, loginEmail);
        return ResponseEntity.noContent().build();
    }
}
