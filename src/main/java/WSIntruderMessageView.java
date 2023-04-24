import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.editor.EditorOptions;
import burp.api.montoya.ui.editor.WebSocketMessageEditor;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class WSIntruderMessageView {
    private JPanel container;
    private MontoyaApi api;
    private JSplitPane resultSplitPane;
    private JTable messageTable;

    private WebSocketMessageEditor messageEditor;

    private JSONRPCMessageTableModel tableModel;

    public WSIntruderMessageView(MontoyaApi api) {
        this.messageEditor = api.userInterface().createWebSocketMessageEditor(EditorOptions.READ_ONLY);
        this.api = api;
        this.resultSplitPane.setRightComponent(this.messageEditor.uiComponent());

        this.tableModel = new JSONRPCMessageTableModel();
        messageTable.setModel(this.tableModel);

        this.messageTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = messageTable.getSelectedRow();
                    if (selectedRow != -1) {
                        JSONRPCMessageTableModel model = (JSONRPCMessageTableModel) messageTable.getModel();
                        JSONRPCMessage message = model.getMessage(selectedRow);
                        messageEditor.setContents(ByteArray.byteArray(message.getMessage()));
                    }
                }
            }
        });
    }

    public JSONRPCMessageTableModel getTableModel() {
        return tableModel;
    }

    public JPanel getContainer() {
        return container;
    }

    public JSplitPane getResultSplitPane() {
        return resultSplitPane;
    }

    public JTable getMessageTable() {
        return messageTable;
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
        resultSplitPane = new JSplitPane();
        container.add(resultSplitPane, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        resultSplitPane.setLeftComponent(scrollPane1);
        messageTable = new JTable();
        scrollPane1.setViewportView(messageTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return container;
    }
}
