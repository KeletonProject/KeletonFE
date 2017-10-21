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

public class CommandRegister implements CommandExecutor {
	public CommandRegister(AuthService service, LocaleProperties locale)
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
			String password = args.<String>getOne("password").get();
			Cause cause = Cause.source(new AuthCommandCauseImpl(player, args, "register")).build();
			AuthResult result = service.register(player.getUniqueId(), password, cause);
			
			if(result == AuthResults.PASSED)
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_REGISTER_PASSED)));
			else if(result == AuthResults.ALREADY_REGISTERED)
			{
				player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_ALREADY_REGISTERED)));
			}
			else if(result.isPassed())
				player.sendMessage(TextUtil.fromColored(String.format(locale.by(I18n.LOCALE_PASSED_WITH_EXTRA, result.getMessage().orElseGet(() -> "")))));
			else
				player.sendMessage(TextUtil.fromColored(String.format(locale.by(I18n.LOCALE_REJECTED_WITH_EXTRA, result.getMessage().orElseGet(() -> "")))));
			
			return CommandResult.success();
		} catch (AuthException e) {
			throw new CommandException(Text.builder(e.getMessage()).color(TextColors.RED).build());
		}
	}
	
	public static CommandSpec spec(AuthService service, LocaleProperties locale)
	{
		return CommandSpec.builder()
			.description(Text.of("Auth command: /register <password>"))
			.arguments(GenericArguments.onlyOne(
				GenericArguments.string(Text.of("password"))))
			.executor(new CommandRegister(service, locale))
			.build();
	}
	
	private final AuthService service;
	
	private final LocaleProperties locale;
}