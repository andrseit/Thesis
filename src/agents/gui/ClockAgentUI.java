package agents.gui;

import agents.online.ClockAgent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Darling on 29/9/2017.
 */
public class ClockAgentUI extends JFrame {

    private JTextArea console;
    private JScrollPane scrollPane;
    private ClockAgent agent;

    public ClockAgentUI (ClockAgent agent) {

        this.agent = agent;
        this.setBounds(100, 100, 250, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        this.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);

        JButton btnStart = new JButton("Start");
        btnStart.setBounds(10, 11, 89, 23);
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agent.sendMessages();
            }
        });
        panel.add(btnStart);


        console = new JTextArea("Incoming messages...");
        console.setFont(new Font("Tahoma", Font.PLAIN, 14));
        scrollPane = new JScrollPane(console);
        scrollPane.setBounds(10, 45, 200, 100);
        panel.add(scrollPane);

        this.add(panel);
        this.setVisible(true);
    }



    public void appendConsole(String text) {
        console.append("\n" + text);
        console.setCaretPosition(console.getDocument().getLength());
    }


}
