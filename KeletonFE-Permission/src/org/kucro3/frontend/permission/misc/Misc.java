package org.kucro3.frontend.permission.misc;

import org.kucro3.frontend.permission.I18n;
import org.kucro3.frontend.permission.SpongeMain;
import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.text.TextUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.ProfileNotFoundException;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Tristate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public final class Misc {
    public static PaginationList fromGroupsNShowOnClick(PermissionService service)
    {
        List<Text> contents = new ArrayList<>();

        SubjectCollection groups = service.getGroupSubjects();
        for(Subject subject : groups.getAllSubjects())
        {
            Text.Builder line = Text.builder(subject.getIdentifier())
                    .color(TextColors.BLUE)
                    .style(TextStyles.UNDERLINE)
                    .onHover(
                            TextActions.showText(
                                    Text
                                            .builder("Click to get further information (Show all permissions)")
                                            .color(TextColors.GRAY)
                                            .build()
                            )
                    )
                    .onClick(
                            TextActions.runCommand(
                                    String.format("permx group showall perms %s", subject.getIdentifier())
                            )
                    );

            contents.add(line.build());
        }

        return PaginationList.builder()
                .title(Text.of("Groups"))
                .padding(Text.of("="))
                .contents(contents)
                .build();
    }

    public static Text[] showContextsOnHover(Set<Context> contexts, Text... text)
    {
        return showContextsOnHover(contexts, "Context", text);
    }

    public static Text[] showContextsOnHover(Set<Context> contexts, String itemName, Text... text)
    {
        Text[] returns = new Text[text.length];

        List<Text> texts = new ArrayList<>();

        if(contexts.isEmpty())
            texts.add(EMPTY);
        else for(Context context : contexts)
            texts.add(Text.builder(context.getType()).color(TextColors.GRAY)
                    .append(SPLITTER)
                    .append(
                            Text.builder(context.getKey())
                                    .color(TextColors.GREEN)
                                    .build()
                    )
                    .append(SPLITTER)
                    .append(
                            Text.builder(context.getValue())
                                    .color(TextColors.BLUE)
                                    .build()
                    ).build());

        ItemStack item = ItemStack.builder()
                .itemType(ItemTypes.TORCH)
                .build();
        item.offer(Keys.DISPLAY_NAME, Text.builder(itemName).color(TextColors.GRAY).build());
        item.offer(Keys.ITEM_LORE, texts);

        for(int i = 0; i < text.length; i++)
            returns[i] =
                    text[i]
                            .toBuilder()
                            .onHover(
                                    TextActions.showItem(item.createSnapshot())
                            )
                            .build();

         return returns;
    }

    public static Text showOverrideOnHover(Text text, String permission, boolean value)
    {
        return text
                .toBuilder()
                .onHover(
                        TextActions.showText(
                                Text.builder("Overrided: ")
                                .color(TextColors.GRAY)
                                .append(
                                        fromPermission(permission, value)
                                )
                                .build()
                        )
                )
                .build();
    }

    public static Text showOverrideOnHover(Text text)
    {
        return text
                .toBuilder()
                .onHover(
                        TextActions.showText(
                                Text.builder("Overrided")
                                .color(TextColors.GRAY)
                                .build()
                        )
                )
                .build();
    }

    public static Text showInheritanceOnHover(Text text, String permission, boolean value)
    {
        return showInheritanceOnHover(text, permission, value, null);
    }

    public static Text showInheritanceOnHover(Text text, String permission, boolean value, @Nullable String inheritance)
    {
        return text
                .toBuilder()
                .onHover(
                        TextActions.showText(
                                Text.builder("Inherited: ")
                                .color(TextColors.GRAY)
                                .append(
                                        fromPermission(permission, value)
                                )
                                .append(
                                        inheritance != null
                                        ?
                                                Text.builder(
                                                        new StringBuilder(" (")
                                                        .append(inheritance)
                                                        .append(")")
                                                        .toString()
                                                )
                                                .color(TextColors.GRAY)
                                                .build()
                                        :
                                                Text.of()
                                )
                                .build()
                        )
                )
                .build();
    }

    public static Text showInheritanceOnHover(Text text, String inheritance)
    {
        return text
                .toBuilder()
                .onHover(
                        TextActions.showText(
                                Text.builder("Inherited: ")
                                .color(TextColors.GRAY)
                                .append(
                                        Text.builder(inheritance)
                                        .color(TextColors.GRAY)
                                        .build()
                                )
                                .build()
                        )
                )
                .build();
    }

    private static void asRoot(InheritanceTree.Builder builder)
    {
        builder
                .content(Text.of())
                .rawContexts(Collections.emptySet())
                .rawContent("");
    }

    public static Text fromPermission(Text symbolTrue, Text symbolFalse, String permission, boolean value)
    {
        Text.Builder builder = Text.builder();

        if(value)
            builder.append(
                    symbolTrue
                    .toBuilder()
                    .build()
            );
        else
            builder.append(
                    symbolFalse
                    .toBuilder()
                    .build()
            );

        builder.append(
                Text.builder(permission)
                .color(value ? symbolTrue.getColor() : symbolFalse.getColor())
                .build()
        );

        return builder.build();
    }

    public static PaginationList fromPermissionsWithCurrentContextsSwitched(Subject subject,
                                                                           boolean plain,
                                                                           boolean flag)
    {
        return flag ?
                fromPermissionsWithCurrentContexts(subject, plain) :
                fromPermissions(subject, plain);
    }

    public static PaginationList fromPermissionsWithCurrentContexts(Subject subject,
                                                                    boolean plain)
    {
        return fromPermissions(subject, plain, PermissionFilter.withContexts(subject.getActiveContexts()));
    }

    public static PaginationList fromPermissions(Subject subject,
                                                 PermissionFilter... filters)
    {
        return fromPermissions(subject, false, filters);
    }

    public static PaginationList fromPermissions(Subject subject,
                                                 boolean plain,
                                                 PermissionFilter... filters)
    {
        Map<Set<Context>, Map<String, Boolean>> overrides = new HashMap<>();
        Map<Set<Context>, Text> subjectContextSymbols = new HashMap<>();
        Map<Set<Context>, Text> permissionContextSymbols = new HashMap<>();
        Increment increment = new Increment();
        InheritanceTree.Builder root = InheritanceTree.builder(true);

        asRoot(root);

        Repeat repeat = Repeat.empty('>');

        InheritanceTree.Builder last = fromPermissions(
                overrides,
                permissionContextSymbols,
                increment,
                root,
                subject,
                filters
        );

        Map<Set<Context>, List<Subject>> parentMap = subject.getSubjectData().getAllParents();

        if(!(plain || parentMap.isEmpty()))
            fromPermissions(
                    overrides,
                    parentMap,
                    subjectContextSymbols,
                    permissionContextSymbols,
                    increment,
                    last,
                    repeat,
                    filters
            );

        List<Text> list = new ArrayList<>();

        root
                .build()
                .preorder(
                        list,
                        (text, raw) -> showInheritanceOnHover(
                                text,
                                raw.getRawContent(),
                                overrides.get(raw.getRawContexts()).get(raw.getRawContent())
                        ),
                        true
                );

        return PaginationList.builder()
                .title(Text.of("Permissions"))
                .padding(Text.of("="))
                .contents(list)
                .build();
    }

    private static InheritanceTree.Builder fromPermissions(Map<Set<Context>, Map<String, Boolean>> overrides,
                                                           Map<Set<Context>, Text> contextSymbols,
                                                           Increment increment,
                                                           InheritanceTree.Builder builder,
                                                           Subject subject,
                                                           PermissionFilter[] filters)
    {
        Map<Set<Context>, Map<String, Boolean>> map = subject.getSubjectData().getAllPermissions();

        if(map.isEmpty())
            return builder;

        InheritanceTree.Builder last = builder;

        for(Map.Entry<Set<Context>, Map<String, Boolean>> entry : map.entrySet())
        {
            if(entry.getValue().isEmpty())
                continue;

            Text contextSymbol = fromContexts(
                    entry.getKey(),
                    null,
                    contextSymbols,
                    increment
            );

            for(Map.Entry<String, Boolean> permEntry : entry.getValue().entrySet())
            {
                if(matches(
                        subject,
                        entry.getKey(),
                        permEntry.getKey(),
                        Tristate.fromBoolean(permEntry.getValue()),
                        filters
                ))
                    last = builder
                            .child()
                            .contextSymbol(contextSymbol)
                            .rawContent(permEntry.getKey())
                            .rawContexts(entry.getKey())
                            .content(
                                    fromPermission(
                                            permEntry.getKey(),
                                            permEntry.getValue()
                                    )
                            );

                recordOverride(
                        overrides,
                        entry.getKey(),
                        permEntry.getKey(),
                        permEntry.getValue()
                );
            }
        }

        return last;
    }

    private static void fromPermissions(Map<Set<Context>, Map<String, Boolean>> overrides,
                                        Map<Set<Context>, List<Subject>> parentMap,
                                        Map<Set<Context>, Text> subjectContextSymbols,
                                        Map<Set<Context>, Text> permissionContextSymbols,
                                        Increment increment,
                                        InheritanceTree.Builder builder,
                                        Repeat repeat,
                                        PermissionFilter[] filters)
    {
        for(Map.Entry<Set<Context>, List<Subject>> parentEntry : parentMap.entrySet())
        {
            Text contextSymbolLeft = fromContexts(
                    parentEntry.getKey(),
                    "Subject contexts",
                    subjectContextSymbols,
                    increment
            );

            repeat = repeat.increase();

            for(Subject subject : parentEntry.getValue())
            {
                Text inheritanceSymbol = fromInheritance(subject, repeat);
                InheritanceTree.Builder last = builder;

                Map<Set<Context>, Map<String, Boolean>> permsMap = subject.getSubjectData().getAllPermissions();
                for(Map.Entry<Set<Context>, Map<String, Boolean>> permsEntry : permsMap.entrySet())
                {
                    Text contextSymbolRight = fromContexts(
                            permsEntry.getKey(),
                            "Permission contexts",
                            permissionContextSymbols,
                            increment
                    );

                    Text contextSymbol = Text
                            .builder()
                            .append(contextSymbolLeft)
                            .append(contextSymbolRight)
                            .build();

                    for(Map.Entry<String, Boolean> permEntry : permsEntry.getValue().entrySet())
                    {
                        Text content = fromPermission(permEntry.getKey(), permEntry.getValue());

                        if(matches(
                                subject,
                                permsEntry.getKey(),
                                permEntry.getKey(),
                                Tristate.fromBoolean(permEntry.getValue()),
                                filters
                        ))
                            last = builder
                                    .child()
                                    .rawContent(permEntry.getKey())
                                    .rawContexts(permsEntry.getKey())
                                    .content(content)
                                    .contextSymbol(contextSymbol)
                                    .inheritanceSymbol(inheritanceSymbol);

                        recordOverride(
                                overrides,
                                permsEntry.getKey(),
                                permEntry.getKey(),
                                permEntry.getValue()
                        );
                    }
                }

                Map<Set<Context>, List<Subject>> subjectParentMap = subject.getSubjectData().getAllParents();

                if(subjectParentMap.isEmpty())
                    continue;

                fromPermissions(
                        overrides,
                        subjectParentMap,
                        subjectContextSymbols,
                        permissionContextSymbols,
                        increment,
                        last,
                        repeat,
                        filters
                );
            }
        }
    }

    public static PaginationList fromParentsWithCurrentContextsSwitched(Subject subject,
                                                                        boolean plain,
                                                                        boolean flag)
    {
        return flag ?
                fromParentsWithCurrentContexts(subject, plain) :
                fromParents(subject, plain);
    }

    public static PaginationList fromParentsWithCurrentContexts(Subject subject,
                                                                boolean plain)
    {
        return fromParents(subject, plain, ParentFilter.withContexts(subject.getActiveContexts()));
    }

    public static PaginationList fromParents(Subject subject,
                                             ParentFilter... filters)
    {
        return fromParents(subject, false);
    }

    public static PaginationList fromParents(Subject subject,
                                             boolean plain,
                                             ParentFilter... filters)
    {
        List<Text> list = new ArrayList<>();

        InheritanceTree.Builder builder = InheritanceTree.builder(true);

        asRoot(builder);

        fromParents(
                subject,
                subject.getSubjectData().getAllParents(),
                new HashMap<>(),
                new Increment(),
                null,
                builder,
                Repeat.empty('>'),
                plain,
                filters
                );

        builder
                .build()
                .preorder(
                        list,
                        (text, raw) -> showOverrideOnHover(text)
                        , true
                );

        return PaginationList.builder()
                .title(Text.of("Parents"))
                .padding(Text.of("="))
                .contents(list)
                .build();
    }

    private static void fromParents(Subject subject,
                                    Map<Set<Context>, List<Subject>> parentMap,
                                    Map<Set<Context>, Text> contextSymbols,
                                    Increment increment,
                                    Subject inheritance,
                                    InheritanceTree.Builder builder,
                                    Repeat repeat,
                                    boolean plain,
                                    ParentFilter[] filters)
    {
        for(Map.Entry<Set<Context>, List<Subject>> entry : parentMap.entrySet()) {
            Text contextSymbol = fromContexts(
                    entry.getKey(),
                    null,
                    contextSymbols,
                    increment
            );

            Text inhertianceSymbol = fromInheritance(inheritance, repeat);

            repeat = repeat.increase();

            for(Subject parent : entry.getValue()) {
                if(!matches(
                        subject,
                        entry.getKey(),
                        parent,
                        filters))
                    continue;

                InheritanceTree.Builder child = builder
                        .child()
                        .contextSymbol(contextSymbol)
                        .inheritanceSymbol(inhertianceSymbol)
                        .rawContent(parent.getIdentifier())
                        .rawContexts(entry.getKey())
                        .content(
                                Text.builder(parent.getIdentifier())
                                        .color(TextColors.BLUE)
                                        .build()
                        );

                if(plain)
                    continue;

                Map<Set<Context>, List<Subject>> nextParentMap;
                if (!(nextParentMap = parent.getSubjectData().getAllParents()).isEmpty())
                    fromParents(
                            subject,
                            nextParentMap,
                            contextSymbols,
                            increment,
                            parent,
                            child,
                            repeat,
                            false,
                            filters
                    );
            }
        }
    }

    private static void recordOverride(Map<Set<Context>, Map<String, Boolean>> overrides,
                               Set<Context> contexts,
                               String permission,
                               boolean value)
    {
        Map<String, Boolean> map = overrides.get(contexts);
        if(map == null)
            overrides.put(contexts, map = new HashMap<>());

        map.putIfAbsent(permission, value);
    }

    private static Text fromInheritance(Subject inheritance,
                                        Repeat repeat)
    {
        Text inhertianceSymbol =
                inheritance != null ?
                        showInheritanceOnHover(
                                Text
                                        .builder(repeat.toString())
                                        .color(TextColors.GRAY)
                                        .build(),
                                inheritance
                                        .getIdentifier()
                        ) : null;
        return inhertianceSymbol;
    }

    private static Text fromContexts(Set<Context> contexts,
                                     @Nullable  String name,
                                     Map<Set<Context>, Text> contextSymbols,
                                     Increment increment)
    {
        Text contextSymbol;
        if((contextSymbol = contextSymbols.get(contexts)) == null)
            contextSymbols.put(
                    contexts,
                    contextSymbol = showContextsOnHover(
                            contexts,
                            name == null ? "Contexts" : name,
                            Text
                                    .builder("(" + increment.increase().value() + ")")
                                    .color(TextColors.GRAY)
                                    .build()
                    )[0]
            );
        return contextSymbol;
    }

    public static Text fromPermission(String symbolTrue, String symbolFalse, String permission, boolean value)
    {
        return fromPermission(Text.of(symbolTrue), Text.of(symbolFalse), permission, value);
    }

    public static Text fromPermission(String permission, boolean value)
    {
        return fromPermission(
                Text.builder("+").color(TextColors.GREEN).build(),
                Text.builder("-").color(TextColors.RED).build(),
                permission,
                value);
    }

    public static Optional<GameProfile> fromUser(String name)
    {
        CompletableFuture<GameProfile> future =
                Sponge.getServer().getGameProfileManager().get(name);
        try {
            GameProfile profile = future.get();
            return Optional.of(profile);
        } catch (InterruptedException e) {
            SpongeMain.getInstance().gerLogger().error("Misc#fromUser", e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof ProfileNotFoundException)
                return Optional.empty();
            SpongeMain.getInstance().gerLogger().error("Misc#fromUser", e);
        }
        return Optional.empty();
    }

    public static Function<Void, Boolean> functionQueryHasPermission(MessageReceiver receiver,
                                                                     LocaleProperties locale,
                                                                     Subject subject,
                                                                     String permission,
                                                                     CommandResult.Builder result)
    {
        return (unused) -> {
            if(subject.hasPermission(permission))
               receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_TRUE)));
            else
                receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_FALSE)));
            result.queryResult(1);
            return null;
        };
    }

    public static Function<Void, Boolean> functionQueryContainsPermission(MessageReceiver receiver,
                                                                          LocaleProperties locale,
                                                                          Subject subject,
                                                                          String permission,
                                                                          CommandResult.Builder result)
    {
        SubjectData data = subject.getSubjectData();
        return (unused) -> {
            Boolean value;
            Map<String, Boolean> permissions = data.getAllPermissions().get(subject.getActiveContexts());
            if(permissions == null || (value = permissions.get(permission)) == null)
                receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_EMPTY)));
            else if(value)
                receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_TRUE)));
            else
                receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_QUERY_RESULT_FALSE)));
            result.queryResult(1);
            return null;
        };
    }

    public static Optional<Function<Void, Boolean>> functionOrdinaryPermissionOperationWithMessage(MessageReceiver receiver,
                                                                                                   LocaleProperties locale,
                                                                                                   String operation,
                                                                                                   Subject subject,
                                                                                                   String permission,
                                                                                                   CommandResult.Builder result)
    {
        Optional<Function<Void, Boolean>> optional =
                Optional.ofNullable(functionOrdinaryPermissionOperation0(receiver, locale, operation, subject, permission, result));
        if(!optional.isPresent())
            receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPERATION, operation)));
        return optional;
    }

    public static Optional<Function<Void, Boolean>> functionOrdinaryPermissionOperation(MessageReceiver receiver,
                                                                                        LocaleProperties locale,
                                                                                        String operation,
                                                                                        Subject subject,
                                                                                        String permission,
                                                                                        CommandResult.Builder result)
    {
        return Optional.ofNullable(functionOrdinaryPermissionOperation0(receiver, locale, operation, subject, permission, result));
    }

    private static @Nullable Function<Void, Boolean> functionOrdinaryPermissionOperation0(MessageReceiver receiver,
                                                                                          LocaleProperties locale,
                                                                                          String operation,
                                                                                          Subject subject,
                                                                                          String permission,
                                                                                          CommandResult.Builder result)
    {
        final SubjectData data = subject.getSubjectData();

        final Function<Void, Boolean> transaction;
        switch(operation)
        {
            case "add":
                transaction = (unused) ->
                        data.setPermission(subject.getActiveContexts(), permission, Tristate.TRUE);
                break;

            case "forbid":
                transaction = (unused) ->
                        data.setPermission(subject.getActiveContexts(), permission, Tristate.FALSE);
                break;

            case "remove":
                transaction = (unused) ->
                        data.setPermission(subject.getActiveContexts(), permission, Tristate.UNDEFINED);
                break;

            case "has":
                transaction = Misc.functionQueryHasPermission(
                        receiver,
                        locale,
                        subject,
                        permission,
                        result
                );
                break;

            case "contains":
                transaction = Misc.functionQueryContainsPermission(
                        receiver,
                        locale,
                        subject,
                        permission,
                        result
                );
                break;

            default:
                return null;
        }
        return transaction;
    }

    public static boolean checkPermission(CommandSource src,
                                          LocaleProperties locale,
                                          String permission)
    {
        boolean result = src.hasPermission(permission);
        if(!result)
            src.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_PERMISSION)));
        return result;
    }

    public static void computeResultWithMessage(MessageReceiver receiver,
                                                   LocaleProperties locale,
                                                   CommandResult.Builder builder,
                                                   Boolean result)
    {
        if(result != null)
            if(result)
            {
                receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_SUCCEEDED)));
                builder.successCount(1);
            }
            else
            {
                receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_FAILED)));
                builder.successCount(0);
            }
    }

    public static Optional<Subject> parseSubjectWithMessage(MessageReceiver receiver,
                                                            PermissionService service,
                                                            LocaleProperties locale,
                                                            String type,
                                                            String identifier)
    {
        return Optional.ofNullable(parseSubjectWithMessage0(receiver, service, locale, type, identifier));
    }

    private static @Nullable Subject parseSubjectWithMessage0(MessageReceiver receiver,
                                                             PermissionService service,
                                                             LocaleProperties locale,
                                                             String type,
                                                             String identifier)
    {
        String target;
        SubjectCollection subjects;
        switch(type)
        {
            case "group":
                subjects = service.getGroupSubjects();
                target = identifier;
                break;

            case "user":
                subjects = service.getUserSubjects();
                Optional<GameProfile> optional = Misc.fromUser(identifier);
                if(!optional.isPresent())
                {
                    receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NO_SUCH_USER, identifier)));
                    return null;
                }
                target = optional.get().getUniqueId().toString();

                break;
            default:
                receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_SUBJECT_TYPE, type)));
                return null;
        }

        return subjects.get(target);
    }

    public static Optional<Function<Void, Boolean>> functionDropNClearWithMessage(MessageReceiver receiver,
                                                                                  LocaleProperties locale,
                                                                                  String option,
                                                                                  Subject subject,
                                                                                  boolean current)
    {
        Optional<Function<Void, Boolean>> optional =
                Optional.ofNullable(functionDropNClear0(option, subject, current));
        if(!optional.isPresent())
            receiver.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_UNKNOWN_OPTION, option)));
        return optional;
    }

    public static Optional<Function<Void, Boolean>> functionDropNClear(String option,
                                                                       Subject subject,
                                                                       boolean current)
    {
        return Optional.ofNullable(functionDropNClear0(option, subject, current));
    }

    private static @Nullable Function<Void, Boolean> functionDropNClear0(String option,
                                                                         Subject subject,
                                                                         boolean current)
    {
        SubjectData data = subject.getSubjectData();

        Function<Void, Boolean> transaction;
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
                return null;
        }
        return transaction;
    }

    private static boolean matches(Subject subject,
                                   Set<Context> contexts,
                                   String permission,
                                   Tristate value,
                                   PermissionFilter[] filters)
    {
        for(PermissionFilter filter : filters)
            if(!filter.filter(subject, contexts, permission, value))
                return false;
        return true;
    }

    private static boolean matches(Subject subject,
                                  Set<Context> contexts,
                                  Subject parent,
                                  ParentFilter[] filters)
    {
        for(ParentFilter filter : filters)
            if(!filter.filter(subject, contexts, parent))
                return false;
        return true;
    }

    public static interface PermissionFilter
    {
        public boolean filter(Subject subject, Set<Context> contexts, String permission, Tristate value);

        public static PermissionFilter withContexts(final Set<Context> contexts)
        {
            return (s, c, p, v) -> c.equals(contexts);
        }

        public static PermissionFilter notWithContexts(final Set<Context> contexts)
        {
            return (s, c, p, v) -> !c.equals(contexts);
        }
    }

    public static interface ParentFilter
    {
        public boolean filter(Subject subject, Set<Context> contexts, Subject parent);

        public static ParentFilter withContexts(final Set<Context> contexts)
        {
            return (s, c, p) -> c.equals(contexts);
        }

        public static ParentFilter notWithContexts(final Set<Context> contexts)
        {
            return (s, c, p) -> !c.equals(contexts);
        }
    }

    private static final Text SPLITTER = Text.builder(":").color(TextColors.DARK_GRAY).build();

    private static final Text EMPTY = Text.builder("EMPTY").color(TextColors.BLUE).build();

    private Misc()
    {
    }
}
