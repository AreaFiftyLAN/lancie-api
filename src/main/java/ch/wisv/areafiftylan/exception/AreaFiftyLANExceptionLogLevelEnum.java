package ch.wisv.areafiftylan.exception;

import lombok.extern.slf4j.Slf4j;
@Slf4j
enum AreaFiftyLANExceptionLogLevelEnum {
    ERROR { @Override void apply(String message) { log.error(message); } },
    WARN  { @Override void apply(String message) { log.warn(message); } },
    INFO  { @Override void apply(String message) { log.info(message); } },
    DEBUG { @Override void apply(String message) { log.debug(message); } },
    TRACE { @Override void apply(String message) { log.trace(message); } };

    abstract void apply(String message);
}
