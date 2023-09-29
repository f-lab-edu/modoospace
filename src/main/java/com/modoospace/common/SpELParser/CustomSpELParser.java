package com.modoospace.common.SpELParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Custom Annotation의 SpEL표현을 Parser하는 클래스
 */
public class CustomSpELParser {

  private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
  private static final Pattern INSTANCE_FILED_PATTERN = Pattern
      .compile("#(\\w+)\\.(\\w+)");

  public static Object extractValueFromExpression(String[] parameterNames, Object[] args,
      String expression) {
    Matcher instanceFieldMatcher = INSTANCE_FILED_PATTERN.matcher(expression);
    // case1 : #instanceName.filedName
    if (instanceFieldMatcher.find()) {
      String instanceName = instanceFieldMatcher.group(1);
      String fieldName = instanceFieldMatcher.group(2);
      return getValueOfInstanceFiled(parameterNames, args, instanceName, fieldName);
    }

    // case2 : #value
    return getValue(expression, createVariableContext(parameterNames, args));
  }

  private static Object getValueOfInstanceFiled(String[] parameterNames, Object[] args,
      String instanceName, String fieldName) {
    int index = IntStream.range(0, parameterNames.length)
        .filter(i -> instanceName.equals(parameterNames[i]))
        .findFirst()
        .orElseThrow(() -> new SpelEvaluationException(
            SpelMessage.PROPERTY_OR_FIELD_NOT_READABLE_ON_NULL, instanceName));

    return getValue(fieldName, createRootObjectContext(args[index]));
  }

  private static EvaluationContext createRootObjectContext(Object object) {
    return new StandardEvaluationContext(object);
  }

  private static EvaluationContext createVariableContext(String[] parameterNames, Object[] args) {
    EvaluationContext context = new StandardEvaluationContext();
    IntStream.range(0, parameterNames.length)
        .forEach(i -> context.setVariable(parameterNames[i], args[i]));
    return context;
  }

  private static Object getValue(String expression, EvaluationContext context) {
    Expression parseExpression = EXPRESSION_PARSER.parseExpression(expression);
    Optional<Object> value = Optional.ofNullable(parseExpression.getValue(context));
    return value.orElseThrow(() -> new SpelEvaluationException(
        SpelMessage.PROPERTY_OR_FIELD_NOT_READABLE_ON_NULL, expression));
  }
}
