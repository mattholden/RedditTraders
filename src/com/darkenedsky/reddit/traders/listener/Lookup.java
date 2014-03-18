package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class Lookup extends RedditListener {

	public Lookup(RedditTraders rt) {
		super(rt, "LOOKUP", false);
	}

	/**
	 * Looks up a given user's feedback score and renders it.
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
			help(msg, tokens, sb);
			return;
		}

		String user = tokens[1];
		if (user.equalsIgnoreCase(config.getBotUser().getUsername())) {
			PreparedStatement ps = config.getJDBC().prepareStatement("select count(tradeid) as trades from trades;");
			int everyTrade = 0;
			ResultSet foo = ps.executeQuery();
			if (foo.first()) {
				everyTrade = foo.getInt("trades");
			}
			foo.close();
			sb.append("Thanks for asking about me!\n\nWhile I don't have anything of my own to trade, I've participated in " + everyTrade + " trades so far.\n\nAlso, I'm a robot, so I'm perfectly reliable and never make mistakes.\n\n*And if I did, I wouldn't tell you, now would I?*\n\n\n");
			return;
		}
		HashMap<String, Integer> successful = new HashMap<String, Integer>();
		HashMap<String, Integer> unsuccessful = new HashMap<String, Integer>();
		HashMap<String, Integer> blamed = new HashMap<String, Integer>();
		HashMap<String, Integer> totals = new HashMap<String, Integer>();

		int uid = -1;

		PreparedStatement ps = config.getJDBC().prepareStatement("select redditorid from redditors where username ilike ?;");
		ps.setString(1, user);
		ResultSet uzer = ps.executeQuery();
		if (uzer.first()) {
			uid = uzer.getInt("redditorid");
		}
		uzer.close();

		if (uid == -1) {
			sb.append("Redditor /u/" + user + " is not found for LOOKUP; he or she may not have ever executed a trade through RedditTraders.\n\n\n");
			return;
		}

		PreparedStatement ps2 = config.getJDBC().prepareStatement("select subreddit, get_trade_count(?, redditid) as successful, get_unsuccessful_count(?, redditid) as unsuccessful, get_blame_count(?, redditid) as blamed from subreddits order by subreddit;");
		ps2.setInt(1, uid);
		ps2.setInt(2, uid);
		ps2.setInt(3, uid);
		ResultSet tradez = ps2.executeQuery();
		int linetotal = 0;
		if (tradez.first()) {
			while (true) {
				String sub = tradez.getString("subreddit");
				int success = tradez.getInt("successful");
				int unsuccess = tradez.getInt("unsuccessful");
				int blame = tradez.getInt("blamed");
				successful.put(sub, success);
				unsuccessful.put(sub, unsuccess);
				blamed.put(sub, blame);
				linetotal = success + unsuccess;
				totals.put(sub, linetotal);

				if (tradez.isLast())
					break;
				tradez.next();
			}
		}
		tradez.close();

		if (totals.isEmpty()) {
			sb.append("Redditor /u/" + user + " has never executed a trade through RedditTraders.\n\n\n");
			return;
		}

		int totalSuccess = 0, totalUnsuccess = 0, totalBlame = 0, totalTotal = 0;
		ArrayList<String> subs = new ArrayList<String>(successful.keySet().size());

		for (String s : successful.keySet()) {
			subs.add(s.toLowerCase());
			totalSuccess += successful.get(s);
			totalUnsuccess += unsuccessful.get(s);
			totalBlame += blamed.get(s);
			totalTotal += totals.get(s);
		}

		// make sure the subreddits appear in alphabetical order
		Collections.sort(subs);

		sb.append("RedditTraders trade history for user /u/" + user + ":\n\n------------------------------------------------\n\n");
		for (String s : subs) {
			sb.append("/r/" + s + " : ");
			sb.append(successful.get(s) + " successful (" + instance.renderPct(successful.get(s), totals.get(s)) + "), ");
			sb.append(unsuccessful.get(s) + " unsuccessful (" + instance.renderPct(unsuccessful.get(s), totals.get(s)) + "), ");
			sb.append(blamed.get(s) + " at-fault (" + instance.renderPct(blamed.get(s), totals.get(s)) + ")\n\n");
		}
		sb.append("------------------------------------------------------\n\nTotal successful trades: " + totalTotal + " (" + instance.renderPct(totalSuccess, totalTotal) + ")\n\n");
		sb.append("Total unsuccessful trades: " + totalUnsuccess + " (" + instance.renderPct(totalUnsuccess, totalTotal) + ") \n\n");
		sb.append("Total at-fault unsuccessful trades: " + totalBlame + " (" + instance.renderPct(totalBlame, totalTotal) + ") \n\n");

	}

}
