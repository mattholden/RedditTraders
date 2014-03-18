package com.darkenedsky.reddit.traders;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.darkenedsky.reddit.traders.listener.About;
import com.darkenedsky.reddit.traders.listener.AcceptModInvite;
import com.darkenedsky.reddit.traders.listener.Activate;
import com.darkenedsky.reddit.traders.listener.Cake;
import com.darkenedsky.reddit.traders.listener.Confirm;
import com.darkenedsky.reddit.traders.listener.CountAllSubs;
import com.darkenedsky.reddit.traders.listener.Help;
import com.darkenedsky.reddit.traders.listener.Install;
import com.darkenedsky.reddit.traders.listener.Lookup;
import com.darkenedsky.reddit.traders.listener.ModHelp;
import com.darkenedsky.reddit.traders.listener.RedditListener;
import com.darkenedsky.reddit.traders.listener.Resolve;
import com.darkenedsky.reddit.traders.listener.SetBlameBan;
import com.darkenedsky.reddit.traders.listener.SetFlair;
import com.darkenedsky.reddit.traders.listener.SetLegacy;
import com.darkenedsky.reddit.traders.listener.SetModFlair;
import com.darkenedsky.reddit.traders.listener.SetTextFlair;
import com.darkenedsky.reddit.traders.listener.TopTraders;
import com.darkenedsky.reddit.traders.listener.Trade;
import com.darkenedsky.reddit.traders.listener.ViewFlair;
import com.omrlnr.jreddit.messages.PrivateMessage;
import com.omrlnr.jreddit.subreddit.Subreddit;
import com.omrlnr.jreddit.utils.Utils;

/**
 * This bot responds to Reddit private messages in order to maintain an
 * Ebay-like feedback system for users of swap meet-style subreddits. It is
 * available under the MIT license.
 * 
 * Built for the fine folks at http://www.reddit.com/r/retrogameswap
 * 
 * Features include: - Allow users to record their own successful trades without
 * moderator involvement - Install, activate and deactivate in new subreddits
 * without interaction from the author - Look up a user's feedback or view a
 * leaderboard for the subreddit - Support legacy trades from before the bot was
 * responsible for a subreddit - Assign flair to users when they have reached
 * certain configurable threshholds - Provide a framework for dispute resolution
 * by a subreddit's moderators - Automatically ban users who are blamed for
 * enough unsuccessful trades in a window
 * 
 * @author Matt Holden (matt@mattholden.com)
 * 
 */
public class RedditTraders {

	public static RedditTraders instance;

	/**
	 * The entry point for the RedditTraders application.
	 * 
	 * 
	 * @param args
	 *            Command line parameters (not used)
	 */
	public static void main(String[] args) {
		instance = new RedditTraders();
		while (true) {
			instance.process();
			int sleep = instance.config.getSleepSec() * 1000;
			try {
				Thread.sleep(sleep);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	private HashMap<String, RedditListener> listeners = new HashMap<String, RedditListener>();

	/** Log4J instance */
	private final Logger LOG = Logger.getLogger(RedditTraders.class);

	/** The Configuration information we will load from config.xml */
	private Configuration config;

	/**
	 * Construct a new RedditTraders instance.
	 * 
	 * 
	 */
	public RedditTraders() {

		// Load XML configuration file, connect to DB and connect to Reddit API
		try {
			config = new Configuration();
			addListener(new Help(this));
			addListener(new About(this));
			addListener(new Confirm(this));
			addListener(new Activate(this));
			listeners.put("DEACTIVATE", new Activate(this));
			addListener(new CountAllSubs(this));
			addListener(new ModHelp(this));
			addListener(new AcceptModInvite(this));
			addListener(new Install(this));
			addListener(new Cake(this));
			addListener(new Trade(this));
			addListener(new TopTraders(this, "TOP20", 20));
			addListener(new Lookup(this));
			addListener(new SetLegacy(this));
			addListener(new SetTextFlair(this));
			addListener(new ViewFlair(this));
			addListener(new Resolve(this));
			listeners.put("BLAME", new Resolve(this));
			listeners.put("CLOSE", new Resolve(this));
			addListener(new SetFlair(this));
			listeners.put("REMOVEFLAIR", new SetFlair(this));
			addListener(new SetModFlair(this));
			listeners.put("REMOVEMODFLAIR", new SetModFlair(this));
			addListener(new SetBlameBan(this));

			// Build a system tray icon
			SystemTray tray = SystemTray.getSystemTray();

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem("Exit");
			ActionListener exitListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			};
			defaultItem.addActionListener(exitListener);
			popup.add(defaultItem);

			Image image = Toolkit.getDefaultToolkit().getImage("reddit.png");
			TrayIcon trayIcon = new TrayIcon(image, "RedditTraders " + config.getVersion(), popup);
			trayIcon.setImageAutoSize(true);
			tray.add(trayIcon);

			LOG.debug("RedditTraders launched OK.");
		} catch (Exception x) {
			x.printStackTrace();
			System.exit(0);
		}
	}

	private void addListener(RedditListener listen) {
		listeners.put(listen.getCommand(), listen);
	}

	/**
	 * Ban a user from the subreddit
	 * 
	 * @param user
	 *            User to ban
	 * @param subreddit
	 *            subreddit to ban from
	 * @param comment
	 *            Reason for the ban
	 * @throws ParseException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public void ban(String user, String subreddit, String comment) throws MalformedURLException, IOException, ParseException {
		String u = "http://www.reddit.com/api/friend";
		Utils.post("name=" + user + "&uh=" + config.getBotUser().getModhash() + "&type=banned&note=" + comment + "&r=" + subreddit, new URL(u), config.getBotUser().getCookie());

	}

	/**
	 * Check to see if the bot is a moderator in the subreddit
	 * 
	 * 
	 * @param subreddit
	 *            Name of the subreddit
	 * @return true if the bot is a moderator of it
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public boolean botIsModerator(String subreddit) throws MalformedURLException, IOException, ParseException {

		Subreddit sub = new Subreddit();
		sub.setDisplayName(subreddit);
		List<String> mods = sub.getModerators(config.getBotUser());
		boolean isMod = mods.contains(config.getBotUser().getUsername());

		/**
		 * //Don't send the message to the mods - it got the bot banned once \
		 * if (!isMod) { sendMessage("/r/" + subreddit,
		 * "RedditTraders Bot Install Not Complete", new StringBuffer(
		 * "Hello. I am the RedditTraders trade resolution bot. One of the moderators of subreddit /r/"
		 * + subreddit +
		 * " asked me to monitor trades and user flair in your subreddit. However, one of the tasks I tried to do couldn't be completed because I don't have moderator access. "
		 * +
		 * "In order for all my functions to work properly, this account (/u/RedditTraders) needs to be a moderator of /r/"
		 * + subreddit +
		 * ". If you've decided you don't want me to monitor your trades anymore, send me a message that reads \"UNINSTALL "
		 * + subreddit +
		 * "\", which will command me to stop monitoring your subreddit. Thank you."
		 * )); }
		 */
		return isMod;
	}

	/**
	 * Evaluate the 'text' string to see if it is a valid command, and execute
	 * it if it is.
	 * 
	 * 
	 * @param pm
	 *            The message the bot received
	 * @param text
	 *            The line of the message being evaluated
	 * @param response
	 *            A string buffer to write any output to, so that if a message
	 *            contains multiple commands, they will all be responded to in
	 *            the same reply message.
	 * 
	 * @return true if a command was found and executed
	 * 
	 * @throws MalformedURLException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ParseException
	 */
	private boolean doCommand(PrivateMessage pm, String text, StringBuffer response) throws MalformedURLException, SQLException, IOException, ParseException {

		if (text == null || "".equals(text))
			return false;
		text = text.replaceAll("\t", " ");
		String[] tokens = text.split("[ ]");

		String command = tokens[0].toUpperCase();
		LOG.debug("Command: " + command);

		RedditListener listen = listeners.get(command);
		try {
			if (listen != null) {
				listen.doCommand(pm, tokens, response);
				return true;
			} else {
				return false;
			}
		} catch (Exception x) {
			LOG.error(x);
		}

		return false;
	}

	/**
	 * Get the configuration settings.
	 * 
	 * @return the configuration.
	 */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * Get the listener for the specified command.
	 * 
	 * @param cmd
	 *            The command to look for
	 * @return the listener attached to that command, or null if there is none.
	 */
	public RedditListener getListener(String cmd) {
		return listeners.get(cmd.toUpperCase());
	}

	/**
	 * A more general check to see if a user is the moderator of a subreddit
	 * 
	 * 
	 * 
	 * @param sender
	 *            User we are testing
	 * @param subreddit
	 *            The subreddit we want the user to be a moderator of
	 * @return true if the bot is a moderator of the subreddit
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public boolean isModerator(String sender, String subreddit) throws MalformedURLException, IOException, ParseException {
		Subreddit sub = new Subreddit();
		sub.setDisplayName(subreddit);
		List<String> mods = sub.getModerators(config.getBotUser());

		return mods.contains(sender);
	}

	public void log(String string) {
		LOG.debug(string);
	}

	/**
	 * Called in a loop every so many seconds to get messages and run any
	 * commands found within them.
	 * 
	 */
	public void process() {
		List<PrivateMessage> messages = null;

		try {
			messages = config.getBotUser().getMessages("unread", 100);
			// LOG.debug("Found " + messages.size() + " new messages.");
		} catch (Exception x) {
			LOG.error(x);
			return;
		}

		for (PrivateMessage pm : messages) {
			LOG.debug("======================================================");
			LOG.debug(Calendar.getInstance().getTime() + " Received message from redditor " + pm.getAuthor() + ": ");
			LOG.debug("Subject: " + pm.getSubject());
			LOG.debug(pm.getBody());
			LOG.debug("\n");
			// Mark the message read
			try {
				pm.markRead(config.getBotUser(), true);

			} catch (Exception e) {
				LOG.error(e);
				continue;
			}

			boolean didSubject = false;
			int bodyCount = 0;
			StringBuffer response = new StringBuffer();

			try {
				didSubject = doCommand(pm, pm.getSubject(), response);
			} catch (Exception x) {
				LOG.error(x);
				response.append("An unknown error occurred while processing this command:\n\n " + pm.getSubject() + "\n\n\n");
			}
			String[] body = pm.getBody().split("[\n]");
			for (String s : body) {
				try {
					if (doCommand(pm, s, response)) {
						bodyCount++;
					}
				} catch (Exception x) {
					response.append("An unknown error occurred while processing this command:\n\n " + s + "\n\n\n");
					LOG.error(x);
				}
			}

			if (!didSubject && bodyCount == 0) {
				try {
					getListener("HELP").process(pm, body, response);
				} catch (Exception x) {
					response.append("An unknown error occurred while processing this command:\n\n " + body + "\n\n\n");
					LOG.error(x);
				}
			}

			try {
				sendMessage(pm.getAuthor(), "RedditTraders Automated Message", response);
				LOG.debug("Sending to " + pm.getAuthor() + ":");
				LOG.debug(response.toString());
				LOG.debug("\n");

			} catch (Exception x) {
				LOG.error(x);
			}

		}
	}

	/**
	 * Render a percentage of two integers neatly
	 * 
	 * 
	 * @param amt
	 *            numerator
	 * @param den
	 *            denominator
	 * @return the percentage as a whole percent
	 */
	public String renderPct(int amt, int den) {
		double numerator = amt;
		double denominator = den;
		double pct = numerator / denominator;
		pct *= 100.0;
		int percent = (int) pct;
		return percent + "%";
	}

	/**
	 * Check to see if the sender of this message is a moderator on a subreddit.
	 * The subreddit's name must be the contents of tokens[1] to do the check.
	 * If you want to check moderation on a message not formatted like this, use
	 * the isModerator() method.
	 * 
	 * 
	 * 
	 * @param msg
	 *            The private message received from the user.
	 * @param tokens
	 *            The individual "words" of the command we are executing
	 * @return true if the bot is a moderator of the subreddit listed in
	 *         tokens[1]
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 * @see isModerator()
	 */
	public boolean senderIsModerator(PrivateMessage msg, String[] tokens) throws MalformedURLException, IOException, ParseException {

		String sender = msg.getAuthor();
		LOG.debug("Sender: " + sender);

		if (tokens.length < 2) {
			return false;
		}

		String subreddit = tokens[1];
		return isModerator(sender, subreddit);
	}

	/**
	 * Send a private message to a user on Reddit
	 * 
	 * 
	 * @param user
	 *            Username of the recipient
	 * @param sub
	 *            Subject of the message
	 * @param body
	 *            Body of the message
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void sendMessage(String user, String sub, StringBuffer body) throws MalformedURLException, IOException, ParseException {
		LOG.debug("Sending message " + sub + " to user " + user);
		new PrivateMessage(user, sub, body.toString()).send(config.getBotUser());

	}

	/**
	 * Internal method to do the actual setting of flair
	 * 
	 * 
	 * @param user
	 *            Redditor's username
	 * @param subreddit
	 *            The subreddit being traded on
	 * @param doTextFlair
	 *            'true' if the TEXTFLAIR option is turned on for this subreddit
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 * @throws SQLException
	 */
	public void setUserFlair(String user, String subreddit, boolean doTextFlair) throws SQLException, MalformedURLException, IOException, ParseException {

		int trades = 0;
		String flair = null;

		PreparedStatement ps1 = config.getJDBC().prepareStatement("select * from get_trade_count_with_countall((select redditorid from redditors where username ilike ?), (select redditid from subreddits where subreddit ilike ?));");
		ps1.setString(1, user);
		ps1.setString(2, subreddit);
		ResultSet set1 = ps1.executeQuery();
		if (set1.first()) {
			trades = set1.getInt("get_trade_count_with_countall");
		}
		set1.close();

		PreparedStatement ps2 = config.getJDBC().prepareStatement("select * from get_flair_class(?,?,?);");
		ps2.setString(1, user);
		ps2.setString(2, subreddit);
		ps2.setBoolean(3, isModerator(user, subreddit));
		ResultSet set2 = ps2.executeQuery();
		if (set2.first()) {
			flair = set2.getString("get_flair_class");
		}
		set2.close();
		if (flair == null) {
			LOG.debug("No flair matching criteria for user.");
			return;
		}

		String tradeCount = "";
		if (doTextFlair) {
			tradeCount = "&text=" + Integer.toString(trades) + " trade" + ((trades != 1) ? "s" : "");
		}

		LOG.debug("Posting flair...");
		String post = "uh=" + config.getBotUser().getModhash() + "&name=" + user + "&r=" + subreddit + "&css_class=" + flair + tradeCount;
		LOG.debug(post);
		Utils.post(post, new URL("http://www.reddit.com/api/flair"), config.getBotUser().getCookie());
		LOG.debug("Flair posted.");

	}

}
