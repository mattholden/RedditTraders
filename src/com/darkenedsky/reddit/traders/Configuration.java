package com.darkenedsky.reddit.traders;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import com.omrlnr.jreddit.user.User;
import com.omrlnr.jreddit.utils.Utils;
import com.darkenedsky.gemini.common.JDBCConnection;

/** 
 * Loads all the configuration for the bot from XML and establishes connections. 
 * 	
 * @author Matt Holden (matt@mattholden.com) 
 * */
public class Configuration {

	/** The SAX Builder object used for string parsing */
	private static SAXBuilder sax = new SAXBuilder();
	
	/** JDBC Connection object */
	private JDBCConnection jdbc;
	
	/** Reddit user for the bot. We'll need to pass this in to reddit API calls in jreddit */
	private User botUser;
	
	/** Bot author's name */
	private String author;
	
	/** Bot's support reddit */
	private String supportReddit;
	
	/** Bot's support email */
	private String supportEmail;
	
	/** Bot version */	
	private String version;
	
	/** Length of time (in seconds) to sleep between checks for new messages */
	private int sleepSec = 15;
	
	
	/** 
	 * Loads the XML config file, connects to the database and configures/logs into the Reddit API
	 * 
	 * @throws Exception
	 */
	public Configuration() throws Exception { 
			Document doc = null;
			doc = sax.build("config.xml");
			Element e = doc.getRootElement();
			
			// connect to the database
			Element db = e.getChild("database");
			jdbc = new JDBCConnection(getString(db,"user"), getString(db, "password"), getString(db, "path"), getString(db, "driver"));
			
			// Set the user agent for the reddit api
			Utils.setUserAgent(getString(e, "reddit_user_agent"));
			
			// authorship info to be displayed in the bot's help and source
			author = getString(e, "author");
			version = getString(e, "version");
			supportReddit = getString(e, "supportreddit");
			supportEmail = getString(e, "supportemail");
			
			// These three numbers help us throttle the bot so it doesn't run afoul of Reddit's 
			// 30 calls per minute requirement and get itself banned.
			String sec = getString(e, "sleepsec");
			if (sec != null) { 
				try { 
					sleepSec = Integer.parseInt(sec);
				}
				catch (NumberFormatException x) { 
					
				}
			}
			sec = getString(e, "apicalls_beforesleep");
			int x = 0, y = 0;
			if (sec != null) { 
				try { 
					x = Integer.parseInt(sec);
				}
				catch (NumberFormatException xs) { 
					
				}
			}
			sec = getString(e, "sleep_after_call_limit_sec");
			if (sec != null) { 
				try { 
					y = Integer.parseInt(sec);
				}
				catch (NumberFormatException xs) { 
					
				}
			}
			Utils.setAPICallSafety(x, y);
			
			// connect to reddit with the bot
			Element usr = e.getChild("reddit_account");
			botUser = new User(getString(usr, "user"), getString(usr,"password"));
			botUser.connect();
						
	}

	/** @return the Reddit username of the author */
	public String getAuthor() {
		return author;
	}

	/** @return the name of the subreddit where you can get support for this bot */
	public String getSupportReddit() {
		return supportReddit;
	}

	/** @return the email address of the author */
	public String getSupportEmail() {
		return supportEmail;
	}

	/** @return the version number of the bot */
	public String getVersion() {
		return version;
	}

	/** @return the number of seconds to sleep between checks for new messages */
	public int getSleepSec() {
		return sleepSec;
	}

	/** @return the JDBC connection object */
	public JDBCConnection getJDBC() { 
		return jdbc;
	}
	
	/** @return the jReddit user object for the bot */
	public User getBotUser() { 
		return botUser;
	}
	
	/** Convenience method to get a string out of an xml tag
	 * 
	 * @param root The XML Element that contains the tag
	 * @param elem The name of the child XML element you want the value of
	 * @return The text of the requested element
	 */
	@SuppressWarnings("rawtypes")
	private String getString(Element root, String elem) { 
		List list = root.getChildren(elem);
		if (list.size() == 0) return null;
		return ((Element)list.get(0)).getText();
	}
	
}
