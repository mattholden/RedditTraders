package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import com.darkenedsky.reddit.traders.Configuration;
import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class About extends RedditListener {

	public About(RedditTraders rt) {
		super(rt, "ABOUT", false);
	}

	/**
	 * Reply to the user with information about the bot and its author
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
	 */
	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {

		String cDate = "2013";
		int yr = Calendar.getInstance().get(Calendar.YEAR);
		if (yr != 2013) {
			cDate += "-" + yr;
		}

		Configuration config = instance.getConfig();
		sb.append("*RedditTraders Trading Bot version " + config.getVersion() + " by /u/" + config.getAuthor() + "*\n\n");
		sb.append("(C) " + cDate + " Matt Holden (matt@mattholden.com)\n\n");
		sb.append("--------------------------------------------------------------\n\n");
		sb.append("RedditTraders is free, open-source software provided under the [MIT License](http://opensource.org/licenses/MIT).\n\n");
		sb.append("All code and required libraries can be found at [the author's GitHub](http://www.github.com/mattholden/RedditTraders).\n\n");
		sb.append("The bot is written in [Java](http://www.java.com) and powered by [PostgreSQL](http://www.postgresql.org).\n\n");
		sb.append("This code utilizes [jReddit by Omer Elnour](https://bitbucket.org/_oe/jreddit) under the jReddit Attribution License.\n\n");
		sb.append("Special thanks to redditor /u/jawabait for all his help in testing this bot.\n\n");
		sb.append("--------------------------------------------------------------\n\n");
		sb.append("Questions? Pull requests? Visit the /r/" + config.getSupportReddit() + " subreddit or message /u/" + config.getAuthor() + ".\n\n\n");

	}

}
