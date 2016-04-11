package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by bvdb on 6-4-2016.
 */
public class ClientGUI extends JPanel {

    private HashMap<InetAddress,String> clients;
    private HashMap<Color, InetAddress> colorMap;
    private HashMap<InetAddress, Color> addressColorHashMap;
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
                Color color = clientList.getSelectionForeground();
                if(!privateChatTabs.containsKey(client)){
                    PrivateChatGUI privateChatGUI = new PrivateChatGUI(colorMap.get(color), clientGUI);
                    privateChatTabs.put(client, privateChatGUI);

                    chatPane.addTab(client, privateChatGUI);
                    chatPane.setSelectedComponent(privateChatGUI);

                } else{
                    chatPane.setSelectedComponent(privateChatTabs.get(client));
                }
            }
        }
    }

    public ClientGUI(Client client){
        setLayout(new BorderLayout());
        this.nickname = client.getNickname();
        this.client = client;
        this.clients = new HashMap<>();
        this.colorMap = new HashMap<>();
        this.addressColorHashMap = new HashMap<>();
        this.privateChatTabs = new HashMap<>();
        mainChat = new JFrame("Awesome ad hoc Chat program");
        mainChat.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainChat.setMinimumSize(new Dimension(800, 600));
        mainChat.setSize(800,600);
        groupChatTab = new GroupChatGUI(this);

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

    public HashMap<InetAddress, Color> getAddressColorHashMap(){
        return addressColorHashMap;
    }
    public String getNickname() { return nickname; }
    public Client getClient(){
        return client;
    }
    public HashMap<InetAddress, String> getClients(){
        return clients;
    }
    public void newGroupMessage(InetAddress address, String message){
        groupChatTab.addMessage(clients.get(address), message, addressColorHashMap.get(address));
    }

    public void newPrivateMessage(InetAddress address, String message){
        if(!privateChatTabs.containsKey(clients.get(address))){
            PrivateChatGUI privateChatGUI = new PrivateChatGUI(address, this);
            privateChatTabs.put(clients.get(address), privateChatGUI);

            chatPane.addTab(clients.get(address), privateChatGUI);
            chatPane.setSelectedComponent(privateChatGUI);
        }
        privateChatTabs.get(clients.get(address)).addMessage(clients.get(address), message);
    }

    public void addClient(String nickname, InetAddress address, Color color){
        clients.put(address, nickname);
        clientListModel.addElement(nickname);
        colorMap.put(color, address);
        addressColorHashMap.put(address, color);
    }

    public void removeClient(InetAddress address){
        clients.remove(address);
        clientListModel.removeElement(clients.get(address));
        addressColorHashMap.remove(address);
        colorMap.values().remove(address);
    }
}
