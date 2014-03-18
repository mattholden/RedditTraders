package com.darkenedsky.reddit.traders.listener;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class Cake extends RedditListener {

	public Cake(RedditTraders rt) {
		super(rt, "CAKE", false);

	}

	@Override
	public void process(PrivateMessage pm, String[] tokens, StringBuffer sb) throws Exception {
		sb.append("I'm sorry, I'm all out of cake. How about some deadly neurotoxin?\n\n\n");

	}

}
