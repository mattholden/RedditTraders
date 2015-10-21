package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;
import com.omrlnr.jreddit.user.User;
import com.omrlnr.jreddit.user.UserInfo;
import com.omrlnr.jreddit.utils.Utils;

public class Trade extends RedditListener {

	public Trade(RedditTraders rt) {
		super(rt, "TRADE", false);
	}

	/**
	 * Initiate a trade confirmation between two Redditors.
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
	 * @throws ParseException
	 * @throws SQLException
	 */
	@Override
	public void process(PrivateMessage msg, String[] tokens, StringBuffer sb) throws Exception {
		if (tokens.length < 3) {
			help(msg, tokens, sb);
			return;
		}

		String sender = msg.getAuthor();
		String tradeWith = tokens[1];
		String url = tokens[2];

		StringBuffer comments = new StringBuffer();
		if (tokens.length > 3) {
			for (int i = 3; i < tokens.length; i++) {
				comments.append(tokens[i] + " ");
			}
		}

		/* Now, let's do some safety checks... */
		// Make sure you aren't doing anything truly silly.
		if (sender.equalsIgnoreCase(tradeWith)) {
			sb.append("TRADE error: You cannot trade with yourself.\n\n\n");
			return;
		} else if (tradeWith.equalsIgnoreCase(config.getBotUser().getUsername())) {
			sb.append("I'd love to trade with you, " + sender + ", but I doubt I have anything you want. I'm just a robot, after all.\n\n\n");
			return;
		}

		// Make sure the URL is in fact a reddit URL.
		String _url = url.toLowerCase();
		if (!url.startsWith("http://www.reddit.com/r/") && !url.startsWith("https://www.reddit.com/r/")) {
			sb.append("TRADE error: You must provide a valid Reddit thread URL on a subreddit that this bot monitors.\n\n\n");
			return;
		}

		// get the name of the subreddit it was posted to.
		String[] urlTokens = _url.split("/");
		if (urlTokens.length < 7 || !urlTokens[5].equals("comments")) {
			sb.append("TRADE error: You must provide a valid Reddit thread URL on a subreddit that this bot monitors.\n\n\n");
			return;
		}
		String subreddit = urlTokens[4];

		// see if the page is actually even there
		if (!Utils.isThere(new URL(url))) {
			sb.append("TRADE error: You must provide a valid Reddit thread URL on a subreddit that this bot monitors.\n\n\n");
			return;
		}

		if (!validateComments(url, msg.getAuthor(), tradeWith)) {
			sb.append("TRADE error: The provided thread does not include a conversation between the trade partners.\n\n\n");
			return;
		}
		instance.log("Comments validated...");

		long minAccountAge = 0;
		boolean requireVerified = false;
		int checkBan = -1;
		long msecBetween = -1;

		// make sure the row is there and active
		PreparedStatement act = config.getJDBC().prepareStatement("select * from subreddits where activesub=true and subreddit ilike ?;");
		act.setString(1, subreddit);
		ResultSet actsub = act.executeQuery();
		if (!actsub.first()) {
			actsub.close();
			sb.append("TRADE error: This bot is not currently configured to actively monitor trades on subreddit /r/" + subreddit + ".\n\n\n");
			return;
		} else {
			minAccountAge = actsub.getLong("min_account_age_sec");
			requireVerified = actsub.getBoolean("require_verified_email");
			checkBan = actsub.getInt("checkban");
			msecBetween = actsub.getInt("daysbetween") * (60 * 60 * 24 * 1000);
		}
		actsub.close();

		// Make sure we're a moderator of the subreddit
		if (!instance.botIsModerator(subreddit)) {
			sb.append("TRADE error: This bot is not currently configured to actively monitor trades on subreddit /r/" + subreddit + ".\n\n\n");
			return;
		}

		// check account age of the requestor.
		UserInfo info = User.about(msg.getAuthor());
		long created = (long) info.getCreatedUTC();
		long age = System.currentTimeMillis() - created;
		long ageSec = age / 1000;
		boolean verified = info.isVerifiedEmail();

		// ignoring difference between UTC and our timezone; this is close
		// enough
		if (ageSec < minAccountAge) {
			long days = (((minAccountAge / 24) / 60) / 60);
			sb.append("TRADE error: Your account must be at least " + days + " days old to register trades on subreddit /r/" + subreddit + ".\n\n\n");
			return;
		}

		if (!verified && requireVerified) {
			sb.append("TRADE error: Accounts must have verified email addresses on Reddit to register trades on subreddit /r/" + subreddit + ".\n\n\n");
			return;
		}

		instance.log("Checking bans...");

		// see if either user is banned
		if (instance.checkBans(msg.getAuthor(), tradeWith, subreddit, checkBan)) {
			sb.append("One or more of the users in this transaction has been banned from trading in /r/" + subreddit + ".\n\n\n");
			return;
		}
		instance.log("Checked bans...");

		// see if the users already have a pending trade between them
		PreparedStatement alreadyTrading = config.getJDBC().prepareStatement("select * from trades where (redditorid1 in (select redditorid from redditors where username ilike ? or username ilike ?) and redditorid2 in (select redditorid from redditors where username ilike ? or username ilike ?)) and subredditid = (select redditid from subreddits where subreddit ilike ?) and (status = 1 or threadurl = ?);");
		alreadyTrading.setString(1, msg.getAuthor());
		alreadyTrading.setString(2, tradeWith);
		alreadyTrading.setString(3, msg.getAuthor());
		alreadyTrading.setString(4, tradeWith);
		alreadyTrading.setString(5, subreddit);
		alreadyTrading.setString(6, url);
		ResultSet setAT = alreadyTrading.executeQuery();
		if (setAT.first()) {
			setAT.close();
			sb.append("TRADE error: You either already have an open trade with " + tradeWith + " on subreddit /r/" + subreddit + ", or you have already traded with this redditor on this thread once.\n\n\n");
			return;
		} else {
			setAT.close();
		}

		instance.log("Checking last trade...");
		// see if the users too recently traded with each other
		PreparedStatement lastTrading = config.getJDBC().prepareStatement("select max(trade_date) as most_recent from trades where (redditorid1 in (select redditorid from redditors where username ilike ? or username ilike ?) and redditorid2 in (select redditorid from redditors where username ilike ? or username ilike ?)) and subredditid = (select redditid from subreddits where subreddit ilike ?);");
		lastTrading.setString(1, msg.getAuthor());
		lastTrading.setString(2, tradeWith);
		lastTrading.setString(3, msg.getAuthor());
		lastTrading.setString(4, tradeWith);
		lastTrading.setString(5, subreddit);
		ResultSet setLT = lastTrading.executeQuery();
		java.sql.Date lastTrade = null;
		if (setLT.first()) {
			lastTrade = setLT.getDate("most_recent");
			setLT.close();
			if (lastTrade != null) {
				long msSince = System.currentTimeMillis() - lastTrade.getTime();
				if (msSince < msecBetween) {
					sb.append("TRADE error: You have traded with /u/" + tradeWith + " on subreddit /r/" + subreddit + " too recently. Please wait to confirm this trade.\n\n\n");
					return;
				}
			}
		} else {
			setLT.close();
		}
		instance.log("Checked last trade. inserting...");

		// Do the insert
		PreparedStatement ps3 = this.config.getJDBC().prepareStatement("select * from insert_trade(?,?,?,?,?);");
		ps3.setString(1, sender);
		ps3.setString(2, tradeWith);
		ps3.setString(3, subreddit);
		ps3.setString(4, url);
		ps3.setString(5, comments.toString());
		ResultSet set3 = ps3.executeQuery();
		if (!set3.first()) {
			set3.close();
			sb.append("TRADE error: An unknown database error has occurred.\n\n\n");
			return;
		}
		int tradeid = set3.getInt("insert_trade");
		set3.close();

		// Notify the recipient that he has an action to complete
		StringBuffer toUser2 = new StringBuffer();
		toUser2.append("You've just made a successful trade!\n\nCongratulations! Redditor /u/");
		toUser2.append(msg.getAuthor() + " has indicated that the two of you have just completed a successful swap on /r/");
		toUser2.append(subreddit + ". The trade thread can be found at: ");
		toUser2.append(url);
		toUser2.append("\n\nTo ensure that both of you get credit (and flair, where applicable) for this trade, it will need to be confirmed. If you agree that the trade was successful, please reply to this message with the following:\n\n");
		toUser2.append("*CONFIRM " + tradeid + "*\n\n");
		toUser2.append("You can add any comments you like after the trade number, but the message MUST begin with CONFIRM and the number. Any comments you make will be saved with the trade in the RedditTraders database.\n\n");
		toUser2.append("If, for some reason, you dispute the success of this trade, you can indicate this by replying with the following message instead:\n\n");
		toUser2.append("*DISPUTE " + tradeid + "*\n\n");
		toUser2.append("As with CONFIRM, you can add any comments you like after the trade number. If you DISPUTE a successful trade, the moderators of /r/" + subreddit);
		toUser2.append(" will be notified, so that they can settle any dispute that exists.\n\nIf you have any questions, reply to this message with simply the word HELP, and I will provide you with a list of commands I support.\n\n");
		toUser2.append("Thanks for being an active member of /r/" + subreddit + "!");
		instance.sendMessage(tradeWith, "Did you complete a trade on /r/" + subreddit + "?", toUser2);

		// notify the sender that the command was executed successfully
		sb.append("A confirmation message has been sent to /u/" + tradeWith + ". Once he/she confirms this trade, your successful trade will be recorded.\n\nYour trade ID for this trade is: " + tradeid + "\n\n");
		sb.append("Thanks for being an active member of /r/" + subreddit + "!\n\n\n");
	}

	private boolean validateComments(String url, String user1, String user2) throws Exception {
		String commentsurl = url;
		if (commentsurl.endsWith("/")) {
			commentsurl.substring(0, commentsurl.length() - 1);
		}
		commentsurl += ".json";
		JSONArray commentsJSON = (JSONArray) Utils.get("", new URL(commentsurl), config.getBotUser().getCookie());

		try {
			// instance.dump(commentsJSON);
			JSONObject c1 = (JSONObject) commentsJSON.get(1);
			JSONObject d1 = (JSONObject) c1.get("data");
			Object kids = d1.get("children");
			String commentz = kids.toString();
			commentz = commentz.toLowerCase();
			instance.log("COMMENTZ: " + commentz);

			String author1 = "\"author\":\"" + user1.toLowerCase() + "\"";
			String author2 = "\"author\":\"" + user2.toLowerCase() + "\"";
			instance.log(author1);
			instance.log(author2);

			if (commentz.indexOf(author1) == -1 || commentz.indexOf(author2) == -1) {
				return false;
			}
		} catch (Exception x) {
			x.printStackTrace();
			throw x;
		}
		return true;
	}
}
