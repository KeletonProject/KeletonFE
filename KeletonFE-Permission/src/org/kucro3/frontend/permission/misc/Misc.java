package org.kucro3.frontend.permission.misc;

import org.kucro3.frontend.permission.SpongeMain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.ProfileNotFoundException;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class Misc {
    public static Text[] showContextsOnHover(Set<Context> contexts, Text... text)
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
        item.offer(Keys.DISPLAY_NAME, Text.builder("Contexts").color(TextColors.GRAY).build());
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

    public static Text showInheritanceOnHover(Text text, String permission, boolean value)
    {
        return showInheritanceOnHover(text, permission, value, null);
    }

    public static Text showInheritanceOnHover(Text text, String permission, boolean value, String inheritance)
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
