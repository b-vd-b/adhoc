package client.gui;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;

/**
 * Created by bvdb on 6-4-2016.
 */
public class GroupChatGUI extends JPanel {

    private String nickname;
    private ClientGUI clientGUI;

    private JTextPane textArea;
    private JButton sendButton;
    private JButton fileButton;
    private JTextField inputField;
    private JFileChooser fileChooser;
    private DefaultStyledDocument document = new DefaultStyledDocument();
    private StyleContext context = new StyleContext();
    private Style style = context.addStyle("Style", null);


    public GroupChatGUI(String nickname, ClientGUI clientGUI){
        this.nickname = nickname;
        this.clientGUI = clientGUI;
        setLayout(new BorderLayout());

        //create and add the text area which cannot be edited
        textArea = new JTextPane(document);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane,BorderLayout.CENTER);

        //create the input panel (inputField, sendButton fileSendButton)
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputField = new JTextField();
        inputField.addActionListener(new SendMessageActionListener());

        //create the send and file send button
        sendButton = new JButton("SEND MESSAGE");
        sendButton.setSize(50,10);
        sendButton.addActionListener(new SendMessageActionListener());
        fileChooser = new JFileChooser();
        fileButton = new JButton("SEND FILE");
        fileButton.setSize(50,10);
        fileButton.addActionListener(new SendFileActionListener());


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = HORIZONTAL;
        inputPanel.add(inputField, gbc);

        //reset gbc to original state for the buttons
        gbc.weightx = 0.0;
        gbc.fill = NONE;
        inputPanel.add(sendButton, gbc);
        inputPanel.add(fileButton, gbc);

        add(inputPanel, BorderLayout.SOUTH);




    }

    public void addMessage(String nickname, String message){

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());

        try {
            document.insertString(document.getLength(), "["+currentTimestamp+"] "+nickname+": "+message, style);
            //BufferedImage img = ImageIO.read(new File("PATH"));
            //ImageIcon pictureImage = new ImageIcon(img);
            //textArea.insertIcon(pictureImage);
            document.insertString(document.getLength(), "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private class SendMessageActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String msg = inputField.getText();
            inputField.setText("");
            if(msg.length() > 0){
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
            if(selection == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                //needs attention (what is neccesary to send a file?)

                try {
                    byte[] data = Files.readAllBytes(Paths.get(file.getPath()));
                    clientGUI.getClient().sendGroupFileMessage(data, file.getName());
                    addMessage(nickname, file.getName()+" has been sent!");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }
    }
}

