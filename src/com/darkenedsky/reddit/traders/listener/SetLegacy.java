package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetLegacy extends RedditListener {

	public SetLegacy(RedditTraders rt) {
		super(rt, "SETLEGACY", true);
	}

	/**
	 * Moderator function to set the number of legacy trades (trades completed
	 * in their subreddit by this user before the bot was in charge of
	 * monitoring trades)
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
	public void process(PrivateMessage msg, String[] tokens, StringBuffer sb) throws Exception {

		if (tokens.length < 4) {
			modHelp(msg, tokens, sb);
			return;
		}
		String sub = tokens[1];
		String user = tokens[2];
		String lt = tokens[3];
		int trades = 0;
		try {
			trades = Integer.parseInt(lt);
		} catch (NumberFormatException x) {
			modHelp(msg, tokens, sb);
			return;
		}

		PreparedStatement ps = config.getJDBC().prepareStatement("select * from set_legacy(?,?,?);");
		ps.setString(1, user);
		ps.setString(2, sub);
		ps.setInt(3, trades);
		ResultSet foo = ps.executeQuery();
		foo.close();

		// update the user's flair
		PreparedStatement p2 = config.getJDBC().prepareStatement("select * from subreddits where subreddit ilike ?;");
		p2.setString(1, sub);
		ResultSet r2 = p2.executeQuery();
		if (!r2.first()) {
			sb.append("SETLEGACY error: RedditTraders does not monitor subreddit /r/" + sub + ".\n\n");
			r2.close();
			return;
		}
		boolean doTextFlair = r2.getBoolean("textflair");
		r2.close();

		instance.setUserFlair(user, sub, doTextFlair);

		sb.append("Legacy trade count for user " + user + " has been updated to " + trades + " on subreddit /r/" + sub + ".\n\n\n");
	}

}
