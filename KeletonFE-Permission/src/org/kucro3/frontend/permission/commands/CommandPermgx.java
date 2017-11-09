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
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;

import java.util.function.Function;

public class CommandPermgx implements CommandExecutor {
    public CommandPermgx(EnhancedPermissionService service, LocaleProperties locale)
    {
        this.service = service;
        this.locale = locale;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        String operation = args.<String>getOne("operation").get();
        String option = args.<String>getOne("option").get();

        final Function<Void, Boolean> transaction;
        final SubjectCollection collection = service.getGroupSubjects();
        final StringBuilder permission = new StringBuilder("permgx.");

        final CommandResult.Builder result = CommandResult.builder();

        boolean drop = false;
        switch(operation)
        {
            case "view":
                switch(option)
                {
                    case "all":
                        final PaginationList list = Misc.fromGroupsNShowOnClick(service);
                        transaction = (unused) -> {
                            list.sendTo(src);
                            return null;
                        };
                        break;

                    default:
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPTION, option)));
                        return CommandResult.empty();
                }
                break;

            case "drop":
                drop = true;
            case "clear":
                switch(option)
                {
                    case "all":
                        final boolean _drop = drop;
                        transaction = (unused) -> {
                            for(Subject subject : collection.getAllSubjects())
                            {
                                SubjectData data = subject.getSubjectData();

                                data.clearPermissions();
                                if(_drop)
                                {
                                    data.clearOptions();
                                    data.clearParents();
                                }
                            }
                            return true;
                        };
                        break;

                    default:
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPTION, option)));
                        return CommandResult.empty();
                }
                break;

            default:
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPERATION, operation)));
                return CommandResult.empty();
        }

        permission.append(operation);

        if(!Misc.checkPermission(src, locale, permission.toString()))
            return CommandResult.empty();

        Misc.computeResultWithMessage(
                src,
                locale,
                result,
                transaction.apply(null)
        );

        return result.build();
    }

    private final EnhancedPermissionService service;

    private final LocaleProperties locale;
}
