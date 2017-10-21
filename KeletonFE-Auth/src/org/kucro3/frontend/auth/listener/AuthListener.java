package org.kucro3.frontend.auth.listener;

import java.util.Optional;

import org.kucro3.frontend.auth.I18n;
import org.kucro3.frontend.auth.SpongeMain;
import org.kucro3.frontend.auth.cause.AuthEventCauseImpl;
import org.kucro3.keleton.auth.AuthException;
import org.kucro3.keleton.auth.AuthService;
import org.kucro3.keleton.auth.event.TokenLoginEvent;
import org.kucro3.keleton.auth.event.TokenLogoutEvent;
import org.kucro3.keleton.cause.FromPlayer;
import org.kucro3.keleton.i18n.LocaleProperties;
import org.kucro3.keleton.text.TextUtil;
import org.kucro3.keleton.world.SpawnProvider;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.ai.AITaskEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class AuthListener {
	public AuthListener(AuthService service, LocaleProperties locale, SpawnProvider spawns)
	{
		this.service = service;
		this.locale = locale;
	}
	
	@Listener
	public void onEffect(ChangeEntityPotionEffectEvent event, @First Player player)
	{
		handle(event, (cancel) -> cancel.setCancelled(true), player);
	}
	
	@Listener
	public void onChat(MessageChannelEvent.Chat event, @First Player player)
	{
		handle(event, (cancel) -> cancel.setCancelled(true), player);
	}
	
	@Listener
	public void onCommand(SendCommandEvent event, @First Player player)
	{
		switch(event.getCommand())
		{
		case "login":
		case "logout":
		case "changepassword":
		case "register":
			break;
		default:
			handle(event, (cancel) -> cancel.setCancelled(true), player);
		}
	}
	
	@Listener
	public void onDropItem(DropItemEvent.Pre event, @First Player player)
	{
		handle(event, (cancel) -> {
			Hotbar hotbar = ((PlayerInventory) player.getInventory()).getHotbar();
			Optional<Slot> slot;
			if((slot = hotbar.getSlot(SlotIndex.of(8))).isPresent())
				if(!slot.get().peek().isPresent())
					hotbar.set(SlotIndex.of(8), event.getDroppedItems().get(0).createStack());
			cancel.setCancelled(true);
		}, player);
	}
	
	@Listener
	public void onClickInventory(ClickInventoryEvent event, @First Player player)
	{
		handle(event, (cancel) -> cancel.setCancelled(true), player);
	}
	
	@Listener
	public void onChangeInventory(ChangeInventoryEvent event, @First Player player)
	{
		handle(event, (cancel) -> {
			Hotbar hotbar = ((PlayerInventory) player.getInventory()).getHotbar();
			if(hotbar.getSelectedSlotIndex() != 8)
			{
				hotbar.setSelectedSlotIndex(8);
				event.setCancelled(true);
			}
		}, player);
	}
	
	@Listener
	public void onDamage(DamageEntityEvent event)
	{
		Entity entity = event.getTargetEntity();
		Player player = null;
		if(entity instanceof Player) try {
			if(!service.isOnline((player = (Player) entity).getUniqueId(), Cause.source(new AuthEventCauseImpl(player, event)).build()))
				event.setCancelled(true);
		} catch (AuthException e) {
			exception(e, player);
		}
	}
	
	@Listener
	public void onAITask(AITaskEvent event)
	{
		event.getTargetEntity().getTarget().ifPresent((entity) -> {
			Player player = null;
			if(entity instanceof Player) try {
				if(!service.isOnline((player = (Player) entity).getUniqueId(), Cause.source(new AuthEventCauseImpl(player, event)).build()))
					event.getTargetEntity().setTarget(null);
			} catch (AuthException e) {
				exception(e, player);
			}
		});
	}
	
	@Listener
	public void onInteract(InteractItemEvent event, @First Player player)
	{
		handle(event, (cancel) -> cancel.setCancelled(true), player);
	}
	
	@Listener
	public void onOpenInventory(InteractInventoryEvent.Open event, @First Player player)
	{
		handle(event, (cancel) -> cancel.setCancelled(true), player);
	}
	
	@Listener
	public void onLogin(TokenLoginEvent event, @First FromPlayer from)
	{
	}
	
	@Listener
	public void onLogout(TokenLogoutEvent event, @First FromPlayer from)
	{
	}
	
	@Listener
	public void onConnect(ClientConnectionEvent.Join event, @First Player player)
	{
		System.out.println(player.getIdentifier());
		((PlayerInventory) player.getInventory()).getHotbar().setSelectedSlotIndex(8);
	}
	
	@Listener
	public void onDisconnect(ClientConnectionEvent.Disconnect event, @First Player player)
	{
		try {
			service.logout(player.getUniqueId(), Cause.source(new AuthEventCauseImpl(player, event)).build());
		} catch (AuthException e) {
			exception(e, player);
		}
	}
	
	@Listener
	public void onMove(MoveEntityEvent event, @First Player player)
	{
		handle(event, (cancel) -> {
			cancel.setCancelled(true);
		}, player);
	}
	
	public void handle(Cancellable cancel, Canceller canceller, Player player)
	{
		BLOCK: try {
			Event event = (Event) cancel;
			Cause cause = Cause.source(new AuthEventCauseImpl(player, event)).build();
			if(!service.isRegistered(player.getUniqueId(), cause))
				if(cooldown()) player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NOT_REGISTERED)));
				else;
			else if(!service.isOnline(player.getUniqueId(), cause))
				if(cooldown()) player.sendMessage(TextUtil.fromColored(locale.by(I18n.LOCALE_NOT_LOGGED_IN)));
				else;
			else
				break BLOCK;
			canceller.cancel(cancel);
		} catch (AuthException e) {
			exception(e, player);
		}
	}
	
	static void exception(AuthException e, Player player)
	{
		player.sendMessage(Text.builder("Could not pass auth process, please contact your admin.").color(TextColors.RED).build());
		player.sendMessage(Text.builder("Information: " + e.getClass().getCanonicalName() + ": " + e.getMessage()).color(TextColors.RED).build());
		SpongeMain.getInstance().getLogger().error("Could not pass auth process for player: " + player.toString(), e);
	}
	
	boolean cooldown()
	{
		long time = System.currentTimeMillis();
		if(time - timestamp > 1000)
		{
			timestamp = time;
			return true;
		}
		return false;
	}
	
	public static interface Canceller
	{
		public void cancel(Cancellable cancel);
	}
	
	private long timestamp;
	
	private final AuthService service;
	
	private final LocaleProperties locale;
}