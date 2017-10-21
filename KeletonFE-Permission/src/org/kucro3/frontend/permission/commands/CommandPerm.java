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
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.*;
import java.util.function.Function;

public class CommandPerm implements CommandExecutor {
    public CommandPerm(EnhancedPermissionService service, LocaleProperties locale)
    {
        this.service = service;
        this.locale = locale;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        final String operation = args.<String>getOne("operation").get();
        final String type = args.<String>getOne("type").get();
        final String permission = args.<String>getOne("argument").get();
        final String argument = args.<String>getOne("argument").get();
        final String targ = args.<String>getOne("target").get();

        final String target;

        final SubjectCollection subjects;
        final Subject subject;

        final CommandResult.Builder commandResult = CommandResult.builder();

        StringBuilder perm = new StringBuilder("permission.perm");
        Function<Void, Boolean> transaction;

        // Parsing type
        switch(type)
        {
            case "group":
                target = targ;
                subjects = service.getGroupSubjects();

                if(!subjects.hasRegistered(target))
                {
                    src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_GROUP, target)));
                    return CommandResult.empty();
                }

                break;

            case "user":
                Optional<GameProfile> optional = Misc.fromUser(targ);
                if(!optional.isPresent())
                {
                    src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_USER, targ)));
                    return CommandResult.empty();
                }

                target = optional.get().getUniqueId().toString();
                subjects = service.getUserSubjects();

                break;

            default:
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_SUBJECT_TYPE, type)));
                return CommandResult.empty();
        }

        subject = subjects.get(target);
        final SubjectData data = subject.getSubjectData();
        perm.append(".").append(type);

        // Parsing operation
        switch(operation)
        {
            case "add":
                transaction = (unused) ->
                        data.setPermission(subject.getActiveContexts(), permission, Tristate.TRUE);
                break;

            case "remove":
                transaction = (unused) ->
                        data.setPermission(subject.getActiveContexts(), permission, Tristate.UNDEFINED);
                break;

            case "forbid":
                transaction = (unused) ->
                        data.setPermission(subject.getActiveContexts(), permission, Tristate.FALSE);
                break;

            case "has":
                transaction = (unused) -> {
                    if(subject.hasPermission(permission))
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_TRUE)));
                    else
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_FALSE)));
                    commandResult.queryResult(1);
                    return null;
                };
                break;

            case "contains":
                transaction = (unused) -> {
                    Boolean value;
                    Map<String, Boolean> permissions = data.getAllPermissions().get(subject.getActiveContexts());
                    if(permissions == null || (value = permissions.get(permission)) == null)
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_EMPTY)));
                    else if(value)
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_TRUE)));
                    else
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_FALSE)));
                    commandResult.queryResult(1);
                    return null;
                };
                break;

            case "include":
                transaction = (unused) -> {
                    if(!service.getGroupSubjects().hasRegistered(argument))
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_GROUP, argument)));
                    else
                    {
                        Subject parent = service.getGroupSubjects().get(argument);
                        if(parent.isChildOf(subject))
                            src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_INHERITANCE_LOOP, argument)));
                        else if(subject.isChildOf(parent))
                            src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_IHERITANCE_EXISTS, argument)));
                        else
                            return data.addParent(subject.getActiveContexts(), parent);
                    }
                    return null;
                };
                break;

            case "exclude":
                transaction = (unused) -> {
                    if(!service.getGroupSubjects().hasRegistered(argument))
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_GROUP, argument)));
                    else
                    {
                        Subject parent = service.getGroupSubjects().get(argument);
                        if(!subject.isChildOf(parent))
                            src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_INHERITANCE_NOT_EXISTS, argument)));
                        else
                            return data.removeParent(subject.getActiveContexts(), parent);
                    }
                    return null;
                };
                break;

            default:
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPERATION, operation)));
                return CommandResult.empty();
        }
        perm.append(".").append(operation);

        if(!src.hasPermission(perm.toString()))
        {
            src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_PERMISSION)));
            return CommandResult.empty();
        }

        Boolean result = transaction.apply(null);

        if(result != null)
            if(!result)
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
