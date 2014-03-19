package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class Undo extends RedditListener {

	public Undo(RedditTraders rt) {
		super(rt, "UNDO", false);
	}

	/**
	 * Completely delete a trade from the database.
	 * 
	 * @param pm
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
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {

		instance.log("Undo tokens: " + tokens.length);
		if (tokens.length != 2) {
			modHelp(pm, tokens, sb);
			return;
		}

		String eyedee = tokens[1];

		int id = -1;
		try {
			id = Integer.parseInt(eyedee);
		} catch (NumberFormatException x) {
			help(pm, tokens, sb);
			return;
		}
		boolean textFlair = false;
		String subreddit;

		PreparedStatement p1 = config.getJDBC().prepareStatement("select * from subreddits where redditid = (select subredditid from trades where tradeid = ?);");
		p1.setInt(1, id);
		ResultSet rs1 = p1.executeQuery();
		if (rs1.first()) {
			textFlair = rs1.getBoolean("textflair");
			subreddit = rs1.getString("subreddit");
		} else {
			sb.append("UNDO error: The subreddit for this trade could not be found.\n\n\n");
			rs1.close();
			return;
		}
		rs1.close();

		// Can't check the moderator status without the subreddit as a
		// parameter, which you don't have until you've done this query.
		// SO check here to be sure.
		String[] tok = { "UNDO", subreddit };
		if (!instance.senderIsModerator(pm, tok)) {
			modHelp(pm, tokens, sb);
			return;
		}

		String user1, user2;
		PreparedStatement p3 = config.getJDBC().prepareStatement("select * from redditors where redditorid = (select redditorid1 from trades where tradeid = ?) or redditorid = (select redditorid2 from trades where tradeid = ?);");
		p3.setInt(1, id);
		p3.setInt(2, id);
		ResultSet rs3 = p3.executeQuery();
		if (rs3.first()) {
			user1 = rs3.getString("username");
			rs3.next();
			user2 = rs3.getString("username");
			rs3.close();
		} else {
			rs3.close();
			sb.append("UNDO error: Could not find both redditors for this trade.\n\n\n");
			return;
		}

		PreparedStatement p2 = config.getJDBC().prepareStatement("delete from trades where tradeid = ?;");
		p2.setInt(1, id);
		p2.execute();

		// update flair
		instance.setUserFlair(user1, subreddit, textFlair);
		instance.setUserFlair(user2, subreddit, textFlair);

		String message = "Trade #" + id + " between /u/" + user1 + " and /u/" + user2 + " has been deleted. Any changes to the users' flair should be visible at this time.";
		sb.append(message + "\n\n\n");

		String message2 = "Trade #" + id + " between /u/" + user1 + " and /u/" + user2 + " has been deleted by moderator /u/" + pm.getAuthor() + ". Any changes to your flair should be visible at this time. If you have any questions regarding this action, please message the moderators of /r/" + subreddit + ".";
		instance.sendMessage(user1, "Trade " + id + " Deleted by Moderator", new StringBuffer(message2));
		instance.sendMessage(user2, "Trade " + id + " Deleted by Moderator", new StringBuffer(message2));

	}
}
