package org.kucro3.frontend.permission.misc;

import org.kucro3.frontend.permission.SpongeMain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.ProfileNotFoundException;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class Misc {
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

    public static PaginationList fromPermissions(Subject subject)
    {
        Map<Set<Context>, Map<String, Boolean>> overrides = new HashMap<>();
        Map<Set<Context>, Text> subjectContextSymbols = new HashMap<>();
        Map<Set<Context>, Text> permissionContextSymbols = new HashMap<>();
        Increment increment = new Increment();
        InheritanceTree.Builder root = InheritanceTree.builder(true);
        Repeat repeat = Repeat.empty('>');

        InheritanceTree.Builder last = fromPermissions(
                overrides,
                permissionContextSymbols,
                increment,
                root,
                subject
        );

        Map<Set<Context>, List<Subject>> parentMap = subject.getSubjectData().getAllParents();

        if(!parentMap.isEmpty())
            fromPermissions(
                    overrides,
                    parentMap,
                    subjectContextSymbols,
                    permissionContextSymbols,
                    increment,
                    last,
                    repeat
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
                                                           Subject subject)
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
                                        Repeat repeat)
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
                        repeat
                );
            }
        }
    }

    public static PaginationList fromParents(Subject subject)
    {
        List<Text> list = new ArrayList<>();

        InheritanceTree.Builder builder = InheritanceTree.builder(true);
        fromParents(
                subject.getSubjectData().getAllParents(),
                new HashMap<>(),
                new Increment(),
                null,
                builder,
                Repeat.empty('>')
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

    private static void fromParents(Map<Set<Context>, List<Subject>> parentMap,
                                    Map<Set<Context>, Text> contextSymbols,
                                    Increment increment,
                                    Subject inheritance,
                                    InheritanceTree.Builder builder,
                                    Repeat repeat)
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

                Map<Set<Context>, List<Subject>> nextParentMap;
                if (!(nextParentMap = parent.getSubjectData().getAllParents()).isEmpty())
                    fromParents(
                            nextParentMap,
                            contextSymbols,
                            increment,
                            parent,
                            child,
                            repeat
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

    private static final Text SPLITTER = Text.builder(":").color(TextColors.DARK_GRAY).build();

    private static final Text EMPTY = Text.builder("EMPTY").color(TextColors.BLUE).build();

    private Misc()
    {
    }
}
