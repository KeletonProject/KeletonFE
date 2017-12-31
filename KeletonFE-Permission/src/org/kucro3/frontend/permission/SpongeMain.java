package org.kucro3.frontend.permission;

import com.google.inject.Inject;
import org.kucro3.frontend.permission.commands.*;
import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.i18n.LocaleService;
import org.kucro3.keleton.implementation.KeletonInstance;
import org.kucro3.keleton.implementation.KeletonModule;
import org.kucro3.keleton.permission.EnhancedPermissionService;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "keleton-fe-permission",
        name = "keleton-fe-permission",
        version = "1.0",
        description = "Permission service frontend under KeletonFramework",
        authors = "Kumonda221")
@KeletonModule(name = "keleton-fe-permission",
               dependencies = {"keletonframework", "keleton-impl-permission"})
public class SpongeMain extends KeletonInstance {
    @Inject
    public SpongeMain(Logger logger)
    {
        this.logger = logger;
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        EnhancedPermissionService service =
                EnhancedPermissionService.TOKEN.get(() -> new IllegalStateException("Permission service not reachable"));
        LocaleProperties locale =
                LocaleService.TOKEN.get(() -> new IllegalStateException("Locale service not available")).getProperties("keleton-permission");

        CommandSpec spec = CommandSpec.builder()
                .description(Text.of("Permission control"))
                .arguments(
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("type"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("operation"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("argument"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("target"))
                        )
                )
                .executor(new CommandPerm(service, locale))
                .build();
        Sponge.getCommandManager().register(this, spec, "perm");

        spec = CommandSpec.builder()
                .description(Text.of("Advanced permission control"))
                .arguments(
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("type"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("operation"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("option"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("target"))
                        )
                )
                .executor(new CommandPermx(service, locale))
                .build();
        Sponge.getCommandManager().register(this, spec, "permx");

        spec = CommandSpec.builder()
                .description(Text.of("Permission control for defaults"))
                .arguments(
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("operation"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("permission"))
                        ),
                        GenericArguments.optionalWeak(
                                GenericArguments.string(Text.of("target"))
                        )
                )
                .executor(new CommandPermd(service, locale))
                .build();
        Sponge.getCommandManager().register(this, spec, "permd");

        spec = CommandSpec.builder()
                .description(Text.of("Advanced Permission control for defaults"))
                .arguments(
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("operation"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("option"))
                        ),
                        GenericArguments.optionalWeak(
                                GenericArguments.string(Text.of("target"))
                        )
                )
                .executor(new CommandPermdx(service, locale))
                .build();
        Sponge.getCommandManager().register(this, spec, "permdx");

        spec = CommandSpec.builder()
                .description(Text.of("Advanced Permission control for permission groups"))
                .arguments(
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("operation"))
                        ),
                        GenericArguments.onlyOne(
                                GenericArguments.string(Text.of("option"))
                        )
                )
                .executor(new CommandPermgx(service, locale))
                .build();
        Sponge.getCommandManager().register(this, spec, "permgx");
    }

    public Logger gerLogger()
    {
        return this.logger;
    }

    public static SpongeMain getInstance()
    {
        return INSTANCE;
    }

    private static SpongeMain INSTANCE;

    private final Logger logger;
}
