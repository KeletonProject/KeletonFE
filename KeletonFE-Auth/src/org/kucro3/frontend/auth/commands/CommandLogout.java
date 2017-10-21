package org.kucro3.frontend.auth.commands;

import org.kucro3.frontend.auth.I18n;
import org.kucro3.frontend.auth.cause.AuthCommandCauseImpl;
import org.kucro3.keleton.auth.AuthException;
import org.kucro3.keleton.auth.AuthResult;
import org.kucro3.keleton.auth.AuthResults;
import org.kucro3.keleton.auth.AuthService;
import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.text.TextUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class CommandLogout implements CommandExecutor {
	public CommandLogout(AuthService service, LocaleProperties locale)
	{
		this.service = service;
		this.locale = locale;
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException 
	{
		if(!(src instanceof Player))
			return CommandResult.empty();
		
		Player player = (Player) src;
		
		try {
			Cause cause = Cause.source(new AuthCommandCauseImpl(player, args, "logout")).build();
			AuthResult result = service.logout(player.getUniqueId(), cause);
			
			if(result == AuthResults.PASSED)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_LOGOUT_PASSED)));
			else if(result == AuthResults.NOT_LOGGED_IN)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NOT_LOGGED_IN)));
			else if(result == AuthResults.NOT_REGISTERED)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NOT_REGISTERED)));
			else if(result.isPassed())
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_PASSED_WITH_EXTRA, result.getMessage().orElseGet(() -> ""))));
			else
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_REJECTED_WITH_EXTRA, result.getMessage().orElseGet(() -> ""))));
			
			return CommandResult.success();
		} catch (AuthException e) {
			throw new CommandException(Text.builder(e.getMessage()).color(TextColors.RED).build());
		}
	}
	
	public static CommandSpec spec(AuthService service, LocaleProperties locale)
	{
		return CommandSpec.builder()
			.description(Text.of("Auth command: /logout"))
			.executor(new CommandLogout(service, locale))
			.permission("auth.logout")
			.build();
	}
	
	private final AuthService service;
	
	private final LocaleProperties locale;
}