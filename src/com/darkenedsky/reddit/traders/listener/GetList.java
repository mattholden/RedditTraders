package com.darkenedsky.reddit.traders.listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class GetList extends RedditListener {

	public GetList(RedditTraders rt) {
		super(rt, "WANTLIST", false);
	}

	/** Set a WANT or HAVE list. */
	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {

		if (tokens.length != 3) {
			help(pm, tokens, sb);
			return;
		}

		String column = (tokens[0].equalsIgnoreCase("WANTLIST") ? "wantlist" : "havelist");
		String listname = (tokens[0].equalsIgnoreCase("WANTLIST") ? "Want List" : "Have List");

		PreparedStatement ps = config.getJDBC().prepareStatement("select * from lists where subredditid = (select redditid from subreddits where subreddit ilike ?) and redditorid = (select redditorid from redditors where username ilike ?);");
		ps.setString(1, tokens[1]);
		ps.setString(2, tokens[2]);
		ResultSet set = ps.executeQuery();
		if (!set.first()) {
			set.close();
			sb.append("Redditor " + tokens[2] + " has not set a " + listname + " for subreddit /r/" + tokens[1] + " yet.");
			return;
		} else {
			String value = set.getString(column);
			if (value == null || "".equals(value)) {
				sb.append("Redditor " + tokens[2] + " has not set a " + listname + " for subreddit /r/" + tokens[1] + " yet.");
				return;
			}
			set.close();
			sb.append(listname + " for redditor /u/" + tokens[2] + ":\n=======================================\n");
			sb.append(value);
		}

	}

}
