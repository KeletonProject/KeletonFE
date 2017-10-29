package org.kucro3.frontend.permission.commands;

import org.kucro3.frontend.permission.I18n;
import org.kucro3.frontend.permission.misc.Misc;
import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.permission.EnhancedPermissionService;
import org.kucro3.keleton.text.TextUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.function.Function;

public class CommandPermx implements CommandExecutor
{
    public CommandPermx(EnhancedPermissionService service, LocaleProperties locale)
    {
        this.service = service;
        this.locale = locale;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        final String type = args.<String>getOne("type").get();
        final String operation = args.<String>getOne("operation").get();
        final String option = args.<String>getOne("option").get();
        final String targ = args.<String>getOne("target").get();

        final Subject subject;

        final CommandResult.Builder commandResult = CommandResult.builder();
        final Function<Void, Boolean> transaction;

        StringBuilder perm = new StringBuilder("permission.permx");

        Optional<Subject> _optional = Misc.parseSubjectWithMessage(
                src,
                service,
                locale,
                type,
                targ
        );

        if(!_optional.isPresent())
            return CommandResult.empty();

        subject = _optional.get();

        perm.append(".").append(type);

        boolean current = false;
        switch(operation)
        {
            case "clear":
                current = true;
            case "drop":
                Optional<Function<Void, Boolean>> optional = Misc.functionDropNClearWithMessage(
                        src,
                        locale,
                        option,
                        subject,
                        current
                );

                if(!optional.isPresent())
                    return CommandResult.empty();

                transaction = optional.get();
                break;

            case "view":
                current = true;
            case "show":
                switch(option)
                {
                    case "perms":
                    {
                        final PaginationList pagination = Misc.fromPermissionsWithCurrentContextsSwitched(
                                subject,
                                true,
                                current
                        );

                        transaction = (unused) -> {
                            pagination.sendTo(src);
                            commandResult.queryResult(1);
                            return null;
                        };
                    }
                        break;

                    case "parents":
                    {
                        final PaginationList pagination = Misc.fromParentsWithCurrentContextsSwitched(
                                subject,
                                true,
                                current
                        );

                        transaction = (unused) -> {
                            pagination.sendTo(src);
                            commandResult.queryResult(1);
                            return null;
                        };
                    }
                        break;

                    default:
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPTION, option)));
                        return CommandResult.empty();
                }
                break;

            case "viewall":
                current = true;
            case "showall":
                final PaginationList list;
                switch(option)
                {
                    case "perms":
                        list = Misc.fromPermissionsWithCurrentContextsSwitched(
                                subject,
                                false,
                                current
                        );
                        break;

                    case "parents":
                        list = Misc.fromParentsWithCurrentContextsSwitched(
                                subject,
                                false,
                                current
                        );
                        break;

                    default:
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPTION, option)));
                        return CommandResult.empty();
                }
                transaction = (unused) -> {
                    list.sendTo(src);
                    return null;
                };
                break;

            default:
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPERATION, operation)));
                return CommandResult.empty();
        }

        perm.append(".").append(operation).append(".").append(option);

        if(!src.hasPermission(perm.toString()))
        {
            src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_PERMISSION)));
            return CommandResult.empty();
        }

        Boolean result = transaction.apply(null);
        if(result != null)
            if(result)
            {
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_FAILED)));
                commandResult.successCount(0);
            }
            else
            {
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_SUCCEEDED)));
                commandResult.successCount(1);
            }

        return commandResult.build();
    }

    private final EnhancedPermissionService service;

    private final LocaleProperties locale;
}
