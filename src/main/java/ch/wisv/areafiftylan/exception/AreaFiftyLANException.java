package ch.wisv.areafiftylan.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AreaFiftyLANException extends RuntimeException {

    public AreaFiftyLANException(AreaFiftyLANExceptionLogLevelEnum logLevelEnum, String message) {
        super("[" + logLevelEnum.toString() + "] " + message);
        switch (logLevelEnum) {
            case ERROR:
                log.error(message);
                break;
            case WARN:
                log.warn(message);
                break;
            case INFO:
                log.info(message);
                break;
            case DEBUG:
                log.debug(message);
                break;
            case TRACE:
                log.trace(message);
                break;
            default:
                log.error(message);
                break;
        }
    }

    public AreaFiftyLANException(String message) {
        this(AreaFiftyLANExceptionLogLevelEnum.WARN, message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
