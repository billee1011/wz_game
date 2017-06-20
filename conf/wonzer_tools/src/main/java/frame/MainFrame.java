package frame;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import reader.ExcelReader;

public class MainFrame extends JFrame {

    private MainPanel panel;

    public MainFrame(String configPath, List<String> files) {
        panel = new MainPanel(configPath, files);

        add(panel);

        setVisible(true);

        setSize(600, 500);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}

class MainPanel extends JPanel {
    private JButton[] buttons;

    private JButton expButton;

    private final JButton buildAllButton;

    public MainPanel(final String configPath, List<String> files) {
        int length = files.size();
        buttons = new JButton[length];
        for (int i = 0; i < length; i++) {
            buttons[i] = new JButton(files.get(i));
            final JButton button = buttons[i];
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    ExcelReader.readExcel(configPath + File.separator + button.getText(), 0);

                    JOptionPane.showMessageDialog(null, "build success", "file build tools ", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            add(buttons[i]);
        }

        buildAllButton = new JButton("build all");

        final List<String> resultFiles = files;
        buildAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent f) {
                for (String string : resultFiles) {
                    ExcelReader.readExcel(configPath + string, 0);
                }

                JOptionPane.showMessageDialog(null, "build success", "file build tools ", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        expButton = new JButton("level_up");
        expButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent f) {
                JOptionPane.showMessageDialog(null, "build success", "file build tools ", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        add(expButton);
        add(buildAllButton);
        setLayout(new FlowLayout());
    }
}

