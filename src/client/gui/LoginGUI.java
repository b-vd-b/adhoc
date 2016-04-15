package client.gui;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LoginGUI extends JDialog implements ActionListener, PropertyChangeListener {

    private JTextField inputField;
    private String btn1 = "Login";
    private JOptionPane optionPane;

    public LoginGUI(){
        super(new DummyFrame("Chat Login"));
        setTitle("Login");
        setMinimumSize(new Dimension(400,200));
        setLocationRelativeTo(null);
        inputField = new JTextField(10);
        String question = "What nickname would you like to use?";
        Object[] array = {question, inputField};
        String btn2 = "Cancel";
        Object[] options = {btn1, btn2};
        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION, null, options, options[0]);
        setContentPane(optionPane);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        inputField.addActionListener(this);
        optionPane.addPropertyChangeListener(this);


        setVisible(true);
        pack();
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
                String nickname = inputField.getText();
                if(nickname.length()>0){
                    JOptionPane.showMessageDialog(this, "Your nickname will be: "+ nickname);
                    new Client(nickname);
                    exit();

                }else{
                    JOptionPane.showMessageDialog(this, "Sorry you need to enter a nickname before you can continue",
                            "Try Again", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Ok, Bye");
                exit();
            }
        }
    }

    private void exit(){
        ((DummyFrame) getParent()).dispose();
        dispose();
    }
}

class DummyFrame extends JFrame {
    DummyFrame(String title) {
        super(title);
        setUndecorated(true);
        setVisible(true);
        setLocationRelativeTo(null);
    }
}
