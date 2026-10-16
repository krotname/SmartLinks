package name.krot.smartlinks.exception;

public class SmartLinkVersionConflictException extends RuntimeException {
    public SmartLinkVersionConflictException() {
        super("The link has changed. Reload it before saving your changes.");
    }
}
