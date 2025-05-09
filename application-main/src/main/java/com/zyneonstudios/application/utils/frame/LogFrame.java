package com.zyneonstudios.application.utils.frame;

import com.formdev.flatlaf.FlatDarkLaf;
import com.zyneonstudios.ApplicationMain;
import com.zyneonstudios.application.Application;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class LogFrame extends JFrame {

    private final JTextArea textArea;
    final Color foreground = Color.decode("#bbbbbb");
    final Color background = Color.decode("#3c3f41");

    public LogFrame(InputStream inputStream, String version) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setIconImage(ImageIO.read(Objects.requireNonNull(getClass().getResource("/logo.png"))).getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[LOGFRAME] Couldn't style log output frame: "+e.getMessage());
        }

        setTitle(version+" log output");
        textArea = new JTextArea();
        textArea.setBackground(background);
        textArea.setForeground(foreground);
        textArea.setCaretColor(Color.white);
        textArea.setSelectionColor(Color.white);
        textArea.setSelectedTextColor(Color.black);
        textArea.setDisabledTextColor(Color.gray);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBackground(background);
        scrollPane.setForeground(foreground);
        scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(900,440));
        setSize(new Dimension(Application.getFrame().getWidth()-60,Application.getFrame().getHeight()-60));
        setLocation(Application.getFrame().getX()+30,Application.getFrame().getY()+30);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setState(JFrame.ICONIFIED);
            }
        });


        Thread updateThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    appendToConsole(line + "\n");
                }
            } catch (IOException e) {
                ApplicationMain.getLogger().err("[LOGFRAME] Couldn't start log output update thread: "+e.getMessage());
            }
        });

        updateThread.start();
        try {
            FlatDarkLaf.setup();
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            ApplicationMain.getLogger().err("[LOGFRAME] Couldn't reset styling: "+e.getMessage());
        }
        setVisible(true);
    }

    public void onStop() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(Application.getFrame().getWidth()-60,Application.getFrame().getHeight()-60));
        setLocation(Application.getFrame().getX()+30,Application.getFrame().getY()+30);
        appendToConsole("============================================================\n");
        appendToConsole("The game is closed. You can close this window now!\n");
        appendToConsole("You can find the saved log file in the instance path.\n");
        appendToConsole("============================================================");
        setState(JFrame.NORMAL);
    }

    private void appendToConsole(String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }

    private class CustomScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            // Ändern Sie hier die Farben der Scrollbar
            Color foreground = Color.decode("#343538");
            thumbColor = foreground;
            thumbDarkShadowColor = background;
            thumbHighlightColor = foreground;
            thumbLightShadowColor = foreground;
            trackColor = background;
            trackHighlightColor = Color.gray;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton("zero button");
            Dimension zeroDim = new Dimension(0, 0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }
    }
}
