package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by bvdb on 6-4-2016.
 */
public class ClientGUI extends JPanel {

    private HashMap<String,InetAddress> clients;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    private JFrame mainChat;
    private JTabbedPane chatPane;
    private JScrollPane scrollPane;
    private JSplitPane splitPane;

    private Client client;
    private String nickname;

    private GroupChatGUI groupChatTab;
    private HashMap<String, PrivateChatGUI> privateChatTabs;

    private class ClientSelectionListener extends MouseAdapter {
        private ClientGUI clientGUI;
        public ClientSelectionListener(ClientGUI clientGUI) {
            super();
            this.clientGUI = clientGUI;
        }

        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount()==2){
                String client = clientList.getSelectedValue();

                if(!privateChatTabs.containsKey(client)){
                    PrivateChatGUI privateChatGUI = new PrivateChatGUI(client, clientGUI);
                    privateChatTabs.put(client, privateChatGUI);

                    chatPane.addTab(client, privateChatGUI);
                    chatPane.setSelectedComponent(privateChatGUI);

                } else{
                    chatPane.setSelectedComponent(privateChatTabs.get(client));
                }
            }
        }
    }

    public ClientGUI(String nickname, Client client){
        setLayout(new BorderLayout());
        this.nickname = nickname;
        this.client = client;
        this.clients = new HashMap<>();
        this.privateChatTabs = new HashMap<>();
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
        clientList = new JList<>(clientListModel);
        clientList.addMouseListener(new ClientSelectionListener(this));
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        scrollPane = new JScrollPane(clientList);
        scrollPane.setMinimumSize(new Dimension(100,0));
        scrollPane.setPreferredSize(new Dimension(100,600));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatPane, scrollPane);
        splitPane.setResizeWeight(1.0);


        mainChat.add(splitPane, BorderLayout.CENTER);
        mainChat.setSize(800,600);
        mainChat.setLocationRelativeTo(null);
        mainChat.setVisible(true);
        mainChat.pack();

    }
    public String getNickname() { return nickname; }
    public Client getClient(){
        return client;
    }
    public HashMap<String, InetAddress> getClients(){
        return clients;
    }
    public void newGroupMessage(String nickname, String message){
        //todo: need to check if this selection works
        chatPane.setSelectedComponent(groupChatTab);
        groupChatTab.addMessage(nickname, message);
    }

    public void newPrivateMessage(String nickname, String message){
        if(!privateChatTabs.containsKey(nickname)){
            PrivateChatGUI privateChatGUI = new PrivateChatGUI(nickname, this);
            privateChatTabs.put(nickname, privateChatGUI);

            chatPane.addTab(nickname, privateChatGUI);
            chatPane.setSelectedComponent(privateChatGUI);
        }
        privateChatTabs.get(nickname).addMessage(nickname, message);
    }

    public void newGroupFileMessage(String nickname, File file){
        //todo: need to check if this selection works
        chatPane.setSelectedComponent(groupChatTab);
        groupChatTab.addMessage(nickname, "has uploaded "+file.getName());
    }

    public void newPrivateFileMessage(String nickname, File file){
        if(!privateChatTabs.containsKey(nickname)){
            PrivateChatGUI privateChatGUI = new PrivateChatGUI(nickname, this);
            privateChatTabs.put(nickname, privateChatGUI);

            chatPane.addTab(nickname, privateChatGUI);
            chatPane.setSelectedComponent(privateChatGUI);
        }
    }

    public void addClient(String nickname, InetAddress address){
        clients.put(nickname, address);
        clientListModel.addElement(nickname);

    }

    public void removeClient(String nickname){
        clients.remove(nickname);
        clientListModel.removeElement(nickname);
    }
}
