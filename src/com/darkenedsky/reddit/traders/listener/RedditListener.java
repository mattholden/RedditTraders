package com.darkenedsky.reddit.traders.listener;

import com.darkenedsky.reddit.traders.Configuration;
import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public abstract class RedditListener {

	private boolean requireMod = false;
	private String command;
	protected RedditTraders instance;
	protected Configuration config;

	public RedditListener(RedditTraders rt, String cmd, boolean mod) {
		instance = rt;
		config = instance.getConfig();
		requireMod = mod;
		command = cmd;
	}

	public void doCommand(PrivateMessage pm, String[] tokens, StringBuffer response) throws Exception {
		if (requireMod && !RedditTraders.instance.senderIsModerator(pm, tokens)) {
			response.append("This command is reserved for subreddit moderators.");
			return;
		}
		process(pm, tokens, response);

	}

	public String getCommand() {
		return command;
	}

	protected void help(PrivateMessage pm, String[] tokens, StringBuffer response) throws Exception {
		instance.getListener("HELP").process(pm, tokens, response);
	}

	protected void modHelp(PrivateMessage pm, String[] tokens, StringBuffer response) throws Exception {
		instance.getListener("MODHELP").process(pm, tokens, response);
	}

	public abstract void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception;

	public boolean requiresMod() {
		return requireMod;
	}

}
