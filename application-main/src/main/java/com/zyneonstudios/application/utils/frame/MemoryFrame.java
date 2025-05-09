package com.zyneonstudios.application.utils.frame;

import com.sun.management.OperatingSystemMXBean;
import com.zyneonstudios.application.Application;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;

public class MemoryFrame extends JFrame {

    private final JsonStorage saveFile;
    private final String title;
    private String instance;
    private final String id;

    public MemoryFrame(JsonStorage saveFile, String title, String instance) {
        id = instance;
        this.saveFile = saveFile;
        this.title = title;
        this.instance = instance;
        SwingUtilities.invokeLater(this::init);
    }

    private void init() {
        if(instance!=null) {
            instance = instance.replace("/","").replace(".","");
        }
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        setLayout(new FlowLayout());

        OperatingSystemMXBean os = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        long c = 1024L*1024L;
        long maxValue = os.getTotalMemorySize() /c;

        int minValue = 0;
        int initialValue = Application.memory;
        if(instance!=null) {
            if(!instance.equalsIgnoreCase("default")) {
                if (saveFile.get("settings.memory") != null) {
                    initialValue = saveFile.getInteger("settings.memory");
                }
            }
        }
        JSlider slider = new JSlider(minValue, (int)maxValue, initialValue);
        double alleWieViel = (double)maxValue/2;
        slider.setMajorTickSpacing((int)alleWieViel);
        slider.setMinorTickSpacing((int)alleWieViel/2);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JTextField textField = new JTextField(10);
        textField.setText(initialValue+"");
        JLabel label = new JLabel("MB");
        slider.addChangeListener(e -> {
            textField.setText(String.valueOf(slider.getValue()));
        });

        textField.addActionListener(e -> {
            try {
                int value = Integer.parseInt(textField.getText());
                if (value >= minValue && value <= maxValue) {
                    slider.setValue(value);
                } else {
                    textField.setText(String.valueOf(slider.getValue()));
                }
            } catch (NumberFormatException ex) {
                textField.setText(String.valueOf(slider.getValue()));
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                save(slider);
            }
        });
        Dimension d = new Dimension(380,92);
        setMinimumSize(d);
        setSize(d);
        setResizable(false);
        setLocationRelativeTo(null);
        add(slider);
        add(textField);
        add(label);
        pack();
        setTitle(title);
        setVisible(true);
    }

    private void save(JSlider slider) {
        String path = "settings.memory.default";
        if(instance!=null) {
            if(!instance.equalsIgnoreCase("default")) {
                if(!instance.equalsIgnoreCase("")) {
                    path = "settings.memory";
                }
            }
        }
        if(path.equalsIgnoreCase("settings.memory.default")) {
            Application.memory = slider.getValue();
        }
        saveFile.set(path,slider.getValue());
        if(instance.isEmpty()||instance.equals("default")) {
            Application.getFrame().getConnector().resolveRequest("sync.settings.global");
            return;
        }
        Application.getFrame().getConnector().resolveRequest("button.instance."+id);
    }
}