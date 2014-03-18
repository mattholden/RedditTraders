package com.darkenedsky.reddit.traders.listener;

import java.sql.PreparedStatement;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetVerifiedEmail extends RedditListener {

	public SetVerifiedEmail(RedditTraders rt) {
		super(rt, "SETVERIFIEDEMAIL", true);
	}

	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {
		if (tokens.length != 3) {
			modHelp(pm, tokens, sb);
			return;
		}
		boolean setting = false;
		try {
			int set = Integer.parseInt(tokens[2]);
			setting = (set == 0) ? false : true;
		} catch (NumberFormatException x) {
			modHelp(pm, tokens, sb);
			return;
		}

		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set require_verified_email = ? where subreddit ilike ?;");
		ps.setBoolean(1, setting);
		ps.setString(2, tokens[1]);
		ps.execute();

		sb.append("RedditTraders will now " + ((setting) ? "" : "NOT ") + " require verified emails for users on /r/" + tokens[1] + ".\n\n");

	}

}
