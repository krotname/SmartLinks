package name.krot.smartlinks.exception;

public class NoMatchingRuleException extends RuntimeException {
    public NoMatchingRuleException(String message) {
        super(message);
    }
}