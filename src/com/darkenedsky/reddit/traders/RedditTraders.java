package com.darkenedsky.reddit.traders;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ParseException;
import com.omrlnr.jreddit.messages.PrivateMessage;
import com.omrlnr.jreddit.subreddit.Subreddit;
import com.omrlnr.jreddit.utils.Utils;

public class RedditTraders {

	
	private Configuration config;
	
	
	private boolean doCommand(PrivateMessage pm, String text, StringBuffer response) throws MalformedURLException, SQLException, IOException, ParseException { 
		
		if (text == null || "".equals(text)) return false;
		text = text.replaceAll("\t", " ");		
		String[] tokens = text.split("[ ]");				
		
		String command = tokens[0].toUpperCase();
		System.out.println("Command: " + command);
		
		if (command.equals("HELP")) { 
			help(pm, tokens, response);
			return true;
		}
		else if (command.equals("TRADE")) { 
			trade(pm, tokens, response);
			return true;
		}
		else if (command.equals("TOP20")) { 
			topTraders(pm, tokens, 20, response);
			return true;
		}		
		else if (command.equals("CONFIRM")) { 
			confirm(pm, tokens, response);
			return true;				
		}
		else if (command.equals("DISPUTE")) { 
			dispute(pm, tokens, response);	
			return true;				
		}
		else if (command.equals("LOOKUP")) { 
			lookup(pm, tokens, response);
			return true;
		}
		else if (command.equals("INSTALL") && senderIsModerator(pm, tokens)) { 
			install(pm, tokens, response);
			return true;
		}
		else if ((command.equals("ACTIVATE") || command.equals("DEACTIVATE")) && senderIsModerator(pm, tokens)) { 
			activate(pm, tokens, response);
			return true;
		}
		else if (command.equals("SETLEGACY") && senderIsModerator(pm, tokens)) { 
			setLegacy(pm, tokens, response);
			return true;
		}
		else if (command.equals("COUNTALL") && senderIsModerator(pm, tokens)) { 
			countAllSubreddits(pm, tokens, response);
			return true;
		}
		else if (command.equals("TEXTFLAIR") && senderIsModerator(pm, tokens)) { 
			setTextFlair(pm, tokens, response);
			return true;
		}
		else if (command.equals("VIEWFLAIR") && senderIsModerator(pm, tokens)) { 
			viewFlair(pm, tokens, response);
			return true;
		}
		else if ((command.equals("RESOLVE") || command.equals("BLAME"))) { 
			resolve(pm, tokens, response);
			return true;
		}
		else if ((command.equals("SETFLAIR") || command.equals("REMOVEFLAIR")) && senderIsModerator(pm, tokens)) { 
			setFlair(pm, tokens, response);
			return true;
		}
		else if ((command.equals("SETMODFLAIR") || command.equals("REMOVEMODFLAIR")) && senderIsModerator(pm, tokens)) { 
			setModFlair(pm, tokens, response);
			return true;
		}
		
		// recognize the mod invite mail
		else if (command.equals("**GADZOOKS!")) { 
			acceptModeratorInvite(pm, tokens);
			return true;
		}
		return false;
	}
	
	private void help(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException { 
			
		sb.append("*RedditTraders ModBot version " + config.getVersion() + " by /u/" + config.getAuthor() + "*\n\n*Command Usage*\n\n");
		sb.append("--------------------------------------------------------------\n\n");		
		sb.append("HELP: Receive this message. \n\n* Usage: HELP\n\n* Example: HELP\n\n");
		sb.append("LOOKUP: Lookup a redditor's trading history. \n\n* Usage: LOOKUP [redditor name]\n\n* Example: LOOKUP RedditTraders\n\n");
		sb.append("TRADE: Initiate a report of a successful trade.\n\n* Usage: TRADE [Redditor's name you traded with] [Trade thread URL] [OPTIONAL: Comments]\n\n* Example: TRADE RedditTraders http://www.reddit.com/r/retrogameswap/comments/178tq4/trade_la_la/ Optional comments go here\n\n");
		sb.append("CONFIRM: Confirm that a trade was successful.\n\n* Usage: CONFIRM [trade id]\n\n* Example: CONFIRM 8675309\n\n");
		sb.append("DISPUTE: Dispute that a trade was successful. *This will notify the mods.*\n\n* Usage: DISPUTE [trade id]\n\n* Example: DISPUTE 8675309\n\n");
		sb.append("TOP20: Get the top 20 traders for a subreddit.\n\n* Usage: TOP20 [subreddit]\n\n* Example: TOP20 retrogameswap\n\n");
		sb.append("--------------------------------------------------------------\n\n");
		sb.append("\nModerator-Only Functions\n\n");
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
		sb.append("RESOLVE: Confirm that a dispute was resolved without assigning blame to either user.\n\n* Usage: RESOLVE [trade id] [OPTIONAL: comments]\n\n* Example: RESOLVE 8675309\n\n");	
		sb.append("BLAME: Confirm that a dispute was resolved and assign blame to a user.\n\n* Usage: BLAME [trade id] [username] [OPTIONAL: comments]\n\n* Example: BLAME 8675309 RedditTraders\n\n");	
		sb.append("--------------------------------------------------------------\n\n");		
		sb.append("Questions? Visit the /r/"+config.getSupportReddit() + " subreddit or message /u/" + config.getAuthor() + ". \n\nPlease note that I only check for new messages every " + config.getSleepSec() + " seconds or so. Please be patient! ;)\n\n\n");
		
	}
	
	
		
	private void viewFlair(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException { 
		
		if (tokens.length < 2) { 
			help(msg, tokens, sb);
			return;
		}
		
		String sub = tokens[1];
		PreparedStatement ps = config.getJDBC().prepareStatement("select * from flairtemplates where subredditid = (select redditid from subreddits where subreddit ilike ?) order by mintrades;");
		ps.setString(1, sub);
		
		sb.append("The following flair templates are set for /r/" + sub + ":\n\n");
		ResultSet set = ps.executeQuery();
		if (set.first()) { 
			while (true) { 
				int t = set.getInt("mintrades");
				sb.append("* " + t + " trade" + ((t!=1)?"s":"") + ": " + set.getString("flairclass") + "\n");
					if (set.isLast()) break;
				set.next();
			}
		}
		set.close();		
		sb.append("\n\n\n");
	}
	
	private void setModFlair(PrivateMessage msg, String[] tokens, StringBuffer sb) throws SQLException, MalformedURLException, IOException, ParseException { 
		
		String flair = null;
		
		// leave null if the command was REMOVEFLAIR - this is so we don't have to have the optional flair parameter
		if (tokens[0].toUpperCase().equals("SETMODFLAIR")) { 
			if (tokens.length < 3) { 
				help(msg, tokens, sb);
				return;
			}
			flair = tokens[2];
		}
		else { 
			if (tokens.length < 2) { 
				help(msg, tokens, sb);
				return;
			}
		}

		String sub = tokens[1];
		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set modflairclass = ? where subreddit ilike ?;");
		if (flair == null) 
			ps.setNull(1, Types.VARCHAR);
		else 
			ps.setString(1, flair);
		ps.setString(2, sub);
		
		ps.execute();
		
		sb.append("Moderator flair for subreddit /r/" + tokens[1] + " has been successfully updated.\n\n\n");
		
	}
	
	private void setFlair(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException { 
	
		
		String flair = null;
		
		// leave null if the command was REMOVEFLAIR - this is so we don't have to have the optional flair parameter
		if (tokens[0].toUpperCase().equals("SETFLAIR")) { 
			if (tokens.length < 4) { 
				help(msg, tokens, sb);
				return;
			}
			flair = tokens[3];
		}
		else { 
			if (tokens.length < 3) { 
				help(msg, tokens, sb);
				return;
			}
		}
		
		String sub = tokens[1];
		String min = tokens[2];
		
		int mintrades = 0;
		try { 
			mintrades = Integer.parseInt(min);
		}
		catch (Exception x) { 
			help(msg, tokens, sb);
			return;
		}
		
		PreparedStatement p = config.getJDBC().prepareStatement("select * from set_flair(?,?,?);");
		p.setString(1, sub);
		p.setInt(2, mintrades);
		if (flair == null) { 
			p.setNull(3, Types.VARCHAR);
		}
		else { 
			p.setString(3, flair);
		}
		p.execute();
		
		sb.append("Flair successfully changed for /r/" + sub + ".\n\n\n");			
		
	}
	
	private void countAllSubreddits(PrivateMessage msg, String[] tokens, StringBuffer sb) throws SQLException, MalformedURLException, IOException, ParseException { 
		if (tokens.length < 3) { 
			help(msg, tokens, sb);
			return;
		}
		
		String onoff = tokens[2];
		boolean toggle = ("on".equals(onoff) ? true : false);
		
		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set count_all_subreddits = ? where subreddit ilike ?;");
		ps.setBoolean(1, toggle);
		ps.setString(2, tokens[1]);
		ps.execute();
		
		sb.append("Flair for subreddit /r/" + tokens[1] + " will now " + 
				(toggle ? "" : "NOT ") + "count trades in other subreddits when determining flair.\n\n\n");
		
	}
	
	private void setTextFlair(PrivateMessage msg, String[] tokens, StringBuffer sb) throws SQLException, MalformedURLException, IOException, ParseException { 
		if (tokens.length < 3) { 
			help(msg, tokens, sb);
			return;
		}
		
		String onoff = tokens[2];
		boolean toggle = ("on".equals(onoff) ? true : false);
		
		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set textflair = ? where subreddit ilike ?;");
		ps.setBoolean(1, toggle);
		ps.setString(2, tokens[1]);
		ps.execute();
		
		sb.append("Flair for subreddit /r/" + tokens[1] + " will now " + 
				(toggle ? "" : "NOT ") + "include the trade count in text.\n\n\n");
		
	}
	
	private void setLegacy(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException { 
		
		if (tokens.length < 4) { 
			help(msg, tokens, sb);
			return;
		}
		String sub = tokens[1];
		String user = tokens[2];
		String lt = tokens[3];
		int trades = 0;
		try { 
			trades = Integer.parseInt(lt);
		}
		catch (NumberFormatException x) { 
			help(msg, tokens, sb);
			return;
		}
		
		PreparedStatement ps = config.getJDBC().prepareStatement("select * from set_legacy(?,?,?);");
		ps.setString(1, user);
		ps.setString(2, sub);
		ps.setInt(3, trades);
		ResultSet foo = ps.executeQuery();
		foo.close();
		sb.append("Legacy trade count for user " + user + " has been updated to " + trades + " on subreddit /r/" + sub + ".\n\n\n");
	}
	
	
	private void lookup(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException { 
		if (tokens.length < 2) { 
			help(msg, tokens, sb);
			return;
		}
		
		String user = tokens[1];
		if (user.equalsIgnoreCase(config.getBotUser().getUsername())) { 
			PreparedStatement ps = config.getJDBC().prepareStatement("select count(tradeid) as trades from trades;");
			int everyTrade =0;
			ResultSet foo = ps.executeQuery();
			if (foo.first()) { 
				everyTrade = foo.getInt("trades");
			}
			foo.close();
			sb.append("Thanks for asking about me!\n\nWhile I don't have anything of my own to trade, I've participated in " + everyTrade + " trades so far.\n\nAlso, I'm a robot, so I'm perfectly reliable and never make mistakes.\n\n*And if I did, I wouldn't tell you, now would I?*\n\n\n");
			return;
		}
		HashMap<String, Integer> trades = new HashMap<String, Integer>();
		int uid = -1;
		
		PreparedStatement ps = config.getJDBC().prepareStatement("select redditorid from redditors where username ilike ?;");
		ps.setString(1, user);
		ResultSet uzer = ps.executeQuery();
		if (uzer.first()) { 
			uid = uzer.getInt("redditorid");
		}
		uzer.close();
		
		if (uid == -1) { 			
			sb.append("Redditor /u/" + user + " is not found for LOOKUP; he or she may not have ever executed a trade through RedditTraders.\n\n\n");
			return;
		}
		
		PreparedStatement ps2 = config.getJDBC().prepareStatement("select subreddit, get_trade_count(?, redditid) as trades from subreddits order by subreddit;");
		ps2.setInt(1, uid);
		ResultSet tradez = ps2.executeQuery();
		if (tradez.first()) { 
			while (true) { 
				trades.put(tradez.getString("subreddit"), tradez.getInt("trades"));
				if (tradez.isLast()) break;
				tradez.next();
			}			
		}
		tradez.close();
		
		int total = 0;
		for (Integer i : trades.values()) { 
			total += i;
		}
		
		if (total == 0) { 
			sb.append("Redditor /u/" + user + " has never executed a trade through RedditTraders.\n\n\n");
			return;
		}
		
		else { 
			sb.append("RedditTraders trade history for user /u/" + user + ":\n\n------------------------------------------------\n\n");
			for (Map.Entry<String, Integer> entry : trades.entrySet()) { 
				sb.append("/r/" + entry.getKey() + " : " + entry.getValue() + " successful trades.\n\n");
			}
			sb.append("------------------------------------------------------\n\n*Total successful trades: "+ total +"*\n\n\n");
			
		}
		
		
	}
	
	private void trade(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException { 
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
		// WONTDO: Make sure both are subscribers. They don't necessarily have to be.		 
		// TODO: Make sure neither user is banned?		
		// TODO: see if both users have a comment on the thread
		// TODO: Watch for repeat traders
		
		// Make sure you aren't doing anything truly silly.
		if (sender.equalsIgnoreCase(tradeWith)) { 
			sb.append("TRADE error: You cannot trade with yourself.\n\n\n");
			return;
		}
		else if (tradeWith.equalsIgnoreCase(config.getBotUser().getUsername())) { 
			sb.append("I'd love to trade with you, " + sender +", but I doubt I have anything you want. I'm just a robot, after all.\n\n\n");
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
		if (!botIsModerator(subreddit)) { 
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
		toUser2.append("As with CONFIRM, you can add any comments you like after the trade number. If you DISPUTE a successful trade, the moderators of /r/"+ subreddit);
		toUser2.append(" will be notified, so that they can settle any dispute that exists.\n\nIf you have any questions, reply to this message with simply the word HELP, and I will provide you with a list of commands I support.\n\n");
		toUser2.append("Thanks for being an active member of /r/"+ subreddit + "!");
		sendMessage(tradeWith, "Did you complete a trade on /r/"+subreddit+"?", toUser2);
		
		// notify the sender that the command was executed successfully
		sb.append("A confirmation message has been sent to /u/" + tradeWith + ". Once he/she confirms this trade, your successful trade will be recorded.\n\nYour trade ID for this trade is: " + tradeid + "\n\n");
		sb.append("Thanks for being an active member of /r/"+ subreddit + "!\n\n\n");
	}
	
	private void topTraders(PrivateMessage pm, String[] tokens, int count, StringBuffer sb) throws SQLException, MalformedURLException, IOException, ParseException { 
		
		if (tokens.length < 2) { 
			help(pm, tokens, sb);
			return;
		}
		String subreddit = tokens[1];
		
		PreparedStatement ps = config.getJDBC().prepareStatement(
				"select username, get_trade_count(redditors.redditorid, (select redditid from subreddits where subreddit ilike ?)) as trades from redditors order by trades desc limit ?;");
		ps.setString(1, subreddit);
		ps.setInt(2, count);
		ResultSet set = ps.executeQuery();
		
		if (!set.first()) { 
			set.close();
			sb.append("TOP: No trades found for subreddit /r/" + subreddit + ".\n\n\n");
			return;
		}
		sb.append("Top " + count + " Traders for /r/" + subreddit + "\n\n-------------------------------------------\n\n");
		int i = 0;
		while (true) { 
			i++;
			sb.append(i + ". [" + set.getInt("trades") + " trades] - " + set.getString("username") + "\n\n");
			if (set.isLast()) break;
			set.next();
		}
		set.close();
		sb.append("\n\n\n");
		
	
	}
	
	private void confirm(PrivateMessage pm, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException {
		
		if (tokens.length < 2) { 
			help(pm, tokens, sb);
			return;
		}
		
		String eyedee = tokens[1];
		int id = -1;
		try { 
			id = Integer.parseInt(eyedee);
		}
		catch (NumberFormatException x) { 
			help(pm, tokens, sb);
			return;
		}
			
		StringBuffer comments = new StringBuffer();
		for (int i = 2; i < tokens.length; i++) {
			comments.append(tokens[i] + " ");
		}
			
		// get the first username
		String user1 = "", user2 = null, url = "", subreddit = "";
		int status = -1;
		boolean textFlair = false;
		
		PreparedStatement p1 = config.getJDBC().prepareStatement("select * from redditors  join trades on (trades.redditorid1 = redditors.redditorid) join subreddits on (subreddits.redditid = trades.subredditid) where tradeid = ?;");
		p1.setInt(1, id);
		ResultSet rs1 = p1.executeQuery();
		if (rs1.first()) { 
			textFlair = rs1.getBoolean("textflair");
			user1 = rs1.getString("username");			
			subreddit = rs1.getString("subreddit");
			url = rs1.getString("threadurl");
			status = rs1.getInt("status");
		}
		else { 
			sb.append("CONFIRM error: An unknown database error has occurred.\n\n\n");
			rs1.close();
			return;
		}
		rs1.close();
		

		if (status == 2 || status == 3) { 
			sb.append("CONFIRM error: Trade #" + id + " is already complete and may not be modified.\n\n\n");
			return;
		}
		
		// now we still need to get user #2
		PreparedStatement p2 = config.getJDBC().prepareStatement("select * from redditors where redditorid = (select redditorid2 from trades where tradeid = ?);");
		p2.setInt(1, id);
		ResultSet rs2 = p2.executeQuery();
		if (!rs2.first()) { 
			rs2.close();
			sb.append("CONFIRM error: An unknown database error has occurred.\n\n\n");
			return;
		}		
		user2 = rs2.getString("username");
		if (!pm.getAuthor().equalsIgnoreCase(user2)) { 
			sb.append("CONFIRM error: Only the user who is is indicated as the trading partner in a trade may confirm it.\n\n\n");
			return;
		}
		
		
		
		// update the DB
		PreparedStatement ps = config.getJDBC().prepareStatement("update trades set status = 2 where tradeid = ?;");
		ps.setInt(1, id);
		ps.execute();
		
		// update flair on both users
		setUserFlair(user1, subreddit, textFlair);
		setUserFlair(user2, subreddit, textFlair);
		
		// send congrats messages to both users
		String message = "Trade #" + id + " between " + user1 + " and " + user2 + " (" + url + ") has been successfully confirmed and recorded. Any changes to your flair should be visible at this time. Thanks for using RedditTraders and being a part of the /r/" + subreddit + " community!";
		sendMessage(user1, "Trade Completed Successfully!", new StringBuffer(message));
		sb.append(message + "\n\n\n");
		
	}
	
	private void setUserFlair(String user, String subreddit, boolean doTextFlair) throws SQLException, MalformedURLException, IOException, ParseException { 
	
		int trades = 0;
		String flair = null;
		
		PreparedStatement ps1 = config.getJDBC().prepareStatement(
				"select * from get_trade_count_with_countall((select redditorid from redditors where username ilike ?), (select redditid from subreddits where subreddit ilike ?));");
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
			System.out.println("No flair matching criteria for user.");
			return;
		}
		
		
		String tradeCount = ""; 
		if (doTextFlair) { 
			tradeCount = "&text=" + Integer.toString(trades) + " trade" + ((trades!=1) ? "s" :"");
		}
		
		Utils.post("uh="+config.getBotUser().getModhash() + "&name="+ user + "&r=" + subreddit + "&css_class = " + flair + tradeCount, 
				new URL("http://www.reddit.com/api/flair"), config.getBotUser().getCookie());
		
	}
	
	
	private void dispute(PrivateMessage pm, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException {
		
		if (tokens.length < 2) { 
			help(pm, tokens, sb);
			return;
		}
		
		String eyedee = tokens[1];
		int id = -1;
		try { 
			id = Integer.parseInt(eyedee);
		}
		catch (NumberFormatException x) { 
			help(pm, tokens, sb);
			return;
		}
			
		StringBuffer comments = new StringBuffer();
		for (int i = 2; i < tokens.length; i++) {
			comments.append(tokens[i] + " ");
		}
		
		// get the details
		String user1 = "", user2 = null, url = "", subreddit = "";
		int status = -1;
		PreparedStatement p1 = config.getJDBC().prepareStatement("select * from redditors  join trades on (trades.redditorid1 = redditors.redditorid) join subreddits on (subreddits.redditid = trades.subredditid) where tradeid = ?;");
		p1.setInt(1, id);
		ResultSet rs1 = p1.executeQuery();
		if (rs1.first()) { 
			user1 = rs1.getString("username");			
			subreddit = rs1.getString("subreddit");
			url = rs1.getString("threadurl");
			status = rs1.getInt("status");
		}
		else { 
			sb.append("DISPUTE error: An unknown database error has occurred.\n\n\n");
			rs1.close();
			return;
		}
		rs1.close();
		
		if (status == 2 || status == 3) { 
			sb.append("DISPUTE error: Trade #" + id + " is already complete and may not be modified.\n\n\n");
			return;
		}

		// now we still need to get user #2
		PreparedStatement p2 = config.getJDBC().prepareStatement("select * from redditors where redditorid = (select redditorid2 from trades where tradeid = ?);");
		p2.setInt(1, id);
		ResultSet rs2 = p2.executeQuery();
		if (!rs2.first()) { 
			rs2.close();
			sb.append("DISPUTE error: An unknown database error has occurred.\n\n\n");
			return;
		}		
		user2 = rs2.getString("username");
		if (!pm.getAuthor().equalsIgnoreCase(user2)) { 
			sb.append("DISPUTE error: Only the user who is is indicated as the trading partner in a trade may dispute it.\n\n\n");
			return;
		}
		
		// update the DB
		PreparedStatement ps = config.getJDBC().prepareStatement("update trades set status = 4 where tradeid = ?;");
		ps.setInt(1, id);
		ps.execute();
		
		// send messages to both users and the mods.
		String message = "Trade #" + id + " between " + user1 + " and " + user2 + " (" + url + ") has been disputed. The moderators of /r/" + subreddit + " have been notified, and will respond shortly. Thanks for using RedditTraders and being a part of the /r/" + subreddit + " community!";
		StringBuffer m = new StringBuffer(message);
		sendMessage(user1, "Trade Disputed", m);
		sb.append(message + "\n\n\n");
		sendMessage("/r/" + subreddit, "Trade Disputed", m);
	}

	private void resolve(PrivateMessage pm, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException {
		
		if (tokens.length < 2) { 
			help(pm, tokens, sb);
			return;
		}
		
		int commentStart = 2;
		
		String blame = null;
		if (tokens[0].equalsIgnoreCase("BLAME")) { 
			if (tokens.length < 3) { 
				help(pm, tokens, sb);
				return;
			}
			blame = tokens[2];
			commentStart = 3;
		}
		
		String eyedee = tokens[1];
		int id = -1;
		try { 
			id = Integer.parseInt(eyedee);
		}
		catch (NumberFormatException x) { 
			help(pm, tokens, sb);
			return;
		}
			
		StringBuffer comments = new StringBuffer();
		for (int i = commentStart; i < tokens.length; i++) {
			comments.append(tokens[i] + " ");
		}
		if (comments.length() >  1024) { 
			String shortened = comments.toString().substring(0, 1024);
			comments = new StringBuffer(shortened);
		}		
				
		// get the details
		String user1 = "", user2 = null, url = "", subreddit = "";
		int status = -1;
		PreparedStatement p1 = config.getJDBC().prepareStatement("select * from redditors  join trades on (trades.redditorid1 = redditors.redditorid) join subreddits on (subreddits.redditid = trades.subredditid) where tradeid = ?;");
		p1.setInt(1, id);
		ResultSet rs1 = p1.executeQuery();
		if (rs1.first()) { 
			user1 = rs1.getString("username");			
			subreddit = rs1.getString("subreddit");
			url = rs1.getString("threadurl");
			status = rs1.getInt("status");
		}
		else { 
			sb.append("RESOLVE/BLAME error: An unknown database error has occurred.\n\n\n");
			rs1.close();
			return;
		}
		rs1.close();
		
		if (status != 4) { 
			sb.append("RESOLVE/BLAME error: Trade #" + id + " is not in dispute.\n\n\n");
			return;
		}

		// now we still need to get user #2
		PreparedStatement p2 = config.getJDBC().prepareStatement("select * from redditors where redditorid = (select redditorid2 from trades where tradeid = ?);");
		p2.setInt(1, id);
		ResultSet rs2 = p2.executeQuery();
		if (!rs2.first()) { 
			rs2.close();
			sb.append("RESOLVE/BLAME error: An unknown database error has occurred.\n\n\n");
			return;
		}		
		user2 = rs2.getString("username");
		

		if (user1.equalsIgnoreCase(pm.getAuthor()) || user2.equalsIgnoreCase(pm.getAuthor())) { 
			sb.append("RESOLVE/BLAME error: You may not resolve your own disputed trade.\n\n\n");
			return;
		}
		if (!this.isModerator(pm.getAuthor(), subreddit)) { 
			sb.append("RESOLVE/BLAME error: Only moderators of the subreddit where the trade was executed may use this function.\n\n\n");
			return;
		}
		
		
		// update the DB
		String blamer = "";
		if (blame != null) { 
			blamer = ", unsuccessful_blame_redditorid = (select redditorid from redditors where username = ?)";
		}
		PreparedStatement ps = config.getJDBC().prepareStatement("update trades set status = 3, modcomments = ?" + blamer + " where tradeid = ?;");
		ps.setString(1, comments.toString());
		if (blame != null) { 
			ps.setString(2, blame);
			ps.setInt(3, id);
		}
		else { 
			ps.setInt(2, id);
		}
		ps.execute();
		
		// send messages to both users and the mods.
		String blameMessage = "";
		if (blame != null)  { 
			blameMessage = "Blame for the unsuccessful trade has been assigned to redditor " + blame + ". ";
		}
		
		String message = "Trade #" + id + " between " + user1 + " and " + user2 + " (" + url + ") has been resolved by the moderators of /r/" + subreddit + ". " + blameMessage + "The moderator's comments follow:\n\n" + comments.toString() + "\n\nThanks for using RedditTraders and being a part of the /r/" + subreddit + " community!\n\n\n";
		StringBuffer m = new StringBuffer(message);
		sendMessage(user1, "Trade Dispute Resolved", m);
		sendMessage(user2, "Trade Dispute Resolved", m);
		sendMessage("/r/" + subreddit, "Trade Dispute Resolved", m);
	}
	
	private void activate(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException {		
		
		if (tokens.length < 3) { 
			help(msg, tokens, sb);
			return;
		}		
		String sub = tokens[1].toLowerCase();
		boolean activate = false;
		if (tokens[0].equalsIgnoreCase("ACTIVATE")) { 
			activate = true;
		}
		
		// check if we're already running on this subreddit
		PreparedStatement is = config.getJDBC().prepareStatement("select * from subreddits where subreddit ilike ?;");
		is.setString(1, sub);
		ResultSet iz = is.executeQuery();
		if (!iz.first()) { 
			sb.append("Error: This bot is not installed on /r/" + sub + ".\n\n\n");
			iz.close();
			return;
		}
		iz.close();
		
		// do the update to the subreddits table
		PreparedStatement ps = config.getJDBC().prepareStatement("update subreddits set activesub = ? where subreddit ilike ?;");
		ps.setBoolean(1, activate);
		ps.setString(2, sub);
		ps.execute();
		
		// subscribe to the subreddit
		String u = "http://www.reddit.com/api/subscribe";
        Utils.post("uh=" + config.getBotUser().getModhash() + "&action=" + (activate?"":"un") + "sub&r=" + sub, new URL(u), config.getBotUser().getCookie());          
    
			
		sb.append("The RedditTraders bot has been successfully " + ((activate)?"":"de") + "activated on subreddit /r/"+sub+".\n\n\n");
	}
	
	private void install(PrivateMessage msg, String[] tokens, StringBuffer sb) throws MalformedURLException, IOException, ParseException, SQLException { 
		
		if (tokens.length < 2) { 
			help(msg, tokens, sb);
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
    
		
		sb.append("RedditTraders has been successfully installed on subreddit /r/" + sub + ". THIS WAS A TRIUMPH.\n\n\nTo configure the bot, please use the commands listed under Moderator Commands on the HELP menu.\n\n\nRemember, in order for the bot to perform moderation tasks like assigning flair, the Reddit user /u/"+ config.getBotUser().getUsername() + " must be made a moderator of /r/" + sub + ". The bot will automatically accept moderator invites it receives.\n\n\nThank you for using RedditTraders!\n\n\n");
		
	}
	


	
	private void process() {
		List<PrivateMessage> messages = null;
	
		try { 	
			messages = config.getBotUser().getMessages("unread", 100);
			//System.out.println("Found " + messages.size() + " new messages.");
		}
		catch (Exception x) { 
			x.printStackTrace();
			return;
		}
		
		for (PrivateMessage pm : messages) { 
			System.out.println("Received message from redditor " + pm.getAuthor() + ": "  + pm.getBody() + " // " + pm.getSubject());
		
			// Mark the message read
			try {
				pm.markRead(config.getBotUser(), true);
							
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			boolean didSubject = false;
			int bodyCount = 0;
			StringBuffer response = new StringBuffer();
			
			try {
				didSubject = doCommand(pm, pm.getSubject(), response);
			}
			catch (Exception x) { 
				x.printStackTrace();
			}
			String[] body = pm.getBody().split("[\n]");
			for (String s : body) { 
				try { 
					if (doCommand(pm, s, response)) { 				
						bodyCount++;
					}
				}
				catch (Exception x) { 
					x.printStackTrace();
				}
			}
			
			if (!didSubject && bodyCount == 0) { 
				try { 
					help(pm, body, response);
				}
				catch (Exception x) { 
					x.printStackTrace();
				}
			}
			
			try { 
				sendMessage(pm.getAuthor(), "RedditTraders Automated Message", response);
			}
			catch (Exception x) { 
				x.printStackTrace();
			}
			
			
			
			
		}
	}

	private void acceptModeratorInvite(PrivateMessage pm, String[] tokens) throws MalformedURLException, IOException, ParseException { 
		if (tokens.length < 10) { 
			return;
		}
		String subreddit = tokens[9];
		subreddit = subreddit.substring(4);
		subreddit = subreddit.substring(0, subreddit.length() - 1);
		Utils.post("uh="+config.getBotUser().getModhash() + "&r="+subreddit, new URL("http://www.reddit.com/api/accept_moderator_invite"), config.getBotUser().getCookie());
		
	}
	
	private boolean botIsModerator(String subreddit) throws MalformedURLException, IOException, ParseException { 
		
		Subreddit sub = new Subreddit();
		sub.setDisplayName(subreddit);
		List<String> mods = sub.getModerators(config.getBotUser());		
		boolean isMod = mods.contains(config.getBotUser().getUsername());
		
		/** 
		 //Don't send the message to the mods - it got the bot banned once		
		if (!isMod) { 
			sendMessage("/r/" + subreddit, "RedditTraders Bot Install Not Complete", 
				new StringBuffer("Hello. I am the RedditTraders trade resolution bot. One of the moderators of subreddit /r/" + subreddit + 
				" asked me to monitor trades and user flair in your subreddit. However, one of the tasks I tried to do couldn't be completed because I don't have moderator access. " + 
				"In order for all my functions to work properly, this account (/u/RedditTraders) needs to be a moderator of /r/" + subreddit + 
				". If you've decided you don't want me to monitor your trades anymore, send me a message that reads \"UNINSTALL " + subreddit + "\", which will command me to stop monitoring your subreddit. Thank you."));
		}
		*/
		return isMod;					
	}
	
	private boolean senderIsModerator(PrivateMessage msg, String[] tokens) throws MalformedURLException, IOException, ParseException { 
		
		String sender = msg.getAuthor();
		System.out.println("Sender: " + sender);
		
		if (tokens.length < 2) { 
			return false;
		}
		
		String subreddit = tokens[1];
		return isModerator(sender, subreddit);
	}
	
	private boolean isModerator(String sender, String subreddit) throws MalformedURLException, IOException, ParseException { 
		Subreddit sub = new Subreddit();
		sub.setDisplayName(subreddit);
		List<String> mods = sub.getModerators(config.getBotUser());
		
		return mods.contains(sender);
	}
	
	private void sendMessage(String user, String sub, StringBuffer body) throws MalformedURLException, IOException, ParseException {
		System.out.println("Sending message " + sub + " to user " + user);
		new PrivateMessage(user, sub, body.toString()).send(config.getBotUser());
		
	}
	
	public static void main(String[] args) { 
		RedditTraders instance = new RedditTraders();	
		while (true) { 
			instance.process();
			int sleep = instance.config.getSleepSec() * 1000;
			try { 
				Thread.sleep(sleep);
			}
			catch (Exception x) { 
				x.printStackTrace();
			}			
		}
	}
	
	public RedditTraders() { 
		
		// Load XML configuration file, connect to DB and connect to Reddit API
		try { 
			config = new Configuration();
			System.out.println("RedditTraders launched OK.");
		}
		catch (Exception x) { 
			x.printStackTrace();
			System.exit(0);
		}	
	}

}
