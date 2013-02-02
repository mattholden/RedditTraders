package com.darkenedsky.reddit.traders;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import com.omrlnr.jreddit.user.User;
import com.omrlnr.jreddit.utils.Utils;
import com.darkenedsky.gemini.common.JDBCConnection;

/** 
 * Load all the configuration for the bot and establish connections 
 * 	
 * @author Matt Holden 
 * */
public class Configuration {

	/** The SAX Builder object used for string parsing */
	private static SAXBuilder sax = new SAXBuilder();
	
	/** JDBC Connection object */
	private JDBCConnection jdbc;
	
	/** Reddit user for the bot. We'll need to pass this in to reddit API calls in jreddit */
	private User botUser;
	
	private String author, supportReddit, supportEmail, version;
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
			author = getString(e, "author");
			version = getString(e, "version");
			supportReddit = getString(e, "supportreddit");
			supportEmail = getString(e, "supportemail");
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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSupportReddit() {
		return supportReddit;
	}

	public void setSupportReddit(String supportReddit) {
		this.supportReddit = supportReddit;
	}

	public String getSupportEmail() {
		return supportEmail;
	}

	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getSleepSec() {
		return sleepSec;
	}

	public void setSleepSec(int sleepSec) {
		this.sleepSec = sleepSec;
	}

	/** @return the JDBC connection object */
	public JDBCConnection getJDBC() { 
		return jdbc;
	}
	
	/** @return the reddit user object for the bot */
	public User getBotUser() { 
		return botUser;
	}
	
	@SuppressWarnings("rawtypes")
	private String getString(Element root, String elem) { 
		List list = root.getChildren(elem);
		if (list.size() == 0) return null;
		return ((Element)list.get(0)).getText();
	}
}
