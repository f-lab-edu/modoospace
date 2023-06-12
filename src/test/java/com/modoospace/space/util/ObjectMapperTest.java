package com.modoospace.space.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Controller 단 에서 RequestBody 인자로 사용되는 DTO의 Getter를 제외하려했으나, 자꾸 역직렬화 실패 예외가 터짐. 따라서 Spring에서
 * Json형변환을 담당하는 Jackson2HttpMessageConverter가 MessageBody read 시 사용하는 objectMapper 학습테스트를 작성한다.
 */
public class ObjectMapperTest {

  @DisplayName("@Getter와 @NoArgsConstructor가 있다면 직렬화 역직렬화 둘다 성공한다.")
  @Test
  public void objectMapper() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String content = objectMapper.writeValueAsString(new Success("name", 10));

    Success success = objectMapper.readValue(content, Success.class);

    assertThat(success.getName()).isEqualTo("name");
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Success {

    private String name;
    private Integer age;
  }

  @DisplayName("@Getter가 없다면 직렬화부터 실패한다.")
  @Test
  public void objectMapper_throwException_IfNoGetter() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    assertThatThrownBy(() -> objectMapper.writeValueAsString(new NoGetter("name", 10)))
        .isInstanceOf(InvalidDefinitionException.class);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  public static class NoGetter {

    String name;
    int age;
  }

  @DisplayName("인자없는 생성자(@NoArgsConstructor)가 없다면 직렬화는 성공하지만 역직렬화는 실패한다.")
  @Test
  public void objectMapper_throwException_IfNoDefaultConstructor() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String content = objectMapper.writeValueAsString(new NoDefaultConstructor("name", 10));

    assertThatThrownBy(() -> objectMapper.readValue(content, NoDefaultConstructor.class))
        .isInstanceOf(InvalidDefinitionException.class);
  }

  @Getter
  @AllArgsConstructor
  public static class NoDefaultConstructor {

    String name;
    int age;
  }
}
