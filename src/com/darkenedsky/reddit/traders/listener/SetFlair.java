package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetFlair extends RedditListener {

	public SetFlair(RedditTraders rt) {
		super(rt, "SETFLAIR", true);
	}

	/**
	 * Moderator function to set the flair for a particular number of trades to
	 * a certain CSS class, or delete the flair entirely (which occurs when the
	 * command is REMOVEFLAIR)
	 * 
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

		String flair = null;

		// leave null if the command was REMOVEFLAIR - this is so we don't have
		// to have the optional flair parameter
		if (tokens[0].toUpperCase().equals("SETFLAIR")) {
			if (tokens.length < 4) {
				modHelp(msg, tokens, sb);
				return;
			}
			flair = tokens[3];
		} else {
			if (tokens.length < 3) {
				modHelp(msg, tokens, sb);
				return;
			}
		}

		String sub = tokens[1];
		String min = tokens[2];

		int mintrades = 0;
		try {
			mintrades = Integer.parseInt(min);
		} catch (Exception x) {
			help(msg, tokens, sb);
			return;
		}

		PreparedStatement p = config.getJDBC().prepareStatement("select * from set_flair(?,?,?);");
		p.setString(1, sub);
		p.setInt(2, mintrades);
		if (flair == null) {
			p.setNull(3, Types.VARCHAR);
		} else {
			p.setString(3, flair);
		}
		p.execute();

		sb.append("Flair successfully changed for /r/" + sub + ".\n\n\n");

	}

}
