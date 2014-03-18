package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;
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
		for (int i = 3; i < tokens.length; i++) {
			comments.append(tokens[i] + " ");
		}

		/* Now, let's do some safety checks... */
		// WONTDO: Make sure both are subscribers. They don't necessarily have
		// to be.
		// TODO: Make sure neither user is banned?
		// TODO: see if both users have a comment on the thread
		// TODO: Watch for repeat traders

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
		if (!url.startsWith("http://www.reddit.com/r/")) {
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

		// make sure the row is there and active
		PreparedStatement act = config.getJDBC().prepareStatement("select * from subreddits where activesub=true and subreddit ilike ?;");
		act.setString(1, subreddit);
		ResultSet actsub = act.executeQuery();
		if (!actsub.first()) {
			actsub.close();
			sb.append("TRADE error: This bot is not currently configured to actively monitor trades on subreddit /r/" + subreddit + ".\n\n\n");
			return;
		}
		actsub.close();

		// Make sure we're a moderator of the subreddit
		if (!instance.botIsModerator(subreddit)) {
			sb.append("TRADE error: This bot is not currently configured to actively monitor trades on subreddit /r/" + subreddit + ".\n\n\n");
			return;
		}

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

}
