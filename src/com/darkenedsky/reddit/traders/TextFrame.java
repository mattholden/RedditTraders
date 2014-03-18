/*
 * TextFrame.java
 *
 * Created on August 30, 2006, 2:28 PM
 */

package com.darkenedsky.reddit.traders;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Simple frame for displaying text
 * 
 * @author Matt Holden
 */
public class TextFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5003756817304063425L;

	/** text */
	private JTextArea text;

	/** Creates a new instance of RTFFrame */
	public TextFrame() {
		setBounds(0, 0, 600, 600);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new java.awt.BorderLayout());
		text = new JTextArea();
		text.setWrapStyleWord(true);
		text.setLineWrap(true);
		text.setEditable(false);
		JScrollPane scroll = new JScrollPane(text);
		scroll.setWheelScrollingEnabled(true);
		getContentPane().add(scroll, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * Creates a new instance of TextFrame
	 * 
	 * @param file
	 *            file name
	 */
	public TextFrame(String file) throws IOException, FileNotFoundException {
		this();
		File f = new File(file);
		BufferedReader read = new BufferedReader(new FileReader(f));
		StringBuffer buffer = new StringBuffer();
		String line = new String("");
		while (line != null) {
			line = read.readLine();
			if (line != null) {
				buffer.append(line);
				buffer.append('\n');
			}
		}
		read.close();
		text.setText(buffer.toString());
		setTitle(f.getName());
		text.setCaretPosition(0);
	}

	/**
	 * Creates a new instance of TextFrame
	 * 
	 * @param file
	 *            file name
	 * @param title
	 *            of frame
	 */
	public TextFrame(String value, String title) {
		this();
		text.setText(value);
		setTitle(title);
		text.setCaretPosition(0);
	}

}
