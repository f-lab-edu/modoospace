package com.modoospace.common;

import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Custom Annotation의 SpEL표현 Parser 클래스
 */
public class CustomSpELParser {

  private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

  public static Object extractValueFromExpression(String[] parameterNames, Object[] args,
      String expression) {
    return getValue(expression, createContextAndSetVariable(parameterNames, args));
  }

  private static EvaluationContext createContextAndSetVariable(String[] parameterNames,
      Object[] args) {
    EvaluationContext context = new StandardEvaluationContext();
    IntStream.range(0, parameterNames.length)
        .forEach(i -> context.setVariable(parameterNames[i], args[i]));
    return context;
  }

  private static Object getValue(String expression, EvaluationContext context) {
    Expression parseExpression = EXPRESSION_PARSER.parseExpression(expression);
    Optional<Object> value = Optional.ofNullable(parseExpression.getValue(context));
    return value.orElseThrow(() -> new NullPointerException(expression));
  }
}
