import java.awt.EventQueue;

import javax.swing.JFrame;

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

import java.awt.Font;
import java.awt.Color;

public class MainGUI implements ActionListener{

    private JFrame frame;
    private JTextField serverIpInput;
    private JTextField textPanelPort;
    private JTextField UserName;
    private JTextField txtHostName;
    private JTextField textField;
    private JTextField searchResults;
    private JTextField txtCommand;
    private JTextField responseTextArea;
    private JButton btnConnect;

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
        frame.setBounds(100, 100, 780, 400);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel lblIp = new JLabel("Server IP:");
        panel.add(lblIp);

        serverIpInput = new JTextField();
        serverIpInput.setText("192.168.0.1");
        panel.add(serverIpInput);
        serverIpInput.setColumns(8);

        JLabel lblPort = new JLabel("Port:");
        panel.add(lblPort);

        textPanelPort = new JTextField();
        textPanelPort.setText("port");
        panel.add(textPanelPort);
        textPanelPort.setColumns(5);

        JLabel lblUsername = new JLabel("Name:");
        panel.add(lblUsername);

        UserName = new JTextField();
        UserName.setText("userName");
        panel.add(UserName);
        UserName.setColumns(10);

        JLabel lblHostname = new JLabel("HostName:");
        panel.add(lblHostname);

        txtHostName = new JTextField();
        txtHostName.setText("HostName");
        panel.add(txtHostName);
        txtHostName.setColumns(10);

        JComboBox dropDown = new JComboBox();
        dropDown.setModel(new DefaultComboBoxModel(new String[] {"Ethernet", "T1"}));
        panel.add(dropDown);

        JComboBox comboBox = new JComboBox();
        comboBox.setModel(new DefaultComboBoxModel(new String[] {"T1", "Ethernet"}));
        comboBox.setSelectedIndex(0);
        panel.add(comboBox);

        btnConnect = new JButton("Connect");
        panel.add(btnConnect);

        JLabel lblNewLabel = new JLabel("");
        panel.add(lblNewLabel);

        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);

        JLabel lblSearchMeBitch = new JLabel("Keyword:");
        lblSearchMeBitch.setBounds(20, 0, 62, 30);
        panel_1.add(lblSearchMeBitch);

        textField = new JTextField();
        textField.setBounds(90, 0, 150, 30);
        panel_1.add(textField);
        textField.setColumns(10);

        JButton btnSearch = new JButton("Search");
        btnSearch.setBounds(0, 0, 0, 0);
        panel_1.add(btnSearch);
        frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
        frame.getContentPane().add(panel);
        frame.getContentPane().add(panel_1);

        JButton btnSearch_1 = new JButton("Search");
        btnSearch_1.setBounds(243, 2, 117, 29);
        panel_1.add(btnSearch_1);

        searchResults = new JTextField();
        searchResults.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        searchResults.setBounds(20, 42, 340, 114);
        panel_1.add(searchResults);
        searchResults.setColumns(100);

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

        responseTextArea = new JTextField();
        responseTextArea.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
        responseTextArea.setColumns(100);
        responseTextArea.setBounds(424, 42, 340, 114);
        panel_1.add(responseTextArea);
    }

    public void actionPerformed(ActionEvent e) {

    }
}
