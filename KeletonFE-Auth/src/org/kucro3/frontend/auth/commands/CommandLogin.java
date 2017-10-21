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
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class CommandLogin implements CommandExecutor {
	public CommandLogin(AuthService service, LocaleProperties properties)
	{
		this.service = service;
		this.locale = properties;
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException 
	{
		if(!(src instanceof Player))
			return CommandResult.empty();
		
		Player player = (Player) src;
		
		try {
			String password = args.<String>getOne("password").get();
			Cause cause = Cause.source(new AuthCommandCauseImpl(player, args, "login")).build();
			AuthResult result = service.login(player.getUniqueId(), password, cause);
			
			if(result == AuthResults.NOT_REGISTERED)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NOT_REGISTERED)));
			else if(result == AuthResults.ALREADY_LOGGED_IN)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_ALREADY_ONLINE)));
			else if(result == AuthResults.WRONG_PASSWORD)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_WRONG_PASSWORD)));
			else if(result == AuthResults.PASSED)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_LOGIN_PASSED)));
			else if(result.isPassed())
				player.sendMessage(TextUtil.fromColored(String.format(locale.by(I18n.LOCALE_PASSED_WITH_EXTRA, result.getMessage().orElseGet(() -> "")))));
			else
				player.sendMessage(TextUtil.fromColored(String.format(locale.by(I18n.LOCALE_REJECTED_WITH_EXTRA, result.getMessage().orElseGet(() -> "")))));
			
			return CommandResult.success();
		} catch (AuthException e) {
			throw new CommandException(Text.builder(e.getMessage()).color(TextColors.RED).build());
		}
	}
	
	public static CommandSpec spec(AuthService service, LocaleProperties properties)
	{
		return CommandSpec.builder()
			.description(Text.of("Auth command: /login <password>"))
			.arguments(
				GenericArguments.onlyOne(
					GenericArguments.string(Text.of("password"))))
			.executor(new CommandLogin(service, properties))
			.build();
	}
	
	private final AuthService service;
	
	private final LocaleProperties locale;
}