package com.modoospace.common.SpELParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modoospace.common.CustomSpELParser;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.expression.spel.SpelEvaluationException;

public class CustomSpELParserTest {

  @DisplayName("#instance.filed 표현식이 주어진 경우 filed의 값을 반환한다.(age)")
  @Test
  public void parserTest_InstanceFiled_Age() {
    String[] parameterNames = {"person"};
    Object[] args = {new Person("jeonghwa", 29)};
    String expression = "#person.age";

    Object ret = CustomSpELParser.extractValueFromExpression(parameterNames, args, expression);

    assertThat((Integer) ret).isEqualTo(29);
  }

  @DisplayName("#instance.filed 표현식이 주어진 경우 filed의 값을 반환한다.(name)")
  @Test
  public void parserTest_InstanceFiled_Name() {
    String[] parameterNames = {"person"};
    Object[] args = {new Person("jeonghwa", 29)};
    String expression = "#person.name";

    Object ret = CustomSpELParser.extractValueFromExpression(parameterNames, args, expression);

    assertThat((String) ret).isEqualTo("jeonghwa");
  }


  @DisplayName("#instance.filed 표현식이 주어진 경우 해당 인스턴스의 없는 field일 경우 Exception이 발생한다.")
  @Test
  public void parserTest_InstanceNotHaveFiled() {
    String[] parameterNames = {"person"};
    Object[] args = {new Person("jeonghwa", 29)};
    String expression = "#person.noField";

    assertThatThrownBy(
        () -> CustomSpELParser.extractValueFromExpression(parameterNames, args, expression))
        .isInstanceOf(SpelEvaluationException.class);
  }

  @DisplayName("#instance.filed 표현식이 주어진 경우 해당 인스턴스 파라미터가 아예 존재하지 않는 경우 Exception이 발생한다.")
  @Test
  public void parserTest_NotAppearParameter_Instance() {
    String[] parameterNames = {"person"};
    Object[] args = {new Person("jeonghwa", 29)};
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
        .isInstanceOf(SpelEvaluationException.class);
  }

  @Getter
  public static class Person {

    private String name;
    private Integer age;

    public Person(String name, Integer age) {
      this.name = name;
      this.age = age;
    }
  }
}



