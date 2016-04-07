package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by bvdb on 7-4-2016.
 */
public class LoginGUI extends JDialog implements ActionListener, PropertyChangeListener {

    private String nickname;
    private JTextField inputField;
    private String btn1 = "Login";
    private String btn2 = "Cancel";
    private String question = "What nickname would you like to use?";
    private JOptionPane optionPane;

    public LoginGUI(){
        setTitle("Login");
        setMinimumSize(new Dimension(400,200));
        inputField = new JTextField(10);
        Object[] array = {question, inputField};
        Object[] options = {btn1,btn2};
        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options, options[0]);
        setContentPane(optionPane);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        inputField.addActionListener(this);
        optionPane.addPropertyChangeListener(this);


        setVisible(true);
        pack();
    }

    public String getNickname(){
        return nickname;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(btn1);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();

        if (isVisible()
                && (evt.getSource() == optionPane)
                && (JOptionPane.VALUE_PROPERTY.equals(prop)
                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
            Object value = optionPane.getValue();
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                //ignore reset
                return;
            }

            if(btn1.equals(value)){
                nickname = inputField.getText();
                if(nickname.length()>0){
                    JOptionPane.showMessageDialog(this, "Your nickname will be: "+nickname);
                    Client client = new Client(nickname);
                    exit();

                }else{
                    JOptionPane.showMessageDialog(this, "Sorry you need to enter a nickname before you can continue",
                            "Try Again", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Ok, Bye");
                nickname = null;
                exit();
            }
        }
    }

    public void exit(){
        dispose();
    }
}
