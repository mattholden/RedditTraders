package com.darkenedsky.reddit.traders.listener;

import java.sql.PreparedStatement;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetAccountAgeRequirement extends RedditListener {

	public SetAccountAgeRequirement(RedditTraders rt) {
		super(rt, "SETACCOUNTAGE", true);
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

		long sec = days * 24 * 60 * 60;
		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set min_account_age_sec = ? where subreddit ilike ?;");
		ps.setLong(1, sec);
		ps.setString(2, tokens[1]);
		ps.execute();

		sb.append("Updated the minimum account age for trading on /r/" + tokens[1] + " to " + days + " days.\n\n");

	}

}
