import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class GuardsHub {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPrompt());
    }
}

class LoginPrompt extends JDialog {
    private JTextField identifierField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private final Map<String, String[]> userAccounts = new HashMap<>();

    public LoginPrompt() {
        setTitle("Login - GuardsHub");
        setSize(300, 150);
        setLayout(new GridLayout(3, 2));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        populateAccounts();

        add(new JLabel("Username/WRmail:"));
        identifierField = new JTextField();
        add(identifierField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        add(loginButton);

        loginButton.addActionListener(_ -> authenticate());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void populateAccounts() {
        // Format: key = WRmail, value = [DisplayName, Password]
        userAccounts.put("wrmaillukino.wrm", new String[]{"Lukas", "qb4sd"});
        userAccounts.put("wrmaillucia.wrm", new String[]{"Lucia", "wes5ad"});
        userAccounts.put("wrmailtomas.wrm", new String[]{"Tomas", "bd4g5"});
        userAccounts.put("wrmailsamo.wrm", new String[]{"Samo", "js54s"});
        userAccounts.put("wrmailmato.wrm", new String[]{"Mato", "js0nmt"});
    }

    private void authenticate() {
        String identifier = identifierField.getText();
        String password = new String(passwordField.getPassword());

        if (userAccounts.containsKey(identifier) && userAccounts.get(identifier)[1].equals(password)) {
            JOptionPane.showMessageDialog(this, "Login successful!");
            new MainApp(userAccounts.get(identifier)[0], identifier);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials, try again.");
        }
    }
}

class MainApp extends JFrame {
    private String loggedInUser;
    private String loggedInWRmail;

    public MainApp(String username, String wrmail) {
        this.loggedInUser = username;
        this.loggedInWRmail = wrmail;

        setTitle("GuardsHub - Main Menu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel userLabel = new JLabel("Logged in as: " + loggedInUser, JLabel.CENTER);
        add(userLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton chatButton = new JButton("Chat");
        JButton settingsButton = new JButton("Settings");
        JButton wrContactButton = new JButton("WRcontact");

        buttonPanel.add(chatButton);
        buttonPanel.add(settingsButton);
        buttonPanel.add(wrContactButton);
        add(buttonPanel, BorderLayout.CENTER);

        chatButton.addActionListener(_ -> new ChatWindow(loggedInUser, loggedInWRmail));
        settingsButton.addActionListener(_ -> new SettingsWindow(this));
        wrContactButton.addActionListener(_ -> new WRcontactWindow(loggedInUser, loggedInWRmail));

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void logout() {
        dispose();
        new LoginPrompt();
    }
}

class ChatWindow extends JFrame {
    JTextArea chatArea;
    JTextField messageField;
    JButton sendButton;
    JComboBox<String> recipientBox;
    String senderUsername;
    String senderWRmail;

    public ChatWindow(String username, String wrmail) {
        this.senderUsername = username;
        this.senderWRmail = wrmail;

        setTitle("GuardsHub - Chat");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        add(chatScroll, BorderLayout.CENTER);

        // Top panel for recipient selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Chat with:"));
        recipientBox = new JComboBox<>(new String[]{
            "wrmaillukino.wrm", "wrmaillucia.wrm", "wrmailtomas.wrm",
            "wrmailsamo.wrm", "wrmailmato.wrm"
        });
        topPanel.add(recipientBox);
        add(topPanel, BorderLayout.NORTH);

        // Bottom panel for message input & send button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Message:"), BorderLayout.WEST);
        messageField = new JTextField();
        inputPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        sendButton = new JButton("Send");
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Wire send actions
        sendButton.addActionListener(_ -> sendMessage());
        messageField.addActionListener(_ -> sendMessage());

        loadChatHistory();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void sendMessage() {
        String recipientWRmail = (String) recipientBox.getSelectedItem();
        String message = messageField.getText();
        if (!message.isEmpty()) {
            String fullMessage = senderUsername + " to " + recipientWRmail + ": " + message;
            chatArea.append(fullMessage + "\n");
            saveChatHistory(recipientWRmail, senderUsername + ": " + message);
            messageField.setText("");
        }
    }

    private void saveChatHistory(String recipient, String message) {
        try (FileWriter writer = new FileWriter("chat_" + recipient + ".txt", true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving chat history.");
        }
    }

    private void loadChatHistory() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("chat_" + senderWRmail + ".txt")));
            chatArea.setText(content);
        } catch (IOException e) {
            chatArea.setText("No previous messages.\n");
        }
    }
}

class SettingsWindow extends JFrame {
    private MainApp mainApp;

    public SettingsWindow(MainApp mainApp) {
        this.mainApp = mainApp;

        setTitle("GuardsHub - Settings");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());

        JButton installButton = new JButton("Install Package");
        JButton logoutButton = new JButton("Logout");

        installButton.addActionListener(_ -> installPackage());
        logoutButton.addActionListener(_ -> logout());

        add(installButton);
        add(logoutButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void installPackage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();
            if (fileName.endsWith(".zip") || fileName.endsWith(".tar")) {
                JOptionPane.showMessageDialog(this, "Installing package: " + fileName);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid package format. Select .zip or .tar.");
            }
        }
    }

    private void logout() {
        mainApp.logout();
        dispose();
    }
}

class WRcontactWindow extends JFrame {
    private String loggedInUser;
    private String loggedInWRmail;
    private JTable contactTable;
    private JButton chatButton;
    private JButton callButton;

    // Contacts data (Name, WRmail)
    private String[][] contacts = {
        {"Lukas", "wrmaillukino.wrm"},
        {"Lucia", "wrmaillucia.wrm"},
        {"Tomas", "wrmailtomas.wrm"},
        {"Samo", "wrmailsamo.wrm"},
        {"Mato", "wrmailmato.wrm"}
    };
    private String[] columnNames = {"Name", "WRmail"};

    public WRcontactWindow(String username, String wrmail) {
        this.loggedInUser = username;
        this.loggedInWRmail = wrmail;

        setTitle("WRcontact - Inbox");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        contactTable = new JTable(contacts, columnNames);
        JScrollPane scrollPane = new JScrollPane(contactTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        chatButton = new JButton("Chat");
        callButton = new JButton("Call");
        bottomPanel.add(chatButton);
        bottomPanel.add(callButton);
        add(bottomPanel, BorderLayout.SOUTH);

        chatButton.addActionListener(_ -> {
            int selectedRow = contactTable.getSelectedRow();
            if (selectedRow >= 0) {
                String contactWRmail = (String) contactTable.getValueAt(selectedRow, 1);
                ChatWindow chatWin = new ChatWindow(loggedInUser, loggedInWRmail);
                chatWin.recipientBox.setSelectedItem(contactWRmail);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a contact to chat with.");
            }
        });

        callButton.addActionListener(_ -> {
            int selectedRow = contactTable.getSelectedRow();
            if (selectedRow >= 0) {
                String contactName = (String) contactTable.getValueAt(selectedRow, 0);
                String contactWRmail = (String) contactTable.getValueAt(selectedRow, 1);
                // Send a WRmail notification to the selected contact:
                sendCallNotification(contactWRmail, loggedInUser);
                // Open CallWindow to simulate a call:
                new CallWindow(contactName, contactWRmail);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a contact to call.");
            }
        });
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void sendCallNotification(String contactWRmail, String callerName) {
        try (FileWriter writer = new FileWriter("wrmail_" + contactWRmail + ".txt", true)) {
            writer.write("You have been called by " + callerName + "\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error sending call notification.");
        }
    }
}

class CallWindow extends JFrame {
    public CallWindow(String contactName, String contactWRmail) {
        setTitle("Calling " + contactName);
        setSize(300, 150);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel callLabel = new JLabel("Calling " + contactName + " (" + contactWRmail + ")...");
        add(callLabel);
        
        JButton endCall = new JButton("End Call");
        add(endCall);

        endCall.addActionListener(_ -> {
            JOptionPane.showMessageDialog(this, "Call Ended.");
            dispose();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
