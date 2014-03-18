package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class ViewFlair extends RedditListener {

	public ViewFlair(RedditTraders rt) {
		super(rt, "VIEWFLAIR", true);
	}

	/**
	 * Reply to the user (moderator only) with a list of the flair classes
	 * configured on this subreddit
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

		if (tokens.length < 2) {
			modHelp(msg, tokens, sb);
			return;
		}

		String sub = tokens[1];
		PreparedStatement ps = config.getJDBC().prepareStatement("select * from flairtemplates where subredditid = (select redditid from subreddits where subreddit ilike ?) order by mintrades;");
		ps.setString(1, sub);

		sb.append("The following flair templates are set for /r/" + sub + ":\n\n");
		ResultSet set = ps.executeQuery();
		if (set.first()) {
			while (true) {
				int t = set.getInt("mintrades");
				sb.append("* " + t + " trade" + ((t != 1) ? "s" : "") + ": " + set.getString("flairclass") + "\n");
				if (set.isLast())
					break;
				set.next();
			}
		}
		set.close();
		sb.append("\n\n\n");
	}
}
