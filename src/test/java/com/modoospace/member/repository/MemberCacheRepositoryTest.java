package com.modoospace.member.repository;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class MemberCacheRepositoryTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    MemberCacheRepository memberCacheRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    public void after() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @DisplayName("Redis에 Member를 캐싱하고 가져온다.")
    @Test
    public void save() {
        Member member = Member.builder()
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();
        memberCacheRepository.save(member);

        Member retMember = memberCacheRepository.findByEmail("host@email").get();
        assertThat(retMember.getEmail()).isEqualTo(member.getEmail());
    }
}
