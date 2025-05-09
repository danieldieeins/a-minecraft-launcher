package com.zyneonstudios.application.utils.frame.web;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomWebFrame extends ZyneonWebFrame {

    private static Point mouseDownCompCoords;
    private String title;
    private JPanel titleBar;
    private boolean border;

    public CustomWebFrame(String url) {
        super(url);
        title = "  NEXUS App";
        setUndecorated(true);
        JPanel customTitleBar = createCustomTitleBar();
        getContentPane().add(customTitleBar, BorderLayout.NORTH);
        addDragAndDropFunctionality();
        setBorder(true);
        pack();
    }

    @Override
    public void setTitlebar(String title, Color background, Color foreground) {
        setTitle(title);
        setTitleBackground(background);
        setTitleForeground(foreground);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        this.title = title;
    }

    @Override
    public void setTitleBackground(Color color) {
        setBackground(color);
        titleBar.setBackground(color);
    }

    @Override
    public void setTitleForeground(Color color) {
        titleBar.setForeground(color);
    }

    private JPanel createCustomTitleBar() {
        titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.BLACK);
        titleBar.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setBackground(null);
        buttonPanel.setForeground(null);

        JButton minimizeButton = new JButton("-");
        minimizeButton.setPreferredSize(new Dimension(30,30));
        minimizeButton.addActionListener(e -> setState(ICONIFIED));
        minimizeButton.setBorder(null);
        minimizeButton.setBackground(null);
        minimizeButton.setForeground(null);
        buttonPanel.add(minimizeButton);

        JButton maximizeButton = new JButton("◻");
        maximizeButton.setPreferredSize(new Dimension(30,30));
        maximizeButton.setBorder(null);
        maximizeButton.setBackground(null);
        maximizeButton.setForeground(null);
        maximizeButton.addActionListener(e -> toggleMaximizeState());
        buttonPanel.add(maximizeButton);

        JButton closeButton = new JButton("X");
        closeButton.setPreferredSize(new Dimension(30,30));
        closeButton.setBorder(null);
        closeButton.setBackground(null);
        closeButton.setForeground(null);
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(Color.RED);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(null);
            }
        });
        closeButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(closeButton);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setBackground(null);
        titleLabel.setForeground(null);

        titleBar.add(buttonPanel, BorderLayout.EAST);
        titleBar.add(titleLabel, BorderLayout.CENTER);

        return titleBar;
    }

    private void toggleMaximizeState() {
        if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
            setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
        } else {
            setExtendedState(getExtendedState() & ~MAXIMIZED_BOTH);
        }
        toggleBorder();
    }

    private void addDragAndDropFunctionality() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
                    if (isWithinTitleBar(e)) {
                        mouseDownCompCoords = e.getPoint();
                    }
                } else {
                    setExtendedState(NORMAL);
                    setBorder(true);
                    mouseDownCompCoords = e.getPoint();
                }
            }
            private boolean isWithinTitleBar(MouseEvent e) {
                return (e.getY() < titleBar.getHeight() && e.getX() > 0 && e.getX() < getWidth());
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isWithinTitleBar(e)) {
                    Point currCoords = e.getLocationOnScreen();
                    setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
                }
            }

            private boolean isWithinTitleBar(MouseEvent e) {
                return (e.getY() < titleBar.getHeight() && e.getX() > 0 && e.getX() < getWidth());
            }
        });
    }

    private void toggleBorder() {
        setBorder(!border);
    }

    private void setBorder(boolean state) {
        if (state) {
            getRootPane().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        } else {
            getRootPane().setBorder(null);
        }
        border = state;
    }
}