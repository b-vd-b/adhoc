package client.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static util.Variables.MAXIMUM_FILE_SIZE;

/**
 * Created by bvdb on 6-4-2016.
 */
public class GroupChatGUI extends JPanel {

    private String nickname;
    private ClientGUI clientGUI;

    private JLabel label;
    private JTextPane textArea;
    private JButton sendButton;
    private JButton fileButton;
    private JTextField inputField;
    private JFileChooser fileChooser;
    private DefaultStyledDocument document = new DefaultStyledDocument();
    private StyleContext context = new StyleContext();
    private Style style = context.addStyle("Style", null);


    public GroupChatGUI(String nickname, ClientGUI clientGUI) {
        this.nickname = nickname;
        this.clientGUI = clientGUI;
        setLayout(new BorderLayout());

        //create and add the text area which cannot be edited
        textArea = new JTextPane(document);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        //create the input panel (inputField, sendButton fileSendButton)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputField = new JTextField();
        inputField.setDocument(new JTextFieldLimit(140));
        inputField.getDocument().addDocumentListener(new InputFieldListener());
        inputField.addActionListener(new SendMessageActionListener());

        //create the label, send and file send button
        label = new JLabel("0/140");
        sendButton = new JButton("SEND MESSAGE");
        sendButton.setSize(50, 10);
        sendButton.addActionListener(new SendMessageActionListener());
        fileChooser = new JFileChooser();
        fileButton = new JButton("SEND FILE");
        fileButton.setSize(50, 10);
        fileButton.addActionListener(new SendFileActionListener());


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = HORIZONTAL;
        inputPanel.add(inputField, gbc);

        //reset gbc to original state for the buttons
        gbc.weightx = 0.0;
        gbc.fill = NONE;
        inputPanel.add(label, gbc);
        inputPanel.add(sendButton, gbc);
        inputPanel.add(fileButton, gbc);

        add(inputPanel, BorderLayout.SOUTH);


    }

    public void addMessage(String nickname, String message) {

        Calendar calendar = Calendar.getInstance();
        String time = ""+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);

        try {
            document.insertString(document.getLength(), "[" + time + "] " + nickname + ": " + message, style);
            //BufferedImage img = ImageIO.read(new File("PATH"));
            //ImageIcon pictureImage = new ImageIcon(img);
            //textArea.insertIcon(pictureImage);
            document.insertString(document.getLength(), "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private class InputFieldListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            String msg = inputField.getText();
            label.setText(msg.length() + "/140");
            warn();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            String msg = inputField.getText();
            label.setText(msg.length() + "/140");
            warn();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            String msg = inputField.getText();
            label.setText(msg.length() + "/140");
            warn();
        }

        public void warn() {
            if ((inputField.getText().length()) > 140) {
                JOptionPane.showMessageDialog(null,
                        "Error: Please enter 140 or less characters", "Error Massage",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class SendMessageActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String msg = inputField.getText();
            inputField.setText("");
            if (msg.length() > 0) {
                try {
                    clientGUI.getClient().sendGroupTextMessage(msg);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                addMessage(nickname, msg);
            }
        }
    }

    private class SendFileActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selection = fileChooser.showOpenDialog(null);
            //do the following when a file has been selected
            if (selection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file.length() > MAXIMUM_FILE_SIZE) {
                    JOptionPane.showMessageDialog(null,
                            "Error: Files can at most be "+MAXIMUM_FILE_SIZE/1000000+" MB large", "Error Massage",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        clientGUI.getClient().sendGroupFileMessage(file);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    addMessage(nickname, file.getName() + " has been sent!");
                }
            }
        }
    }
}

