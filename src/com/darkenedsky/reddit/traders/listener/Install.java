package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;
import com.omrlnr.jreddit.subreddit.Subreddit;
import com.omrlnr.jreddit.utils.Utils;

public class Install extends RedditListener {

	public Install(RedditTraders rt) {
		super(rt, "INSTALL", true);
	}

	/**
	 * Moderator function to install the bot in a new subreddit where it has
	 * never run before
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

		String sub = tokens[1].toLowerCase();

		// check if we're already running on this subreddit
		PreparedStatement is = config.getJDBC().prepareStatement("select * from subreddits where subreddit ilike ?;");
		is.setString(1, sub);
		ResultSet iz = is.executeQuery();
		if (iz.first()) {
			sb.append("INSTALL error: This bot is already installed on /r/" + sub + ".\n\n\n");
			iz.close();
			return;
		}
		iz.close();

		// do the insert to the subreddits table
		PreparedStatement ps = config.getJDBC().prepareStatement("insert into subreddits(subreddit) values (?);");
		ps.setString(1, sub);
		ps.execute();

		// subscribe to the subreddit
		Subreddit subreddit = new Subreddit();
		subreddit.setTitle(sub);

		String u = "http://www.reddit.com/api/subscribe";
		Utils.post("uh=" + config.getBotUser().getModhash() + "&action=sub&r=" + sub, new URL(u), config.getBotUser().getCookie());

		sb.append("RedditTraders has been successfully installed on subreddit /r/" + sub + ". THIS WAS A TRIUMPH.\n\n\nTo configure the bot, please use the commands listed under Moderator Commands on the HELP menu.\n\n\nRemember, in order for the bot to perform moderation tasks like assigning flair, the Reddit user /u/" + config.getBotUser().getUsername() + " must be made a moderator of /r/" + sub + ". The bot will automatically accept moderator invites it receives.\n\n\nThank you for using RedditTraders!\n\n\n");

	}
}
