package com.darkenedsky.reddit.traders.listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class LastTrades extends RedditListener {

	private int trades;

	public LastTrades(RedditTraders rt, String command, int tradez) {
		super(rt, command, false);
		trades = tradez;
	}

	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer buffer) throws Exception {

		if (tokens.length != 2) {
			instance.log("Failed LAST10, showing help");

			help(pm, tokens, buffer);
			return;
		}
		String user = tokens[1];

		String query = "select tradeid, redditorid1, redditorid2, subredditid, trades.status, comments1, comments2, threadurl, unsuccessful_blame_redditorid, modcomments, " + "trade_date, resolve_date, r1.username as redditor1, r2.username as redditor2, sub.subreddit, stat.status as statustext " + "from trades join redditors r1 on (r1.redditorid = trades.redditorid1) join redditors r2 on (r2.redditorid = trades.redditorid2) " + " join subreddits sub on (sub.redditid = trades.subredditid) join statuscodes stat on (trades.status = stat.statusid) " + "where r1.username ilike ? or r2.username ilike ? order by trade_date desc limit ?;";

		PreparedStatement ps = config.getJDBC().prepareStatement(query);
		ps.setString(1, user);
		ps.setString(2, user);
		ps.setInt(3, trades);
		ResultSet set = ps.executeQuery();

		int actualTrades = 0;
		if (!set.first()) {
			set.close();
			buffer.append("No trades found for redditor " + user + ". If this user has trade flair, he/she most likely earned it prior to the installation of RedditTraders in this subreddit.\n\n\n");
			return;
		} else {
			StringBuffer sb = new StringBuffer();
			while (true) {
				actualTrades++;
				sb.append("Trade " + actualTrades + " : " + set.getDate("trade_date") + " ");

				String otherGuy = "";
				int userID = 0;

				Boolean gotBlamed = null;
				int blamedGuy = set.getInt("unsuccessful_blame_redditorid");
				String userComments, otherComments;

				if (set.getString("redditor1").equalsIgnoreCase(user)) {
					otherGuy = set.getString("redditor2");
					userID = set.getInt("redditorid1");
					userComments = set.getString("comments1");
					otherComments = set.getString("comments2");
				} else {
					otherGuy = set.getString("redditor1");
					userID = set.getInt("redditorid2");
					userComments = set.getString("comments2");
					otherComments = set.getString("comments1");
				}
				if (blamedGuy == userID) {
					gotBlamed = true;
				} else if (blamedGuy != 0) {
					gotBlamed = false;
				}

				sb.append(" with " + otherGuy + " on thread: " + set.getString("threadurl") + "\n");
				sb.append("Status: " + set.getString("statustext") + "\n");
				if (gotBlamed != null) {
					sb.append("Moderators assigned blame for the failed trade to " + ((gotBlamed) ? "this" : "the other") + " redditor.\nModerator comments: " + set.getString("modcomments") + "\n");
				}

				if (userComments != null && userComments.equals("") == false) {
					sb.append("\nUser comments:\n" + userComments + "\n");
				}
				if (otherComments != null && otherComments.equals("") == false) {
					sb.append("\nTrade partner comments:\n" + otherComments + "\n");
				}
				sb.append("=====================================================\n");

				if (set.isLast()) {
					break;
				}
				set.next();
			}
			set.close();

			buffer.append("Most Recent " + actualTrades + " trades for redditor /u/" + user + ":\n=======================================\n");
			buffer.append(sb.toString());
		}

	}

}
