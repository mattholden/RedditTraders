package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class CountAllSubs extends RedditListener {

	public CountAllSubs(RedditTraders rt) {
		super(rt, "COUNTALL", true);
	}

	/**
	 * Moderator function to toggle whether a subreddit's trade counts should
	 * include every trade we have seen for a user and not just the trades
	 * he/she has made on this subreddit
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
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SQLException
	 * @throws ParseException
	 */
	@Override
	public void process(PrivateMessage msg, String[] tokens, StringBuffer sb) throws Exception {
		if (tokens.length < 3) {
			modHelp(msg, tokens, sb);
			return;
		}

		String onoff = tokens[2];
		boolean toggle = ("on".equals(onoff) ? true : false);

		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set count_all_subreddits = ? where subreddit ilike ?;");
		ps.setBoolean(1, toggle);
		ps.setString(2, tokens[1]);
		ps.execute();

		sb.append("Flair for subreddit /r/" + tokens[1] + " will now " + (toggle ? "" : "NOT ") + "count trades in other subreddits when determining flair.\n\n\n");

	}

}
