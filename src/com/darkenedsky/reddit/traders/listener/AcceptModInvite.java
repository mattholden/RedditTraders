package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;
import com.omrlnr.jreddit.utils.Utils;

public class AcceptModInvite extends RedditListener {

	public AcceptModInvite(RedditTraders rt) {
		super(rt, "**GADZOOKS!", false);
	}

	/**
	 * Parses a message inviting the bot to become moderator of a subreddit, and
	 * accepts it
	 * 
	 * 
	 * @param pm
	 *            The private message received from the user.
	 * @param tokens
	 *            The individual "words" of the command we are executing
	 * 
	 * @param response
	 *            Unused.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer response) throws Exception {
		if (tokens.length < 10) {
			return;
		}
		String subreddit = tokens[9];
		subreddit = subreddit.substring(4);
		subreddit = subreddit.substring(0, subreddit.length() - 1);
		Utils.post("uh=" + config.getBotUser().getModhash() + "&r=" + subreddit, new URL("http://www.reddit.com/api/accept_moderator_invite"), config.getBotUser().getCookie());

	}
}
