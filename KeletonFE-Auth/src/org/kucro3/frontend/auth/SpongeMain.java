package org.kucro3.frontend.auth;

import org.kucro3.frontend.auth.commands.CommandChangepassword;
import org.kucro3.frontend.auth.commands.CommandLogin;
import org.kucro3.frontend.auth.commands.CommandLogout;
import org.kucro3.frontend.auth.commands.CommandRegister;
import org.kucro3.frontend.auth.listener.AuthListener;
import org.kucro3.keleton.auth.AuthException;
import org.kucro3.keleton.auth.AuthKeys;
import org.kucro3.keleton.auth.AuthService;
import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.i18n.LocaleService;
import org.kucro3.keleton.implementation.InvokeOnEnable;
import org.kucro3.keleton.implementation.KeletonInstance;
import org.kucro3.keleton.implementation.KeletonModule;
import org.kucro3.keleton.keyring.ObjectService;
import org.kucro3.keleton.world.SpawnProvider;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import java.util.Locale;

@Plugin(id = "keleton-fe-auth",
		name = "keleton-fe-auth",
		version = "1.0",
		description = "Auth system frontend under KeletonFramework",
		authors = {"Kumonda221"})
@KeletonModule(name = "keleton-fe-auth",
			   dependencies = {"keletonframework", "keleton-impl-auth"})
public class SpongeMain {
	@Inject
	public SpongeMain(Logger logger)
	{
		this.logger = logger;
		instance = this;
	}
	
	public Logger getLogger()
	{
		return logger;
	}

	@InvokeOnEnable
	public void onEnable()
	{
		try {
			AuthService auth = ObjectService.get(AuthKeys.SERVICE_POOL).get().constructIfAbsent("keleton-auth");
			LocaleProperties locale = LocaleService.TOKEN.get(() -> new IllegalStateException("Locale service not available")).getProperties("keleton-auth");
			SpawnProvider spawns = SpawnProvider.TOKEN.get().get();
			CommandManager cm = Sponge.getCommandManager();
			EventManager em = Sponge.getEventManager();
			
			cm.register(this, CommandLogin.spec(auth, locale), "login");
			cm.register(this, CommandLogout.spec(auth, locale), "logout");
			cm.register(this, CommandRegister.spec(auth, locale), "register");
			cm.register(this, CommandChangepassword.spec(auth, locale), "changepassword");
			
			em.registerListeners(this, new AuthListener(auth, locale, spawns));
		} catch (AuthException e) {
			logger.error("Cannot initialize auth service!");
			e.printStackTrace();
		}
	}
	
	public static SpongeMain getInstance()
	{
		return instance;
	}
	
	private static SpongeMain instance;
	
	public static final String ID = "keleton-fe-auth";

	private final Logger logger;
}