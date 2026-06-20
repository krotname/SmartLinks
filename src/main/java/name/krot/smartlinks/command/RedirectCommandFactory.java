package name.krot.smartlinks.command;

import name.krot.smartlinks.model.SmartLink;

import java.util.List;

public interface RedirectCommandFactory {
    List<RedirectCommand> createCommands(SmartLink smartLink);
}
