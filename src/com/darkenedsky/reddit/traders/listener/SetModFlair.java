package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetModFlair extends RedditListener {

	public SetModFlair(RedditTraders rt) {
		super(rt, "SETMODFLAIR", true);
	}

	/**
	 * Moderator function to set the CSS class for a special flair to assign to
	 * moderators
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
	 * @throws SQLException
	 * @throws IOException
	 * @throws ParseException
	 */
	@Override
	public void process(PrivateMessage msg, String[] tokens, StringBuffer sb) throws Exception {

		String flair = null;

		// leave null if the command was REMOVEFLAIR - this is so we don't have
		// to have the optional flair parameter
		if (tokens[0].toUpperCase().equals("SETMODFLAIR")) {
			if (tokens.length < 3) {
				modHelp(msg, tokens, sb);
				return;
			}
			flair = tokens[2];
		} else {
			if (tokens.length < 2) {
				modHelp(msg, tokens, sb);
				return;
			}
		}

		String sub = tokens[1];
		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set modflairclass = ? where subreddit ilike ?;");
		if (flair == null)
			ps.setNull(1, Types.VARCHAR);
		else
			ps.setString(1, flair);
		ps.setString(2, sub);

		ps.execute();

		sb.append("Moderator flair for subreddit /r/" + tokens[1] + " has been successfully updated.\n\n\n");

	}

}
