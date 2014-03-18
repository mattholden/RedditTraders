package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class Resolve extends RedditListener {

	public Resolve(RedditTraders rt) {
		super(rt, "RESOLVE", true);
	}

	/**
	 * Moderator function to resolve a disputed trade, optimally assigning blame
	 * to one user or the other
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
			modHelp(pm, tokens, sb);
			return;
		}

		int commentStart = 2;

		String blame = null;
		if (tokens[0].equalsIgnoreCase("BLAME")) {
			if (tokens.length < 3) {
				modHelp(pm, tokens, sb);
				return;
			}
			blame = tokens[2];
			commentStart = 3;
		}

		String eyedee = tokens[1];
		int id = -1;
		try {
			id = Integer.parseInt(eyedee);
		} catch (NumberFormatException x) {
			modHelp(pm, tokens, sb);
			return;
		}

		StringBuffer comments = new StringBuffer();
		for (int i = commentStart; i < tokens.length; i++) {
			comments.append(tokens[i] + " ");
		}
		if (comments.length() > 1024) {
			String shortened = comments.toString().substring(0, 1024);
			comments = new StringBuffer(shortened);
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
			sb.append("RESOLVE/CLOSE/BLAME error: An unknown database error has occurred.\n\n\n");
			rs1.close();
			return;
		}
		rs1.close();

		if (status != 4) {
			sb.append("RESOLVE/CLOSE/BLAME error: Trade #" + id + " is not in dispute.\n\n\n");
			return;
		}

		// now we still need to get user #2
		PreparedStatement p2 = config.getJDBC().prepareStatement("select * from redditors where redditorid = (select redditorid2 from trades where tradeid = ?);");
		p2.setInt(1, id);
		ResultSet rs2 = p2.executeQuery();
		if (!rs2.first()) {
			rs2.close();
			sb.append("RESOLVE/CLOSE/BLAME error: An unknown database error has occurred.\n\n\n");
			return;
		}
		user2 = rs2.getString("username");

		if (user1.equalsIgnoreCase(pm.getAuthor()) || user2.equalsIgnoreCase(pm.getAuthor())) {
			sb.append("RESOLVE/CLOSE/BLAME error: You may not resolve your own disputed trade.\n\n\n");
			return;
		}
		if (!instance.isModerator(pm.getAuthor(), subreddit)) {
			sb.append("RESOLVE/CLOSE/BLAME error: Only moderators of the subreddit where the trade was executed may use this function.\n\n\n");
			return;
		}

		// update the DB
		String blamer = "";
		if (blame != null) {
			blamer = ", unsuccessful_blame_redditorid = (select redditorid from redditors where username ilike ?)";
		}

		int newStatus = (tokens[0].equalsIgnoreCase("RESOLVE") ? 2 : 3);
		PreparedStatement ps = config.getJDBC().prepareStatement("update trades set resolve_date = now(), status = ?, modcomments = ?" + blamer + " where tradeid = ?;");
		ps.setInt(1, newStatus);
		ps.setString(2, comments.toString());
		if (blame != null) {
			ps.setString(3, blame);
			ps.setInt(4, id);
		} else {
			ps.setInt(3, id);
		}
		ps.execute();

		// send messages to both users and the mods.
		String blameMessage = "";
		boolean banned = false;

		if (blame != null) {
			blameMessage = "Blame for the unsuccessful trade has been assigned to redditor " + blame + ". ";
			instance.log("Banning an offending user!");
			PreparedStatement ban = config.getJDBC().prepareStatement("select * from should_ban((select redditorid from redditors where username ilike ?),(select redditid from subreddits where subreddit ilike ?)) as ban;");
			ban.setString(1, blame);
			ban.setString(2, subreddit);
			ResultSet banz = ban.executeQuery();
			if (banz.first()) {
				banned = banz.getBoolean("ban");
			}
			banz.close();

			if (banned) {
				instance.ban(blame, subreddit, "Automatic ban from RedditTraders for exceeding at-fault unsuccessful trade limit.");
				blameMessage = blameMessage + " Redditor /u/" + blame + " has been automatically banned from /r/" + subreddit + " for excessive at-fault unsuccessful trades.\n\n";
			}
		}

		String message = "Trade #" + id + " between " + user1 + " and " + user2 + " (" + url + ") has been resolved by the moderators of /r/" + subreddit + ". " + blameMessage + "The moderator's comments follow:\n\n" + comments.toString() + "\n\n\n";
		StringBuffer m = new StringBuffer(message);
		instance.sendMessage(user1, "Trade Dispute Results", m);
		instance.sendMessage(user2, "Trade Dispute Results", m);
		instance.sendMessage("/r/" + subreddit, "Trade Dispute Results", m);
	}

}
