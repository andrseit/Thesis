package agents.gui;

import jade.core.Agent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Darling on 27/9/2017.
 */
public class AgentUI extends JFrame {

    JPanel main_panel;
    private JTextArea console;
    private JTextArea clock_text;
    JLabel title;
    private JScrollPane scrollPane;
    private JScrollPane clock_scrollPane;
    private Agent agent;
    private boolean suspended = false;


    public AgentUI (String agent, int width, int height, Agent a) {

        super( "Agent Console" );


        this.agent = a;
        this.setTitle(agent);


        this.setBounds(100, 100, 450, 320);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBounds(100, 100, 450, 320);
        this.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);


        console = new JTextArea("Starting...");
        clock_text = new JTextArea("Slots");
        scrollPane = new JScrollPane(console);
        scrollPane.setBounds(10, 100, 415, 170);

        clock_scrollPane = new JScrollPane(clock_text);
        clock_scrollPane.setBounds(10, 30, 85, 60);

        JButton btnPause = new JButton("Pause");
        btnPause.setBounds(350, 70, 80, 25);
        btnPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (suspended) {
                    a.doActivate();
                    suspended = false;
                    btnPause.setText("Pause");
                } else {
                    a.doSuspend();
                    suspended = true;
                    btnPause.setText("Wake");
                }
            }
        });
        panel.add(btnPause);

        panel.add( scrollPane);
        panel.add(clock_scrollPane);

        /*
        main_panel = new JPanel();
        main_panel.setPreferredSize(new Dimension(height, width));
        main_panel.setLayout( new GridLayout() );



        console = new JTextArea("Starting...");
        clock_text = new JTextArea("Slots");
        //console.setPreferredSize(new Dimension(200, 200));
        scrollPane = new JScrollPane(console);
        clock_scrollPane = new JScrollPane(clock_text);


        //scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        main_panel.add( scrollPane);
        main_panel.add(clock_scrollPane);

        setDefaultCloseOperation( EXIT_ON_CLOSE );
        */

        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }

        this.add(panel);
        //pack();
        setVisible( true );
    }

    public void appendConsole(String text) {
        console.append("\n" + text);
        console.setCaretPosition(console.getDocument().getLength());
    }

    public void appendClock (String text) {
        clock_text.append("\n" + text);
        clock_text.setCaretPosition(clock_text.getDocument().getLength());
    }

}