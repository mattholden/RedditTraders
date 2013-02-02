package com.omrlnr.jreddit.messages;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omrlnr.jreddit.Thing;
import com.omrlnr.jreddit.user.User;
import com.omrlnr.jreddit.utils.Utils;

public class PrivateMessage extends Thing {

	private String author;
	private String subject;
	private String body;
	private String dest;
	
	public PrivateMessage() { 
		kind = "t4";
	}
	
	public PrivateMessage(JSONObject obj) { 
		kind = "t4";
		author = toString(obj.get("author"));
		dest = toString(obj.get("dest"));
		subject = toString(obj.get("subject"));
		body = toString(obj.get("body"));	
		this.fullName = toString(obj.get("name"));
		
	}
	
	public PrivateMessage(String recipient, String subject, String message) { 
		kind = "t4";
		dest = recipient;
		this.subject = subject;
		body = message;
	}
	
	 /**
     * Safely converts an object into string (used because sometimes
     * JSONObject's get() method returns null).
     *
     * @param obj The object to convert.
     * @return The string.
     */
    private static String toString(Object obj) {
        return (obj == null ? null : obj.toString());
    }

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}
    

    public void markRead(User user, boolean read) throws MalformedURLException, IOException, ParseException { 
    	
    	Utils.post("id=" + fullName +"&uh=" + user.getModhash(), new URL(
                "http://www.reddit.com/api/" + ((read)?"read_message":"unread_message")), user.getCookie());
     }
    
    public void send(User user) throws MalformedURLException, IOException, ParseException { 
    	this.author = user.getUsername();
    	
    	Utils.post("subject="+subject + "&to="+this.dest +"&uh="+user.getModhash() + "&text="+this.body,
    			new URL("http://www.reddit.com/api/compose"), user.getCookie());
    }
}
