package ch.wisv.areafiftylan.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class AreaFiftyLANException extends RuntimeException {

    public enum LogLevelEnum {
        ERROR(log::error),
        WARN(log::warn),
        INFO(log::info),
        DEBUG(log::debug),
        TRACE(log::trace);

        private final Consumer<String> logger;

        LogLevelEnum(Consumer<String> logger) {
            this.logger = logger;
        }

        public void logMessage(String message) {
            logger.accept(message);
        }
    }

    public AreaFiftyLANException(LogLevelEnum logEnum, String message) {
        super(message);
        logEnum.logMessage(message);
    }

    public AreaFiftyLANException(String message) {
        this(LogLevelEnum.WARN, message);
    }
}
