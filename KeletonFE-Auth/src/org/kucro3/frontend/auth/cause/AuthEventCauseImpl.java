package org.kucro3.frontend.auth.cause;

import org.kucro3.keleton.cause.FromPlayerEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

public class AuthEventCauseImpl extends AuthCauseImpl implements FromPlayerEvent {
	public AuthEventCauseImpl(Player source, Event event)
	{
		super(source);
		this.event = event;
	}
	
	@Override
	public Event getEvent()
	{
		return event;
	}
	
	private final Event event;
}