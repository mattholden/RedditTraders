package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;
import com.omrlnr.jreddit.utils.Utils;

public class Activate extends RedditListener {

	public Activate(RedditTraders rt) {
		super(rt, "ACTIVATE", true);
	}

	/**
	 * Activate or deactivate the bot in a particular subreddit. This will only
	 * deactivate new TRADE requests; we will allow lookups and we will allow
	 * any existing trades in progress to be completed.
	 * 
	 * 
	 * 
	 * @param msg
	 *            The private message received from the user.
	 * @param tokens
	 *            The individual "words" of the command we are executing
	 * @param sb
	 *            The StringBuffer to write any response text out to the user
	 * 
	 * @throws SQLException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	@Override
	public void process(PrivateMessage msg, String[] tokens, StringBuffer sb) throws Exception {

		if (tokens.length < 3) {
			modHelp(msg, tokens, sb);
			return;
		}
		String sub = tokens[1].toLowerCase();
		boolean activate = false;
		if (tokens[0].equalsIgnoreCase("ACTIVATE")) {
			activate = true;
		}

		// check if we're already running on this subreddit
		PreparedStatement is = config.getJDBC().prepareStatement("select * from subreddits where subreddit ilike ?;");
		is.setString(1, sub);
		ResultSet iz = is.executeQuery();
		if (!iz.first()) {
			sb.append("Error: This bot is not installed on /r/" + sub + ".\n\n\n");
			iz.close();
			return;
		}
		iz.close();

		// do the update to the subreddits table
		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set activesub = ? where subreddit ilike ?;");
		ps.setBoolean(1, activate);
		ps.setString(2, sub);
		ps.execute();

		// subscribe to the subreddit
		String u = "http://www.reddit.com/api/subscribe";
		Utils.post("uh=" + config.getBotUser().getModhash() + "&action=" + (activate ? "" : "un") + "sub&r=" + sub, new URL(u), config.getBotUser().getCookie());

		sb.append("The RedditTraders bot has been successfully " + ((activate) ? "" : "de") + "activated on subreddit /r/" + sub + ".\n\n\n");
	}
}
