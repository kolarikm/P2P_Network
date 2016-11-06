import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.*;

import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.FlowLayout;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.JScrollPane;

public class MainGUI implements ActionListener{

    private JFrame frame;
    private JTextField serverIpInput;
    private JTextField portInput;
    private JTextField userNameInput;
    private JTextField hostNameInput;
    private JTextField textField;
    private JTextField txtCommand;
    private JButton btnConnect;
    private JTextArea commandRestuls;
    private JComboBox speedSelector;
    private FTPClient client;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainGUI window = new MainGUI();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainGUI() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBackground(Color.DARK_GRAY);
        frame.setBounds(100, 100, 772, 412);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel lblIp = new JLabel("Server IP:");
        panel.add(lblIp);

        serverIpInput = new JTextField();
        serverIpInput.setText("localhost");
        panel.add(serverIpInput);
        serverIpInput.setColumns(8);

        JLabel lblPort = new JLabel("Port:");
        panel.add(lblPort);

        portInput = new JTextField();
        //This is the default server port.
        portInput.setText("5012");
        panel.add(portInput);
        portInput.setColumns(5);

        JLabel lblUsername = new JLabel("Name:");
        panel.add(lblUsername);

        userNameInput = new JTextField();
        userNameInput.setText("Ben");
        panel.add(userNameInput);
        userNameInput.setColumns(10);

        JLabel lblHostname = new JLabel("HostName:");
        panel.add(lblHostname);

        hostNameInput = new JTextField();
        hostNameInput.setText("HostName");
        panel.add(hostNameInput);
        hostNameInput.setColumns(10);

        speedSelector = new JComboBox();
        speedSelector.setModel(new DefaultComboBoxModel(new String[] {"T1", "Ethernet"}));
        speedSelector.setSelectedIndex(0);
        panel.add(speedSelector);

        btnConnect = new JButton("Connect");
        panel.add(btnConnect);
        btnConnect.addActionListener(this);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);

        JLabel lblSearchMeBitch = new JLabel("Keyword:");
        lblSearchMeBitch.setBounds(20, 0, 62, 30);
        panel_1.add(lblSearchMeBitch);

        textField = new JTextField();
        textField.setBounds(90, 0, 150, 30);
        panel_1.add(textField);
        textField.setColumns(10);
        frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
        frame.getContentPane().add(panel);
        frame.getContentPane().add(panel_1);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(243, 2, 117, 29);
        panel_1.add(btnSearch);

        JLabel commandLabel = new JLabel("Enter Command:");
        commandLabel.setBounds(414, 7, 108, 16);
        panel_1.add(commandLabel);

        txtCommand = new JTextField();
        txtCommand.setText("Command");
        txtCommand.setBounds(522, 2, 130, 26);
        panel_1.add(txtCommand);
        txtCommand.setColumns(10);

        JButton btnNewButton = new JButton("Do it!");
        btnNewButton.setBounds(652, 2, 117, 29);
        panel_1.add(btnNewButton);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 39, 340, 114);
        panel_1.add(scrollPane);

        JTextArea txtrSampletext = new JTextArea();
        txtrSampletext.setText("sampleText");
        scrollPane.setViewportView(txtrSampletext);

        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(414, 39, 340, 114);
        panel_1.add(scrollPane_1);

        commandRestuls = new JTextArea();
        commandRestuls.setText("sample text");
        commandRestuls.setColumns(25);
        scrollPane_1.setViewportView(commandRestuls);
    }

    private boolean createConnection() {
        try {
             client = new FTPClient(serverIpInput.getText(), portInput.getText(),
                    userNameInput.getText(), hostNameInput.getText(), speedSelector.getSelectedItem().toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void actionPerformed(ActionEvent e) {
        JComponent pressed = (JComponent)e.getSource();

        if (pressed == btnConnect) {
            createConnection();
        }
    }
}