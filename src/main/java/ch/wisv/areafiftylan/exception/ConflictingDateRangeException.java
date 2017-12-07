package ch.wisv.areafiftylan.exception;

public class ConflictingDateRangeException extends AreaFiftyLANException {

    public ConflictingDateRangeException() { super("An entity covering the given date range already exists"); }
}
