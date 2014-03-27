package com.darkenedsky.reddit.traders.listener;

import java.sql.PreparedStatement;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetDaysBetween extends RedditListener {

	public SetDaysBetween(RedditTraders rt) {
		super(rt, "SETDAYSBETWEEN", true);
	}

	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {
		if (tokens.length != 3) {
			modHelp(pm, tokens, sb);
			return;
		}
		int days = 0;
		try {
			days = Integer.parseInt(tokens[2]);
		} catch (NumberFormatException x) {
			modHelp(pm, tokens, sb);
			return;
		}

		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set daysbetween = ? where subreddit ilike ?;");
		ps.setLong(1, days);
		ps.setString(2, tokens[1]);
		ps.execute();

		sb.append("Updated the minimum time between trades involving the same two redditors on /r/" + tokens[1] + " to " + days + " days.\n\n");

	}

}
