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

        final String target;

        final SubjectCollection subjects;
        final Subject subject;
        final SubjectData data;

        final CommandResult.Builder commandResult = CommandResult.builder();
        final Function<Void, Boolean> transaction;

        StringBuilder perm = new StringBuilder("permission.permx");

        switch(type)
        {
            case "group":
                subjects = service.getGroupSubjects();
                target = targ;

                if(!subjects.hasRegistered(target))
                {
                    src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_GROUP, target)));
                    return CommandResult.empty();
                }
                break;

            case "user":
                subjects = service.getUserSubjects();
                Optional<GameProfile> optional = Misc.fromUser(targ);
                if(!optional.isPresent())
                {
                    src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_USER, targ)));
                    return CommandResult.empty();
                }
                target = optional.get().getUniqueId().toString();

                break;
            default:
                src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_SUBJECT_TYPE, type)));
                return CommandResult.empty();
        }

        subject = subjects.get(target);
        data = subject.getSubjectData();

        perm.append(".").append(type);

        List<Text> texts;
        boolean current = false;
        switch(operation)
        {
            case "clear":
                current = true;
            case "drop":
                switch(option)
                {
                    case "all":
                        transaction =
                                current ?
                                        (unused) -> {
                                            boolean r0 = data.clearParents(subject.getActiveContexts());
                                            boolean r1 = data.clearPermissions(subject.getActiveContexts());
                                            return r0 && r1;
                                        }
                                        :
                                        (unused) -> {
                                            boolean r0 = data.clearParents();
                                            boolean r1 = data.clearPermissions();
                                            return r0 && r1;
                                        };
                        break;

                    case "perms":
                        transaction =
                                current ?
                                        (unused) -> data.clearPermissions(subject.getActiveContexts())
                                        :
                                        (unused) -> data.clearPermissions();
                        break;

                    case "parents":
                        transaction =
                                current ?
                                        (unused) -> data.clearParents(subject.getActiveContexts())
                                        :
                                        (unused) -> data.clearParents();
                        break;

                    default:
                        src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPTION, option)));
                        return CommandResult.empty();
                }
                break;

            case "view":
                current = true;
            case "show":
                switch(option)
                {
                    case "perms":
                        texts = new ArrayList<>();
                        if(current)
                        {
                            final Map<String, Boolean> map = data.getPermissions(subject.getActiveContexts());
                            for(Map.Entry<String, Boolean> permEntry : map.entrySet())
                                texts.add(Misc.fromPermission(permEntry.getKey(), permEntry.getValue()));
                        }
                        else
                        {
                            int i = 0;

                            final Map<Set<Context>, Map<String, Boolean>> map = data.getAllPermissions();
                            for(Map.Entry<Set<Context>, Map<String, Boolean>> entry : map.entrySet())
                            {
                                Text contextOnHover = Misc.showContextsOnHover(entry.getKey(),
                                        Text.builder("(" + i + ")").color(TextColors.GRAY).build())[0];

                                for(Map.Entry<String, Boolean> permEntry : entry.getValue().entrySet())
                                    texts.add(Text.builder()
                                            .append(contextOnHover)
                                            .append(
                                                    Misc.fromPermission(permEntry.getKey(), permEntry.getValue())
                                            )
                                            .build()
                                    );
                            }
                        }

                    {
                        final PaginationList pagination = PaginationList.builder()
                                .contents(texts)
                                .padding(Text.builder("=").color(TextColors.BLUE).build())
                                .title(Text.builder("Permissions").color(TextColors.YELLOW).build())
                                .build();

                        transaction = (unused) -> {
                            pagination.sendTo(src);
                            commandResult.queryResult(1);
                            return null;
                        };
                    }
                        break;

                    case "parents":
                        texts = new ArrayList<>();
                        if(current)
                        {
                            List<Subject> list = data.getParents(subject.getActiveContexts());
                            for(Subject object : list)
                                texts.add(Text.builder(object.getIdentifier()).color(TextColors.GREEN).build());
                        }
                        else
                        {
                            int i = 0;

                            final Map<Set<Context>, List<Subject>> map = data.getAllParents();
                            for(Map.Entry<Set<Context>, List<Subject>> entry : map.entrySet())
                            {
                                Text contextOnHover = Misc.showContextsOnHover(entry.getKey(),
                                        Text.builder("(" + i + ")").color(TextColors.GREEN).build())[0];

                                for(Subject object : entry.getValue())
                                    texts.add(Text.builder()
                                            .append(contextOnHover)
                                            .append(
                                                    Text.builder(object.getIdentifier())
                                                    .color(TextColors.BLUE)
                                                    .build()
                                            )
                                            .build());
                            }
                        }

                    {
                        final PaginationList pagination = PaginationList.builder()
                                .contents(texts)
                                .padding(Text.builder("=").color(TextColors.BLUE).build())
                                .title(Text.builder("Parents").color(TextColors.YELLOW).build())
                                .build();

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
                transaction = null; // TODO
                switch(option)
                {
                    case "perms":

                        break;

                    case "parents":

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
