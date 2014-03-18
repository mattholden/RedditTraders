package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class Dispute extends RedditListener {

	public Dispute(RedditTraders rt) {
		super(rt, "DISPUTE", false);
	}

	/**
	 * Dispute that a trade was successfully completed. This will notify the
	 * subreddit's moderators.
	 * 
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

		if (tokens.length < 2) {
			help(pm, tokens, sb);
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

		StringBuffer comments = new StringBuffer();
		for (int i = 2; i < tokens.length; i++) {
			comments.append(tokens[i] + " ");
		}

		// get the details
		String user1 = "", user2 = null, url = "", subreddit = "";
		int status = -1;
		PreparedStatement p1 = config.getJDBC().prepareStatement("select * from redditors  join trades on (trades.redditorid1 = redditors.redditorid) join subreddits on (subreddits.redditid = trades.subredditid) where tradeid = ?;");
		p1.setInt(1, id);
		ResultSet rs1 = p1.executeQuery();
		if (rs1.first()) {
			user1 = rs1.getString("username");
			subreddit = rs1.getString("subreddit");
			url = rs1.getString("threadurl");
			status = rs1.getInt("status");
		} else {
			sb.append("DISPUTE error: An unknown database error has occurred.\n\n\n");
			rs1.close();
			return;
		}
		rs1.close();

		if (status == 2 || status == 3) {
			sb.append("DISPUTE error: Trade #" + id + " is already complete and may not be modified.\n\n\n");
			return;
		}

		// now we still need to get user #2
		PreparedStatement p2 = config.getJDBC().prepareStatement("select * from redditors where redditorid = (select redditorid2 from trades where tradeid = ?);");
		p2.setInt(1, id);
		ResultSet rs2 = p2.executeQuery();
		if (!rs2.first()) {
			rs2.close();
			sb.append("DISPUTE error: An unknown database error has occurred.\n\n\n");
			return;
		}
		user2 = rs2.getString("username");
		if (!pm.getAuthor().equalsIgnoreCase(user2)) {
			sb.append("DISPUTE error: Only the user who is is indicated as the trading partner in a trade may dispute it.\n\n\n");
			return;
		}

		// update the DB
		PreparedStatement ps = config.getJDBC().prepareStatement("update trades set status = 4 where tradeid = ?;");
		ps.setInt(1, id);
		ps.execute();

		// send messages to both users and the mods.
		String message = "Trade #" + id + " between " + user1 + " and " + user2 + " (" + url + ") has been disputed. The moderators of /r/" + subreddit + " have been notified, and will respond shortly. Thanks for using RedditTraders and being a part of the /r/" + subreddit + " community!";
		StringBuffer m = new StringBuffer(message);
		instance.sendMessage(user1, "Trade Disputed", m);
		sb.append(message + "\n\n\n");
		instance.sendMessage("/r/" + subreddit, "Trade Disputed", m);
	}

}
