package uk.ac.gla.spre.warmup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExtremeStartupClient {

	private static final String EXTREME_STARTUP_REGISTER_PAGE_URL_POSTFIX = "players";

	private static final String EXTREME_STARTUP_REGISTER_FORM_NAME_FIELD = "name";

	private static final String EXTREME_STARTUP_REGISTER_FORM_URL_FIELD = "url";

	private static final String EXTREME_STARTUP_WITHDRAW_PAGE_POSTFIX = "withdraw/";

	@Value("${extreme.startup.server}")
	private String extremeStartupServer;

	@Value("${extreme.startup.username}")
	private String extremeStartupUsername;

	@Value("${extreme.startup.client.hostname:#{null}}")
	private String extremeStartupClientHostname;

	@Value("${extreme.startup.maxRequestIntervalMS}")
	private long extremeStartupMaxRequestIntervalMS;

	@Value("${server.port}")
	private String serverPort;

	private static final Logger logger = LoggerFactory.getLogger(ExtremeStartupClient.class);

	private String myPlayerId;

	private Date requestLastReceivedAt;

	private TimerTask heartbeatMonitor = null;

	public void register() {
		String registrationUrl = extremeStartupServer + EXTREME_STARTUP_REGISTER_PAGE_URL_POSTFIX;
		if (isValidUrl(registrationUrl)) {
			logger.debug("REGISTERING");
			logger.debug(" - Extreme Startup server URL: " + extremeStartupServer
					+ EXTREME_STARTUP_REGISTER_PAGE_URL_POSTFIX);
			logger.debug(" - Extreme Startup username: " + extremeStartupUsername);

			myPlayerId = registerExtremeStartupPlayer(registrationUrl);
			if (null != myPlayerId) {
				logger.debug(" - Extreme Startup playerId: " + myPlayerId);
				logger.info("Extreme Startup personal log page: " + extremeStartupServer
						+ EXTREME_STARTUP_REGISTER_PAGE_URL_POSTFIX + "/" + myPlayerId);
			}
			else {
				logger.info("Unable to register; server not available?");
			}

			resetHeartbeatMonitor();
		}
		else {
			logger.debug("Not Registering, no valid Extreme Startup URL available.");
		}
	}

	private void resetHeartbeatMonitor() {
		cancelHeartbeatMonitor();

		heartbeatMonitor = new TimerTask() {
			@Override
			public void run() {
				logger.info("Extreme Startup Heartbeat Monitor: Checking registration.");
				if (!registrationStillCurrent()) {
					register();
				}

				resetHeartbeatMonitor();
			}
		};

		new Timer("HeartbeatTimer").schedule(heartbeatMonitor, 2L * extremeStartupMaxRequestIntervalMS);
	}

	private void cancelHeartbeatMonitor() {
		if (null != heartbeatMonitor) {
			heartbeatMonitor.cancel();
		}
		heartbeatMonitor = null;
	}

	public void trackRequest(String request) {
		requestLastReceivedAt = new Date();
	}

	public boolean registrationStillCurrent() {
		if (null == extremeStartupServer) {
			return false;
		}

		long timeSinceLastRequestInMilliSeconds = new Date().getTime();
		if (null != requestLastReceivedAt) {
			timeSinceLastRequestInMilliSeconds = timeSinceLastRequestInMilliSeconds - requestLastReceivedAt.getTime();
		}

		if (timeSinceLastRequestInMilliSeconds <= extremeStartupMaxRequestIntervalMS) {
			return true;
		}

		return playerIdIsStillValid();
	}

	public void withdraw() {
		String withdrawUrl = extremeStartupServer + EXTREME_STARTUP_WITHDRAW_PAGE_POSTFIX + myPlayerId;

		logger.debug("WITHDRAWING");
		if (null != myPlayerId && isValidUrl(withdrawUrl)) {
			logger.debug(" - Extreme Startup server URL: " + extremeStartupServer
					+ EXTREME_STARTUP_REGISTER_PAGE_URL_POSTFIX);
			logger.debug(" - Extreme Startup username: " + extremeStartupUsername);
			logger.debug(" - Extreme Startup playerId: " + myPlayerId);

			cancelHeartbeatMonitor();
			try {
				httpGet(withdrawUrl);
			}
			catch (Throwable t) {
				logger.info("Unable to withdraw cleanly; was player already removed?");
			}
			myPlayerId = null;
		}
		else {
			logger.debug("Not attempting withdrawal, no valid Extreme Startup URL or playerId available.");
		}
	}

	private boolean playerIdIsStillValid() {
		if (null == myPlayerId || null == extremeStartupServer) {
			return false;
		}

		String result = httpGet(extremeStartupServer + EXTREME_STARTUP_REGISTER_PAGE_URL_POSTFIX + "/" + myPlayerId);

		Pattern p = Pattern.compile(".*<title>Extreme Startup - " + extremeStartupUsername + "</title>.*");
		Matcher m = p.matcher(result);

		return m.find();
	}

	private boolean isValidUrl(String potentialUrl) {
		try {
			new URL(potentialUrl).toURI();
			return true;
		}
		catch (URISyntaxException | MalformedURLException ex) {
			return false;
		}
	}

	private String registerExtremeStartupPlayer(String registrationPageUrl) {
		String playerId = null;

		String myHostname = extremeStartupClientHostname;
		if (null == myHostname) {
			logger.info("Local client hostname not specified, trying to resolve via round-trip");
			String myIpAddress = getIpAddressFromExtremeStartupServerRegistrationPage(registrationPageUrl);
			if (null == myIpAddress) {
				logger.warn("Unable to register player; no IP address found in registration page.");
				return null;
			}
			myHostname = myIpAddress;
		}

		playerId = getPlayerIdByRegisteringWithExtremeStartupServer(registrationPageUrl, myHostname);

		return playerId;
	}

	private String getIpAddressFromExtremeStartupServerRegistrationPage(String registrationPageUrl) {
		String myIpAddress = null;

		String registrationResponse = httpGet(registrationPageUrl);
		String myIpAddressPattern = "((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}";

		Pattern p = Pattern.compile(myIpAddressPattern);
		Matcher m = p.matcher(registrationResponse);

		if (m.find()) {
			myIpAddress = m.group();
		}
		return myIpAddress;
	}

	private String getPlayerIdByRegisteringWithExtremeStartupServer(String registrationPageUrl, String myIpAddress) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> formFields = new LinkedMultiValueMap<>();
		formFields.add(EXTREME_STARTUP_REGISTER_FORM_NAME_FIELD, extremeStartupUsername);
		formFields.add(EXTREME_STARTUP_REGISTER_FORM_URL_FIELD, "http://" + myIpAddress + ":" + serverPort + "/");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(formFields,
				headers);

		String responseText = "";
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate
				.postForEntity(extremeStartupServer + EXTREME_STARTUP_REGISTER_PAGE_URL_POSTFIX, request, String.class);
			responseText = response.getBody();
		}
		catch (ResourceAccessException rae) {
			logger.debug("Unable to access Extreme Startup server; " + rae.getMessage());
			logger.trace(rae.getMessage(), rae);
		}

		String myPersonalLogPagePattern = "/([a-z0-9]{8})'";
		Pattern p = Pattern.compile(myPersonalLogPagePattern);
		assert responseText != null;
		Matcher m = p.matcher(responseText);

		if (!m.find()) {
			logger.warn("Unable to register player; no playerId found in registration form response.");
			return null;
		}

		return m.group(1);
	}

	private String httpGet(String uri) {
		RestClient restClient = RestClient.create();
		String result = "";
		try {
			result = restClient.get().uri(uri).retrieve().body(String.class);
		}
		catch (ResourceAccessException rae) {
			logger.debug(rae.getMessage());
			logger.trace(rae.getMessage(), rae);
		}
		return result;
	}

}
