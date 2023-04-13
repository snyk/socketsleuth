import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.WebSocketMessageEditor;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class WSIntruder implements ContainerProvider {
    private MontoyaApi api;
    private WebSocketMessageEditor messageEditor;
    private JPanel container;

    @Override
    public void handleData(Object data) {
        if (data instanceof byte[]) {
            byte[] byteArray = (byte[]) data;
            if (this.messageEditor == null) {
                return;
            }
            // Not so nice but setContents uses custom ByteArray
            this.messageEditor.setContents(ByteArray.byteArray(new String((byte[]) data)));

            // Also not so nice, it results in two panels being created - this is because the
            // JSONRPC auto detect is run during creation of the panel, and in the first time
            // there is no data in the WS editor. This is a hack to auto detect when added via
            // right click context menu.
            setWsIntruderPanel(constructJSONRPCMethodPanel());
        }
    }

    private JComboBox attackTypeCombo;
    private JPanel wsIntruderPanel;

    private JPanel attackTypePanel;

    public JPanel getContainer() {
        return container;
    }

    public JPanel getAttackTypePanel() {
        return attackTypePanel;
    }

    public JComboBox getAttackTypeCombo() {
        return attackTypeCombo;
    }

    public WSIntruder(MontoyaApi api) {
        this.api = api;
        this.messageEditor = api.userInterface().createWebSocketMessageEditor();

        this.getAttackTypeCombo().setModel(new DefaultComboBoxModel<>(WSIntruderType.values()));
        this.getAttackTypeCombo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WSIntruderType selectedOption = (WSIntruderType) getAttackTypeCombo().getSelectedItem();
                switch (selectedOption) {
                    case JSONRPCMETHOD:
                        api.logging().logToOutput("its the method");
                        setWsIntruderPanel(constructJSONRPCMethodPanel());
                        break;
                    case JSONRPCPARAM:
                        api.logging().logToOutput("its the param");
                        setWsIntruderPanel(constructJSONRPCParamPanel());
                        break;
                    case SNIPER:
                        api.logging().logToOutput("its the sniper");
                        setWsIntruderPanel(constructJSONRPCMethodPanel());
                    default:
                        break;
                }
            }
        });
        this.setWsIntruderPanel(constructJSONRPCMethodPanel());
    }

    private JPanel constructJSONRPCParamPanel() {
        return new JSONRPCParamBruteIntruder().getContainer();
    }

    private JPanel constructJSONRPCMethodPanel() {
        Color lightGreen = new Color(0, 204, 102);
        JSONRPCIntruder jsonrpcIntruder = new JSONRPCIntruder();
        jsonrpcIntruder.getPayloadContainer().add(this.messageEditor.uiComponent());
        jsonrpcIntruder.getPayloadContainer().revalidate();
        jsonrpcIntruder.getPayloadContainer().repaint();

        jsonrpcIntruder.getAutoDetectButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jsonrpcIntruder.attemptAutoDetectJSONRPC(messageEditor);
            }
        });

        jsonrpcIntruder.getAddButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ByteArray message = messageEditor.getContents();
                try {
                    JSONObject jsonObj = new JSONObject(new String(message.getBytes()));
                    String method = jsonObj.getString(jsonrpcIntruder.getFieldText().getText());
                    jsonrpcIntruder.getMethodLabel().setText(jsonrpcIntruder.getFieldText().getText());
                    jsonrpcIntruder.getMethodLabel().setForeground(lightGreen);
                } catch (JSONException ex) {
                    // Unable to detect
                    jsonrpcIntruder.getMethodLabel().setText("unable to select");
                    jsonrpcIntruder.getMethodLabel().setForeground(Color.RED);
                }
            }
        });

        // Default wordlist
        DefaultListModel<String> listModel = new DefaultListModel<>();
        jsonrpcIntruder.getMethodWordlist().setModel(listModel);
        populateDefaultWordlist(jsonrpcIntruder.getMethodWordlist());

        jsonrpcIntruder.getRemoveButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedIndices = jsonrpcIntruder.getMethodWordlist().getSelectedIndices();
                for (int i = selectedIndices.length - 1; i >= 0; i--) {
                    listModel.remove(selectedIndices[i]);
                }
            }
        });

        jsonrpcIntruder.getAddWordlistItemButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = jsonrpcIntruder.getWordlistManualText().getText();
                if (text.trim().equals("")) {
                    return;
                }

                listModel.addElement(text);
                jsonrpcIntruder.getWordlistManualText().setText("");
            }
        });

        jsonrpcIntruder.getClearButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
            }
        });

        jsonrpcIntruder.getResetDefaultButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.clear(); // Clear the existing items from the listModel
                populateDefaultWordlist(jsonrpcIntruder.getMethodWordlist());
            }
        });

        jsonrpcIntruder.getPasteButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clipboard = "";
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    try {
                        clipboard = (String) systemClipboard.getData(DataFlavor.stringFlavor);
                        String[] lines = clipboard.split("\\r?\\n");
                        listModel.addAll(Arrays.stream(lines).collect(Collectors.toList()));
                    } catch (UnsupportedFlavorException ex) {
                        throw new RuntimeException(ex);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        jsonrpcIntruder.attemptAutoDetectJSONRPC(this.messageEditor);
        api.logging().logToOutput("we tried to auto detect");
        return jsonrpcIntruder.getContainer();
    }

    private void populateDefaultWordlist(JList<String> list) {
        DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
        try {
            // Get the InputStream for the embedded resource file
            InputStream is = getClass().getResourceAsStream("/jsonrpc.txt");
            if (is == null) {
                throw new IOException("Resource file not found: /jsonrpc.txt");
            }

            // Create a BufferedReader to read the file line by line
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                listModel.addElement(line);
            }

            reader.close(); // Close the BufferedReader
        } catch (IOException ex) {
            // Handle the exception, e.g., show an error message, log the error, etc.
            ex.printStackTrace();
        }
    }

    public void setWsIntruderPanel(JPanel intruderPanel) {
        this.wsIntruderPanel.removeAll();
        this.wsIntruderPanel.add(intruderPanel);
        this.wsIntruderPanel.revalidate();
        this.wsIntruderPanel.repaint();
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
        container.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        container.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Choose an attack type");
        container.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Attack type:");
        container.add(label2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        container.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(1, 10), null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        container.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(-1, 20), null, null, 0, false));
        attackTypeCombo = new JComboBox();
        container.add(attackTypeCombo, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        wsIntruderPanel = new JPanel();
        wsIntruderPanel.setLayout(new CardLayout(0, 0));
        container.add(wsIntruderPanel, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
