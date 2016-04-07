package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 * Created by bvdb on 6-4-2016.
 */
public class ClientGUI extends JPanel {

    private ListModel<Client> clientListModel;
    private JList<Client> clientList;
    private JFrame mainChat;
    private JTabbedPane chatPane;
    private JScrollPane scrollPane;
    private JSplitPane splitPane;

    private Client client;
    private String id;

    private GroupChatGUI groupChatTab;
    private HashMap<Client, PrivateChatGUI> privateChatTabs;

    private class ClientSelectionListener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount()==2){
                Client client = clientList.getSelectedValue();
                PrivateChatGUI privateChatGUI = new PrivateChatGUI();
                // set actionlistener private moet hier nog komen
                privateChatTabs.put(client,privateChatGUI);
                chatPane.addTab(client.toString(), privateChatGUI);
                chatPane.setSelectedComponent(privateChatGUI);
            }
        }
    }

    public ClientGUI(String nickname, Client client){
        setLayout(new BorderLayout());
        this.client = client;
        mainChat = new JFrame("Awesome ad hoc Chat program");
        mainChat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainChat.setMinimumSize(new Dimension(800, 600));
        mainChat.setSize(800,600);
        groupChatTab = new GroupChatGUI(nickname, this);

        chatPane = new JTabbedPane();
        chatPane.addTab("GroupChat", groupChatTab);
        chatPane.setMinimumSize(new Dimension(400, 0));
        chatPane.setPreferredSize(new Dimension(600,600));

        clientListModel = new DefaultListModel<>();
        clientList = new JList<Client>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        scrollPane = new JScrollPane(clientList);
        scrollPane.setMinimumSize(new Dimension(100,0));
        scrollPane.setPreferredSize(new Dimension(100,600));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPane, scrollPane);
        splitPane.setResizeWeight(1.0);


        mainChat.add(splitPane, BorderLayout.CENTER);
        mainChat.setSize(800,600);
        mainChat.setVisible(true);
        mainChat.pack();

    }

    public Client getClient(){
        return client;
    }
    public void newGroupMessage(String nickname, String message){
        groupChatTab.addMessage(nickname, message);
    }

    public void newPrivateMessage(String nickname, String message){

    }

}
