package org.kucro3.frontend.auth.cause;

import org.kucro3.frontend.auth.SpongeMain;
import org.kucro3.keleton.cause.FromPlayer;
import org.kucro3.keleton.cause.FromPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

public class AuthCauseImpl implements FromPlugin, FromPlayer {
	public AuthCauseImpl(Player source)
	{
		this.source = source;
	}
	
	@Override
	public Player getSource() 
	{
		return this.source;
	}

	@Override
	public PluginContainer getPlugin() 
	{
		return Sponge.getPluginManager().getPlugin(SpongeMain.ID).get();
	}
	
	private final Player source;
}