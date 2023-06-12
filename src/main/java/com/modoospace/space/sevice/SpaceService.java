package com.modoospace.space.sevice;

import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.space.controller.dto.SpaceCreateDto;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SpaceService {

  private final MemberRepository memberRepository;
  private final SpaceRepository spaceRepository;

  public Long createSpace(SpaceCreateDto createDto, String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("해당 " + email + "을 가진 멤버를 찾을 수 없습니다."));
    Space space = createDto.toEntity(member);

    spaceRepository.save(space);

    return space.getId();
  }
}
