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
							SEVERE = 2,
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
		
    public static void main(String[] args) throws Exception{
    	
    	long startTime = System.nanoTime();
    	
    	Arrays.sort(args);
    	if (Arrays.binarySearch(args, "nogui") != -1)
    		;
    	else
    		createAndShowGUI();
    	
    	Thread worker = new Thread(new Runnable() {
    		public void run(){
    			try {
					start();
				} catch (Exception e) {
					append(SEVERE, "Could not start!");
				}
    		}
    	});
    	worker.start();
    	
    	long elapsed = (System.nanoTime() - startTime) / 1000000; //calculates difference then converts to milliseconds
    	
    	append(INFO, "Initialized in " + elapsed + "ms");
    	append(INFO, "Type 'help' for help.");
    }
    
    public static void createAndShowGUI() throws Exception{
    	//set look and feel
    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
    
    public static void start() throws Exception{
    	//things that only need to be set once
        String from = USER_NAME;
        String pass;
        boolean alive = true;
        int oldCount;
        int newCount;
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
        Store store = session.getStore();
        store.connect("imap.gmail.com", from, pass);
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        oldCount = inbox.getMessageCount();
           
        while (alive) {
        	//detects new messages
        	newCount = inbox.getMessageCount();
        	diff = newCount - oldCount;
        	oldCount = newCount;
        	if (diff != 0) {
        		//loops in case incoming messages ever outpaces program
        		for (int i = 0; i < diff; i++) {
	        		//read message
	                Message msg = inbox.getMessage(inbox.getMessageCount() - i);
	                Address[] in = msg.getFrom();
                
                	//send message
                	MimeMessage message = new MimeMessage(session);  
	                
	                for (Address address : in) {
	                	append(REQUEST, address.toString() + " [\"" + msg.getContent().toString().trim() + "\"]");
	                	updateUsers(address.toString());
	                    messagesReceived++;
	                    message.setFrom(new InternetAddress(from));
	                    InternetAddress toAddress = new InternetAddress(address.toString());
	                    message.setRecipient(Message.RecipientType.TO, toAddress);

	                    String exc = excuses.get(gen.nextInt(size));
	                    message.setText(exc);
	                    append(RESPONSE, exc);
	                    repliesSent++;
	                    Transport transport = session.getTransport("smtp");
	                    transport.connect("smtp.gmail.com", from, pass);
	                    transport.sendMessage(message, message.getAllRecipients());
	                    transport.close();	                    
	                }
        		}
        	}
        	formatStats();
        }
    }
}//beautiful cascading brackets