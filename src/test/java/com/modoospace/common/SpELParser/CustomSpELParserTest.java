package com.modoospace.common.SpELParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.common.CustomSpELParser;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.SpelEvaluationException;

public class CustomSpELParserTest {

  private Person person = new Person(10L, "jeonghwa", 29, true);

  @DisplayName("#instance.filed 표현식이 주어진 경우 filed의 값을 반환한다.(Long)")
  @Test
  public void parserTest_InstanceFiled_Long() {
    String[] parameterNames = {"person"};
    Object[] args = {person};
    String expression = "#person.id";

    Object ret = CustomSpELParser.extractValueFromExpression(parameterNames, args, expression);

    assertThat((Long) ret).isEqualTo(10L);
  }

  @DisplayName("#instance.filed 표현식이 주어진 경우 filed의 값을 반환한다.(String)")
  @Test
  public void parserTest_InstanceFiled_String() {
    String[] parameterNames = {"person"};
    Object[] args = {person};
    String expression = "#person.name";

    Object ret = CustomSpELParser.extractValueFromExpression(parameterNames, args, expression);

    assertThat((String) ret).isEqualTo("jeonghwa");
  }

  @DisplayName("#instance.filed 표현식이 주어진 경우 filed의 값을 반환한다.(Integer)")
  @Test
  public void parserTest_InstanceFiled_Integer() {
    String[] parameterNames = {"person"};
    Object[] args = {person};
    String expression = "#person.age";

    Object ret = CustomSpELParser.extractValueFromExpression(parameterNames, args, expression);

    assertThat((Integer) ret).isEqualTo(29);
  }

  @DisplayName("#instance.filed 표현식이 주어진 경우 filed의 값을 반환한다.(Boolean)")
  @Test
  public void parserTest_InstanceFiled_Boolean() {
    String[] parameterNames = {"person"};
    Object[] args = {person};
    String expression = "#person.use";

    Object ret = CustomSpELParser.extractValueFromExpression(parameterNames, args, expression);

    assertThat((Boolean) ret).isEqualTo(true);
  }


  @DisplayName("#instance.filed 표현식이 주어진 경우 해당 인스턴스의 없는 field일 경우 Exception이 발생한다.")
  @Test
  public void parserTest_InstanceNotHaveFiled() {
    String[] parameterNames = {"person"};
    Object[] args = {person};
    String expression = "#person.noField";

    assertThatThrownBy(
        () -> CustomSpELParser.extractValueFromExpression(parameterNames, args, expression))
        .isInstanceOf(SpelEvaluationException.class);
  }

  @DisplayName("#instance.filed 표현식이 주어진 경우 해당 인스턴스 파라미터가 아예 존재하지 않는 경우 Exception이 발생한다.")
  @Test
  public void parserTest_NotAppearParameter_Instance() {
    String[] parameterNames = {"person"};
    Object[] args = {person};
    String expression = "#noInstance.age";

    assertThatThrownBy(
        () -> CustomSpELParser.extractValueFromExpression(parameterNames, args, expression))
        .isInstanceOf(SpelEvaluationException.class);
  }

  @DisplayName("#value 표현식이 주어진 경우 value의 값을 반환한다.")
  @Test
  public void parserTest_Value() {
    String[] parameterNames = {"test"};
    Object[] args = {"test value"};
    String expression = "#test";

    Object ret = CustomSpELParser.extractValueFromExpression(parameterNames, args, expression);

    assertThat((String) ret).isEqualTo("test value");
  }

  @DisplayName("#value 표현식이 주어진 경우 해당 value 파라미터가 아예 존재하지 않는 경우 Exception이 발생한다.")
  @Test
  public void parserTest_NotAppearParameter_Value() {
    String[] parameterNames = {"test"};
    Object[] args = {"test value"};
    String expression = "#noTest";

    assertThatThrownBy(
        () -> CustomSpELParser.extractValueFromExpression(parameterNames, args, expression))
        .isInstanceOf(NullPointerException.class);
  }

  @Getter
  public static class Person {

    private Long id;
    private String name;
    private Integer age;
    private Boolean use;

    public Person(Long id, String name, Integer age, Boolean use) {
      this.id = id;
      this.name = name;
      this.age = age;
      this.use = use;
    }
  }
}



