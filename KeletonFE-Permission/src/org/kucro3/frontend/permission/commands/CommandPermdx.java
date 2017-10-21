package org.kucro3.frontend.permission.commands;

import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.permission.EnhancedPermissionService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class CommandPermdx implements CommandExecutor {
    public CommandPermdx(EnhancedPermissionService service, LocaleProperties locale)
    {
        this.service = service;
        this.locale = locale;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        return null;
    }

    private final EnhancedPermissionService service;

    private final LocaleProperties locale;
}
