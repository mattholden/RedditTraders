package com.darkenedsky.reddit.traders.listener;

import java.sql.PreparedStatement;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class SetList extends RedditListener {

	public SetList(RedditTraders rt) {
		super(rt, "SETWANTLIST", false);
	}

	/** Set a WANT or HAVE list. */
	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {

		if (tokens.length < 1 || pm.getBody() == null || pm.getBody().equals("")) {
			help(pm, tokens, sb);
			return;
		}

		String body = pm.getBody();
		int column = (tokens[0].equalsIgnoreCase("SETWANTLIST")) ? 1 : 0;
		PreparedStatement ps = config.getJDBC().prepareStatement("select * from set_list(?,?,?,?);");
		ps.setString(1, pm.getAuthor());
		ps.setString(2, tokens[1]);
		ps.setString(3, body);
		ps.setInt(4, column);
		ps.execute();

		sb.append("Set " + ((column == 1) ? "want" : "have") + " list for redditor " + pm.getAuthor() + " in subreddit /r/" + tokens[1] + ".");

	}

}
