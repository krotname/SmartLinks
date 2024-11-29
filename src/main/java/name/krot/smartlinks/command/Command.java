package name.krot.smartlinks.command;

@FunctionalInterface
public interface Command<T> {
    T execute();
}