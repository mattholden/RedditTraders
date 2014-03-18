package com.darkenedsky.reddit.traders.listener;

import java.io.IOException;
import java.net.MalformedURLException;

import com.darkenedsky.reddit.traders.RedditTraders;
import com.omrlnr.jreddit.messages.PrivateMessage;

public class ModHelp extends RedditListener {

	public ModHelp(RedditTraders instance) {
		super(instance, "MODHELP", false);
	}

	/**
	 * Reply to the user with a list of all the bot's publicly available
	 * commands.
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
	public void process(PrivateMessage msg, String[] tokens, StringBuffer sb) throws Exception {
		sb.append("*RedditTraders Trading Bot version " + config.getVersion() + " by /u/" + config.getAuthor() + "*\n\n*Moderator Command Usage*\n\n");
		sb.append("--------------------------------------------------------------\n\n");
		sb.append("INSTALL: Install the RedditTraders bot for a subreddit. \n\n* Usage: INSTALL [subreddit]\n\n* Example: INSTALL retrogameswap\n\n");
		sb.append("ACTIVATE: Activate RedditTraders trade processing for a subreddit. Newly INSTALLed subreddits are active by default.\n\n* Usage: ACTIVATE [subreddit]\n\n* Example: ACTIVATE retrogameswap\n\n");
		sb.append("DEACTIVATE: Dectivate RedditTraders trade processing for a subreddit. Data already gathered will be kept and trades in progress may be completed.\n\n* Usage: ACTIVATE [subreddit]\n\n* Example: ACTIVATE retrogameswap\n\n");
		sb.append("SETLEGACY: Set the number of total trades a user had in a subreddit before this bot was installed.\n\n* Usage: SETLEGACY [subreddit] [redditor] [trade_count]\n\n* Example: SETLEGACY retrogameswap RedditTraders 100\n\n");
		sb.append("COUNTALL: Set whether or not the subreddit's flair assignment counts trades in other subreddits.\n\n* Usage: COUNTALL [subreddit] [ON/OFF]\n\n* Example: COUNTALL retrogameswap ON\n\n");
		sb.append("TEXTFLAIR: Set whether or not the subreddit's flair should include a text trade count.\n\n* Usage: TEXTFLAIR [subreddit] [ON/OFF]\n\n* Example: TEXTFLAIR retrogameswap ON\n\n");
		sb.append("VIEWFLAIR: View the flair settings in the bot.\n\n* Usage: VIEWFLAIR [subreddit]\n\n* Example: VIEWFLAIR retrogameswap\n\n");
		sb.append("SETFLAIR: Set the flair class for a trade level in the bot.\n\n* Usage: SETFLAIR [subreddit] [trades] [flairclass]\n\n* Example: SETFLAIR retrogameswap 10 yellow10\n\n");
		sb.append("REMOVEFLAIR: Remove the flair class for a trade level in the bot.\n\n* Usage: REMOVEFLAIR [subreddit] [trades]\n\n* Example: REMOVEFLAIR retrogameswap 10\n\n");
		sb.append("SETMODFLAIR: Set the flair class for moderators in the bot.\n\n* Usage: SETMODFLAIR [subreddit] [flairclass]\n\n* Example: SETMODFLAIR retrogameswap modsarecool\n\n");
		sb.append("REMOVEMODFLAIR: Remove the flair class for moderators in the bot, meaning mods will have the same flair as everyone else.\n\n* Usage: REMOVEMODFLAIR [subreddit]\n\n* Example: REMOVEMODFLAIR retrogameswap\n\n");
		sb.append("RESOLVE: Confirm that a dispute was resolved, resulting in a successful trade.\n\n* Usage: RESOLVE [trade id] [OPTIONAL: comments]\n\n* Example: RESOLVE 8675309\n\n");
		sb.append("CLOSE: Confirm that a dispute was resolved, resulting in an unsuccessful trade but without assigning fault to either redditor.\n\n* Usage: CLOSE [trade id] [OPTIONAL: comments]\n\n* Example: CLOSE 8675309\n\n");
		sb.append("BLAME: Confirm that a dispute was resolved and assign blame to a user.\n\n* Usage: BLAME [trade id] [username] [OPTIONAL: comments]\n\n* Example: BLAME 8675309 RedditTraders\n\n");
		sb.append("SETBLAMEBAN: Configure the auto-banning feature. If a user receives [blame] blames in [days] days, they will be banned. Enter 0 for both parameters to disable this feature.\n\n* Usage: SETBLAMEBAN [subreddit] [blames] [days]\n\n* Example: SETBLAMEBAN retrogameswap 5 365\n\n");
		sb.append("SETACCOUNTAGE: Set the minimum age (in days) for Reddit accounts to be able to confirm trades. Enter 0 to disable this feature.\n\n* Usage: SETACCOUNTAGE [subreddit] [days]\n\n* Example: SETACCOUNTAGE retrogameswap 90\n\n");
		sb.append("SETVERIFIEDEMAIL: Toggles whether or not redditors must have reddit-verified emails to confirm trades. Enter 0 to disable this feature or 1 to enable it.\n\n* Usage: SETVERIFIEDEMAIL [subreddit] [1 or 0]\n\n* Example: SETVERIFIEDEMAIL retrogameswap 1\n\n");
		sb.append("--------------------------------------------------------------\n\n");
		sb.append("Questions? Visit the /r/" + config.getSupportReddit() + " subreddit or message /u/" + config.getAuthor() + ". \n\nPlease note that I only check for new messages every " + config.getSleepSec() + " seconds or so. Please be patient! ;)\n\n\n");

	}

}
