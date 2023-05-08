import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.WebSocketMessageEditor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

public class AutoRepeaterTab implements ContainerProvider {
    private MontoyaApi api;
    private JPanel container;
    private JSplitPane primarySplit;
    private JSplitPane configurationSplit;
    private JTable connectionConfigTable;
    private JButton selectSourceButton;
    private JButton selectTargetButton;
    private JButton activateWSAutoRepeaterButton;
    private JComboBox directionCombo;
    private JTabbedPane requestTabbedPane;
    private JLabel selectedSrcLabel;
    private JLabel selectedDstLabel;
    private TableModel sourceTableModel;
    private TableModel targetTableModel;

    private int selectedSocketId;
    private int selectedTargetId;

    AutoRepeaterTableModel tableModel;

    public AutoRepeaterTab(int tabID, MontoyaApi api, TableModel tableModel) {
        this.api = api;
        this.tableModel = new AutoRepeaterTableModel((AbstractTableModel) tableModel);
        this.connectionConfigTable.setModel(tableModel);

        TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(this.tableModel);

        primarySplit.setDividerLocation(169);
        configurationSplit.setDividerLocation(1000);

        // Build tabs
        // Tab -> SplitPane: Left (AutoRepeaterMessageTable) Right (MessageEditor)
        JSplitPane originalSocketSplit = new JSplitPane();
        AutoRepeaterMessageTable messageTable = new AutoRepeaterMessageTable();
        //messageTable.getMessageTable().setModel(tableModel);
        WebSocketMessageEditor messageEditor = this.api.userInterface().createWebSocketMessageEditor(EditorOptions.READ_ONLY);

        // Assign components
        originalSocketSplit.setLeftComponent(messageTable.getContainer());
        originalSocketSplit.setRightComponent(messageEditor.uiComponent());
        originalSocketSplit.setDividerLocation(800);
        requestTabbedPane.addTab("Source Socket", originalSocketSplit);


        /*rowSorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                boolean isActive = (Boolean) entry.getValue(4);

                return isActive;
            }
        });*/

        //this.connectionConfigTable.setRowSorter(rowSorter);

        DefaultComboBoxModel<String> cbModel = new DefaultComboBoxModel<String>();
        cbModel.addElement("Client to Server");
        cbModel.addElement("Server to Client");
        cbModel.addElement("Bidirectional");
        this.directionCombo.setModel(cbModel);

        setButtonEvents();
    }

    private boolean validateWebsocketSelection(int selectedRow) {
        boolean active = (boolean) connectionConfigTable.getModel().getValueAt(selectedRow, 3);
        if (!active) {
            JOptionPane.showMessageDialog(
                    null,
                    "WebSocket is no longer active! You must select an active WebSocket",
                    "Inactive WebSocket", JOptionPane.WARNING_MESSAGE
            );
        }
        return active;
    }

    private void setButtonEvents() {
        // Source socket button
        this.selectSourceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = connectionConfigTable.getSelectedRow();
                TableModel tm = connectionConfigTable.getModel();

                if (selectedRow >= 0) {
                    if (!validateWebsocketSelection(selectedRow)) {
                        return;
                    }

                    int socketId = (int) tm.getValueAt(selectedRow, 0);
                    selectedSrcLabel.setText(Integer.toString(socketId));
                    selectedSocketId = socketId;
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "Please select a WebSocket from the table.",
                            "No row selected", JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        // Target socket button
        this.selectTargetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = connectionConfigTable.getSelectedRow();
                TableModel tm = connectionConfigTable.getModel();

                if (selectedRow >= 0) {
                    if (!validateWebsocketSelection(selectedRow)) {
                        return;
                    }

                    int socketId = (int) tm.getValueAt(selectedRow, 0);
                    selectedDstLabel.setText(Integer.toString(socketId));
                    selectedTargetId = socketId;
                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "Please select a WebSocket from the table.",
                            "No row selected", JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });
    }

    public JPanel getContainer() {
        return container;
    }

    public JSplitPane getPrimarySplit() {
        return primarySplit;
    }

    public JSplitPane getConfigurationSplit() {
        return configurationSplit;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        container = new JPanel();
        container.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        container.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        primarySplit = new JSplitPane();
        primarySplit.setDividerLocation(179);
        primarySplit.setOrientation(0);
        container.add(primarySplit, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        configurationSplit = new JSplitPane();
        configurationSplit.setDividerLocation(174);
        primarySplit.setLeftComponent(configurationSplit);
        final JScrollPane scrollPane1 = new JScrollPane();
        configurationSplit.setLeftComponent(scrollPane1);
        connectionConfigTable = new JTable();
        scrollPane1.setViewportView(connectionConfigTable);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        configurationSplit.setRightComponent(panel1);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Source Socket:");
        panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectedSrcLabel = new JLabel();
        selectedSrcLabel.setText("None selected");
        panel1.add(selectedSrcLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectSourceButton = new JButton();
        selectSourceButton.setText("Select");
        panel1.add(selectSourceButton, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.BOLD, -1, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Target Socket:");
        panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectedDstLabel = new JLabel();
        selectedDstLabel.setText("None selected");
        panel1.add(selectedDstLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectTargetButton = new JButton();
        selectTargetButton.setText("Select");
        panel1.add(selectTargetButton, new com.intellij.uiDesigner.core.GridConstraints(3, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        activateWSAutoRepeaterButton = new JButton();
        activateWSAutoRepeaterButton.setBackground(new Color(-65536));
        activateWSAutoRepeaterButton.setForeground(new Color(-12935007));
        activateWSAutoRepeaterButton.setText("Activate WS Auto Repeater");
        panel1.add(activateWSAutoRepeaterButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, Font.BOLD, -1, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Direction:");
        panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        directionCombo = new JComboBox();
        panel1.add(directionCombo, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        requestTabbedPane = new JTabbedPane();
        primarySplit.setRightComponent(requestTabbedPane);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return container;
    }

}
