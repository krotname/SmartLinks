package name.krot.smartlinks.command;

@FunctionalInterface
public interface PredicateCommand {
    boolean execute();
}
