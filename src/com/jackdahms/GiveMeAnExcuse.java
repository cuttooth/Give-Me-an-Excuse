package com.jackdahms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

public class GiveMeAnExcuse {
	
	public static final int INFO = 0,
							COMMAND = 1,
							SEVERE = 2, //TODO remove throw statements, add tries and catches
							REQUEST = 3,
							RESPONSE = 4,
							WARNING = 5;
	
	private static String USER_NAME = "givemeanexcuse@gmail.com"; 
	private static JTextArea display, users;
	private static JPanel numbers;
    private static List<String> excuses = new ArrayList<String>();
	
	//stats
	private static int messagesReceived = 0,
			repliesSent = 0,
			uniqueUsersThisSession = 0;
		
	private static String[] command = {"help", "list"};
	private static String[] desc = {"displays all commands and brief descriptions for each", 
		"lists all excuses"
	};
	
	private static List<String> uniqueUsers = new ArrayList<String>();
		
	private static int WIDTH = 900, HEIGHT = 550;
		
    public static void main(String[] args){
    	
    	long startTime = System.nanoTime();
    	
    	Arrays.sort(args);
    	if (Arrays.binarySearch(args, "nogui") != -1)
    		;
    	else
    		createAndShowGUI();
    	
    	Thread worker = new Thread(new Runnable() {
    		public void run(){
				start();
    		}
    	});
    	worker.start();
    	
    	long elapsed = (System.nanoTime() - startTime) / 1000000; //calculates difference then converts to milliseconds
    	
    	append(INFO, "Initialized in " + elapsed + "ms");
    	append(INFO, "Type 'help' for help.");
    }
    
    public static void createAndShowGUI(){
    	//set look and feel
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Could not set look and feel with UIManager!");
		}
    	//set up jframe
    	JFrame frame = new JFrame();
    	
    	frame.setTitle("Give Me an Excuse");
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setSize(WIDTH, HEIGHT);
    	frame.setLocationRelativeTo(null);
    	frame.setResizable(false);
    	
    	Container pane = frame.getContentPane();
    	pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
    	
    	int statWidth = WIDTH / 4;
    	int numbersHeight = 60;
    	
    	JPanel stats = new JPanel();
    	stats.setBorder(BorderFactory.createTitledBorder("Stats"));
    	stats.setMaximumSize(new Dimension(statWidth, HEIGHT));
    	stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
    	
    	numbers = new JPanel() {
    		private static final long serialVersionUID = 1L;

    		@Override    		    		
    		public void paint(Graphics g) {
    			g.setColor(Color.white);
    			g.fillRect(0, 0, getWidth(), getHeight());
    			g.setColor(Color.gray);
    			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    			g.setColor(Color.black);
    			g.setFont(new Font("dialog", Font.PLAIN, 12));
    			g.drawString("Messages received: " + messagesReceived, 10, 15);
    			g.drawString("Replies sent: " + repliesSent, 10, 30);
    			g.drawString("Unique users this session: " + uniqueUsersThisSession, 10, 45);
    		}
    	};
    	numbers.setMaximumSize(new Dimension(numbers.getMaximumSize().width, numbersHeight));
    	    	
    	JPanel session = new JPanel();
    	session.setBorder(BorderFactory.createTitledBorder("Session"));
    	session.setMaximumSize(new Dimension(session.getMaximumSize().width, HEIGHT - numbersHeight));
    	session.setLayout(new GridLayout(1, 1));
    	
    	users = new JTextArea();
    	users.setFont(new Font("courier", Font.PLAIN, 12));
    	users.setEditable(false);
    	JScrollPane playScroll = new JScrollPane(users);
    	playScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    	
    	session.add(playScroll);
    	
    	stats.add(numbers);
    	stats.add(session);
    	
    	JPanel log = new JPanel();
    	log.setBorder(BorderFactory.createTitledBorder("Log"));
    	log.setMaximumSize(new Dimension(WIDTH - statWidth, HEIGHT));
    	log.setLayout(new BorderLayout());
    	
    	display = new JTextArea();
    	display.setFont(new Font("courier", Font.PLAIN, 12));
    	display.setEditable(false);
    	JScrollPane dispScroll = new JScrollPane(display);
    	dispScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    	    	
    	JTextField comm = new JTextField();
    	comm.setFont(new Font("courier", Font.PLAIN, 12));
    	comm.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e) {
    			command(comm.getText());
    			comm.setText("");
    		}
    	});
    	
    	log.add(dispScroll, BorderLayout.CENTER);
    	log.add(comm, BorderLayout.SOUTH);
    	
    	pane.add(stats);
    	pane.add(log);
    	
    	frame.setVisible(true);
    }
    
    
     
    public static void command(String msg) {
    	Scanner msgScanner = new Scanner(msg);
    	String first = null;
    	try {
    		first = msgScanner.next();
    		if (first.equals("help")) {
        		for (int i = 0; i < command.length; i++) 
        			msg += "\n\t" + command[i] + " - " + desc[i];
            	append(COMMAND, msg);
        	} else if (first.equals("list")) {
        		for (int i = 0; i < excuses.size(); i++) 
        			msg += "\n " + (i + 1) + ". " + excuses.get(i);
            	append(COMMAND, msg);
        	} else {
        		msg = "[\"" + msg + "\"] is not recognized as a command...";
        		append(INFO, msg);
        	}
    	} catch (Exception e) {
    		append(WARNING, "Hey! You didn't type anything!");
    	}    	
    	msgScanner.close();
    }
        
    public static void append(int code, String msg) {
    	switch(code) {
    		case INFO: display.append("[INFO] "); break;
    		case COMMAND: display.append("[COMMAND] "); break;
    		case SEVERE: display.append("[SEVERE] "); break;
    		case REQUEST: display.append("[REQUEST] "); break;
    		case RESPONSE: display.append("[RESPONSE] "); break;
    		case WARNING: display.append("[WARNING]"); break;
    	}
    	display.append(msg + "\n");
    	display.setCaretPosition(display.getDocument().getLength());
    }
    
    public static void formatStats() {
    	numbers.repaint();
    }
    
    public static void updateUsers(String address) {
    	boolean unique = true;
    	for (String user : uniqueUsers)
    		if (user.equals(address))
    			unique = false;
    	if (unique) {
    		uniqueUsers.add(address);
    		users.setText("");
    		for (String user : uniqueUsers) 
    			users.append(user + "\n");
    		uniqueUsersThisSession = uniqueUsers.size();
    	}
    }
    
    public static void start(){
    	//things that only need to be set once
        String from = USER_NAME;
        String pass;
        boolean alive = true;
        int oldCount = 0;
        int newCount = 0;
        int diff = 0;
        int size;
        Random gen = new Random();
        
        //read password
        Scanner	key = new Scanner(GiveMeAnExcuse.class.getResourceAsStream("key.lock"));
        pass = key.nextLine();
        key.close();
        
        //read excuses
        Scanner excuse = new Scanner(GiveMeAnExcuse.class.getResourceAsStream("excuses.txt"));
        while (excuse.hasNextLine())
        	excuses.add(excuse.nextLine());
        excuse.close();
        size = excuses.size();
        
    	Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true"); 
        
        Session session = Session.getDefaultInstance(props);
        
        //for reading mail
        Store store;
        Folder inbox = null;
		try {
			store = session.getStore();
			store.connect("imap.gmail.com", from, pass);
			inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
			oldCount = inbox.getMessageCount();
		} catch (Exception e) {
			append(SEVERE, "Could not open inbox!");
		}
           
        while (alive) {
        	//detects new messages
        	try {
				newCount = inbox.getMessageCount();
			} catch (MessagingException e) {
				append(SEVERE, "Could not get message count from inbox!");
			}
        	diff = newCount - oldCount;
        	oldCount = newCount;
        	if (diff != 0) {
        		//loops in case incoming messages ever outpaces program
        		for (int i = 0; i < diff; i++) {
	        		//read message
	                Message msg = null;
	                Address[] in = null;
					try {
						msg = inbox.getMessage(inbox.getMessageCount() - i);
		                in = msg.getFrom();
					} catch (MessagingException e) {
						append(SEVERE, "Could not read message!");
					}
                
                	//send message
                	MimeMessage message = new MimeMessage(session);  
	                
	                for (Address address : in) {
	                	try {
							append(REQUEST, address.toString() + " [\"" + msg.getContent().toString().trim() + "\"]");
						} catch (Exception e) {
							append(WARNING, "Could not read body of message!");
						}
	                	updateUsers(address.toString());
	                    messagesReceived++;
	                    InternetAddress toAddress;
	                    try {
	                    	message.setFrom(new InternetAddress(from));
	                    	toAddress = new InternetAddress(address.toString());
		                    message.setRecipient(Message.RecipientType.TO, toAddress);
	                    } catch (Exception e) {
	                    	append(SEVERE, "Could not address reply!");
	                    }

	                    String exc = excuses.get(gen.nextInt(size));
	                    try {
							message.setText(exc);
						} catch (MessagingException e) {
							append(SEVERE, "Could not set reply text!");
						}
	                    append(RESPONSE, exc);
	                    repliesSent++;
	                    try {
		                    Transport transport = session.getTransport("smtp");
		                    transport.connect("smtp.gmail.com", from, pass);
		                    transport.sendMessage(message, message.getAllRecipients());
		                    transport.close();
	                    } catch (Exception e) {
	                    	append(SEVERE, "Could not send reply!");
	                    }
	                }
        		}
        	}
        	formatStats();
        }
    }
}//beautiful cascading brackets