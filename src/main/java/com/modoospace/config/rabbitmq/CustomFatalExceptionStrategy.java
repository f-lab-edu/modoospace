package com.modoospace.config.rabbitmq;

import com.modoospace.common.exception.MessageParsingError;
import com.modoospace.common.exception.NotFoundEntityException;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler.DefaultExceptionStrategy;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;

public class CustomFatalExceptionStrategy implements FatalExceptionStrategy {

    private final FatalExceptionStrategy fatalExceptionStrategy = new DefaultExceptionStrategy();

    /**
     * 치명적 에러로 간주하고 재처리하지 않는다.
     */
    @Override
    public boolean isFatal(Throwable t) {
        return fatalExceptionStrategy.isFatal(t)
                || t.getCause() instanceof NotFoundEntityException
                || t.getCause() instanceof MessageParsingError;
    }
}
