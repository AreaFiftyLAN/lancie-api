package ch.wisv.areafiftylan.exception;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
@Slf4j
enum AreaFiftyLANExceptionLogLevelEnum {
    ERROR(log::error),
    WARN(log::warn),
    INFO(log::info),
    DEBUG(log::debug),
    TRACE(log::trace);

    private Consumer consumer;

    AreaFiftyLANExceptionLogLevelEnum(Consumer<String> consumer) {
        this.consumer = consumer;
    }

    public Consumer<String> getConsumer() {
        return consumer;
    }
}
