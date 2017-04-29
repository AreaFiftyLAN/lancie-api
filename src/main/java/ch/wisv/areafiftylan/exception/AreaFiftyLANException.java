package ch.wisv.areafiftylan.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class AreaFiftyLANException extends RuntimeException {

    public AreaFiftyLANException(AreaFiftyLANExceptionLogLevelEnum logLevelEnum, String message) {
        super(message);
        logLevelEnum.getConsumer().accept(message);
    }

    public AreaFiftyLANException(String message) {
        this(AreaFiftyLANExceptionLogLevelEnum.WARN, message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
