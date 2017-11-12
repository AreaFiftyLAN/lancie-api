package ch.wisv.areafiftylan.exception;

public class ConflictingDateRangeException extends AreaFiftyLANException {

    public ConflictingDateRangeException() { super("An entity covering (a part of) the given date range already exists"); }
}
