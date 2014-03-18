package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetBlameBan extends RedditListener {

	public SetBlameBan(RedditTraders rt) {
		super(rt, "SETBLAMEBAN", true);
	}

	/**
	 * Moderator function to set the criteria for banning a user for receiving
	 * too many blames
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
		if (tokens.length < 4) {
			modHelp(msg, tokens, sb);
			return;
		}
		int days = 0, blames = 0;
		try {
			blames = Integer.parseInt(tokens[2]);
			days = Integer.parseInt(tokens[3]);
		} catch (NumberFormatException x) {
			modHelp(msg, tokens, sb);
			return;
		}
		String sub = tokens[1];

		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set banblames = ?, bandays = ? where subreddit ilike ?;");
		ps.setInt(1, blames);
		ps.setInt(2, days);
		ps.setString(3, sub);
		ps.execute();

		if (blames == 0) {
			sb.append("Users of subreddit /r/" + sub + " will never be automatically banned for blames.\n\n\n");
			return;
		}

		sb.append("Users of subreddit /r/" + tokens[1] + " will now be automatically banned if they receive " + blames + " blames in " + days + " days, respective of the subreddit's COUNTALL setting.\n\n\n");

	}

}
