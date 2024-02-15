package uk.ac.gla.spre.warmup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WarmupApplicationContextEventListener {

	@Autowired
	private ExtremeStartupClient extremeStartupClient;

	@EventListener
	public void handleContextRefreshedEvent(ContextRefreshedEvent ctxRefreshEvt) {
		extremeStartupClient.register();
	}

	@EventListener
	public void handleContextClosedEvent(ContextClosedEvent ctxCloseEvt) {
		extremeStartupClient.withdraw();
	}

}
