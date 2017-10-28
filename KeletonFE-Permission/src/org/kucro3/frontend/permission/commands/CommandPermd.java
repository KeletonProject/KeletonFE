package org.kucro3.frontend.permission.commands;

import org.kucro3.frontend.permission.I18n;
import org.kucro3.frontend.permission.misc.Misc;
import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.permission.EnhancedPermissionService;
import org.kucro3.keleton.text.TextUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class CommandPermd implements CommandExecutor {
    public CommandPermd(EnhancedPermissionService service, LocaleProperties locale)
    {
        this.service = service;
        this.locale = locale;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        String operation = args.<String>getOne("operation").get();
        String permission = args.<String>getOne("permission").get();
        Optional<String> target = args.<String>getOne("target");

        final Subject subject;
        if(!target.isPresent())
            subject = service.getDefaults();
        else
        {
            SubjectCollection collection = service.getKnownSubjects().get(target.get());
            if(collection == null)
            {
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_COLLECTION, target.get())));
                return CommandResult.empty();
            }
            subject = collection.getDefaults();
        }

        final SubjectData data = subject.getSubjectData();

        final Function<Void, Boolean> transaction;
        final CommandResult.Builder result = CommandResult.builder();

        StringBuilder perm = new StringBuilder("permission.permd.");

        Optional<Function<Void, Boolean>> optional =
                Misc.functionOrdinaryPermissionOperationWithMessage(
                        src,
                        locale,
                        operation,
                        subject,
                        permission,
                        result
                );

        if(!optional.isPresent())
            return CommandResult.empty();

        transaction = optional.get();

        perm.append(operation);

        if(!src.hasPermission(perm.toString()))
        {
            src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_PERMISSION)));
            return CommandResult.empty();
        }

        Boolean _result = transaction.apply(null);
        if(_result != null)
            if(_result)
            {
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_SUCCEEDED)));
                result.successCount(1);
            }
            else
            {
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_FAILED)));
                result.successCount(0);
            }

        return result.build();
    }

    private final EnhancedPermissionService service;

    private final LocaleProperties locale;
}
