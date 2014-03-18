package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class TopTraders extends RedditListener {

	private int traders;

	public TopTraders(RedditTraders rt, String cmd, int count) {
		super(rt, cmd, false);
		traders = count;
	}

	/**
	 * Get a list of the top traders in a given subreddit
	 * 
	 * 
	 * @param pm
	 *            The private message received from the user.
	 * @param tokens
	 *            The individual "words" of the command we are executing
	 * @param sb
	 *            The StringBuffer to write any response text out to the user
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SQLException
	 * @throws ParseException
	 */
	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {

		if (tokens.length < 2) {
			help(pm, tokens, sb);
			return;
		}
		String subreddit = tokens[1];

		PreparedStatement ps = config.getJDBC().prepareStatement("select username, get_trade_count(redditors.redditorid, (select redditid from subreddits where subreddit ilike ?)) as trades from redditors order by trades desc limit ?;");
		ps.setString(1, subreddit);
		ps.setInt(2, traders);
		ResultSet set = ps.executeQuery();

		if (!set.first()) {
			set.close();
			sb.append("TOP: No trades found for subreddit /r/" + subreddit + ".\n\n\n");
			return;
		}
		sb.append("Top " + traders + " Traders for /r/" + subreddit + "\n\n-------------------------------------------\n\n");
		int i = 0;
		while (true) {
			i++;
			sb.append(i + ". [" + set.getInt("trades") + " trades] - " + set.getString("username") + "\n\n");
			if (set.isLast())
				break;
			set.next();
		}
		set.close();
		sb.append("\n\n\n");

	}

}
