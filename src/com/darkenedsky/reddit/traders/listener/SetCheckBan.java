package com.darkenedsky.reddit.traders.listener;

import java.sql.PreparedStatement;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetCheckBan extends RedditListener {

	public SetCheckBan(RedditTraders rt) {
		super(rt, "SETCHECKBAN", true);
	}

	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {

		if (tokens.length != 3) {
			modHelp(pm, tokens, sb);
			return;
		}

		int theMode = -1;
		String mode = tokens[2];
		if (mode.equalsIgnoreCase("none")) {
			theMode = 0;
		} else if (mode.equalsIgnoreCase("this")) {
			theMode = 1;
		} else if (mode.equalsIgnoreCase("all")) {
			theMode = 2;
		} else {
			modHelp(pm, tokens, sb);
		}

		String subreddit = tokens[1].toLowerCase();
		if (!instance.isModerator(config.getBotUser().getUsername(), subreddit)) {
			sb.append("The RedditTraders bot must be a moderator in /r/" + subreddit + " to use this command.");
		}

		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set checkban = ? where subreddit ilike ?;");
		ps.setInt(1, theMode);
		ps.setString(2, subreddit);
		ps.execute();

		sb.append("Ban verification on subreddit /r/" + subreddit + " has been successfully set to " + mode.toUpperCase() + ".\n\n\n");

	}

}
