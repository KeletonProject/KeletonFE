package org.kucro3.frontend.auth.cause;

import org.kucro3.keleton.cause.FromCommand;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

public class AuthCommandCauseImpl extends AuthCauseImpl implements FromCommand {

	public AuthCommandCauseImpl(Player source, CommandContext args, String... possibleAliases)
	{
		super(source);
		this.args = args;
		this.possibleAliases = possibleAliases;
	}

	@Override
	public CommandContext getArguments() 
	{
		return this.args;
	}

	@Override
	public String[] possibleAliases() 
	{
		return this.possibleAliases;
	}

	private final CommandContext args;
	
	private final String[] possibleAliases;
}