package ch.wisv.areafiftylan.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AreaFiftyLANException extends RuntimeException {

    public AreaFiftyLANException(AreaFiftyLANExceptionLogLevelEnum logEnum, String message) {
        super(message);
        logEnum.apply(message);
    }

    public AreaFiftyLANException(String message) {
        this(AreaFiftyLANExceptionLogLevelEnum.WARN, message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
