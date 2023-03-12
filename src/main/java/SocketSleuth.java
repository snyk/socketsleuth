import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.RawEditor;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class SocketSleuth implements BurpExtension {

    MontoyaApi api;
    SleuthUI uiForm;
    SettingsUI settingsUI;
    WebSocketConnectionTableModel tableModel;

    WebSocketInterceptionRulesTableModel interceptionRulesModel;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        // Main table model for websocket connections
        this.tableModel = new WebSocketConnectionTableModel();
        this.interceptionRulesModel = new WebSocketInterceptionRulesTableModel();

        api.extension().setName("SocketSleuth");
        api.userInterface().registerSuiteTab("SocketSleuth", constructBurpUi());

        // Create handler for new websocket connections
        // The table might not exist yet, check if there is bugs
        MyProxyWebSocketCreationHandler exampleWebSocketCreationHandler = new MyProxyWebSocketCreationHandler(api,
                this.tableModel,
                this.uiForm.getConnectionTable(),
                this.uiForm.getStreamTable(),
                this.interceptionRulesModel
        );
        api.proxy().registerWebSocketCreationHandler(exampleWebSocketCreationHandler);
    }

    private Component constructSettingsTab() {
        settingsUI = new SettingsUI();
        settingsUI.getInterceptTable().setModel(this.interceptionRulesModel);
        settingsUI.getMatchReplaceTable().setModel(new WebSocketInterceptionRulesTableModel());

        // Everything related to the "add" button for interception rules
        settingsUI.getAddInterceptButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InterceptEditWindow form = new InterceptEditWindow();
                JDialog popupDialog = new JDialog();
                popupDialog.add(form.getContainer()); // add the formPanel to the popupDialog
                popupDialog.setSize(400, 180);
                popupDialog.setLocationRelativeTo(null);

                form.getCancelButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popupDialog.dispose();
                    }
                });

                form.getOkButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        WebSocketInterceptionRulesTableModel.MatchType selectedMatchType =
                                (WebSocketInterceptionRulesTableModel.MatchType) form.getMatchTypeCombo().getSelectedItem();

                        WebSocketInterceptionRulesTableModel.Direction selectedDirection =
                                (WebSocketInterceptionRulesTableModel.Direction) form.getDirectionCombo().getSelectedItem();

                        WebSocketInterceptionRulesTableModel rulesTableModel = (WebSocketInterceptionRulesTableModel) settingsUI.getInterceptTable().getModel();

                        if (form.getConditionTextField().getText().trim() == "") return;

                        Object[] newRow = new Object[] {
                                true, // enabled
                                selectedMatchType, // match type
                                selectedDirection, // direction
                                form.getConditionTextField().getText() // condition
                        };

                        rulesTableModel.addRow(newRow);
                        api.logging().logToOutput("adding new row to table: " + interceptionRulesModel.getRowCount());
                        rulesTableModel.fireTableDataChanged();
                        popupDialog.dispose();
                    }
                });

                EnumSet.allOf(WebSocketInterceptionRulesTableModel.MatchType.class).forEach(form.getMatchTypeCombo()::addItem);
                EnumSet.allOf(WebSocketInterceptionRulesTableModel.Direction.class).forEach(form.getDirectionCombo()::addItem);

                popupDialog.setVisible(true);
            }
        });

        // All "edit" button related stuff
        settingsUI.getEditInterceptButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                api.logging().logToOutput("edit clicked");
                int selectedRowIndex = settingsUI.getInterceptTable().getSelectedRow();
                api.logging().logToOutput("row: " + selectedRowIndex);

                WebSocketInterceptionRulesTableModel tableModel
                        = (WebSocketInterceptionRulesTableModel) settingsUI.getInterceptTable().getModel();


                // if (selectedRowIndex == -1) return;;

                // Get current values
                WebSocketInterceptionRulesTableModel.MatchType matchType
                        = (WebSocketInterceptionRulesTableModel.MatchType) tableModel.getValueAt(selectedRowIndex, 1);

                WebSocketInterceptionRulesTableModel.Direction direction
                        = (WebSocketInterceptionRulesTableModel.Direction) tableModel.getValueAt(selectedRowIndex, 2);

                String condition = (String) tableModel.getValueAt(selectedRowIndex, 3);

                // Prepare and show window
                InterceptEditWindow form = new InterceptEditWindow();
                JDialog popupDialog = new JDialog();
                popupDialog.add(form.getContainer()); // add the formPanel to the popupDialog
                popupDialog.setSize(400, 180);
                popupDialog.setLocationRelativeTo(null);

                EnumSet.allOf(WebSocketInterceptionRulesTableModel.MatchType.class).forEach(form.getMatchTypeCombo()::addItem);
                EnumSet.allOf(WebSocketInterceptionRulesTableModel.Direction.class).forEach(form.getDirectionCombo()::addItem);

                form.getMatchTypeCombo().setSelectedItem(matchType);
                form.getDirectionCombo().setSelectedItem(direction);
                form.getConditionTextField().setText(condition);

                form.getCancelButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popupDialog.dispose();
                    }
                });

                form.getOkButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Get form current values
                        WebSocketInterceptionRulesTableModel.MatchType selectedMatchType =
                                (WebSocketInterceptionRulesTableModel.MatchType) form.getMatchTypeCombo().getSelectedItem();

                        WebSocketInterceptionRulesTableModel.Direction selectedDirection =
                                (WebSocketInterceptionRulesTableModel.Direction) form.getDirectionCombo().getSelectedItem();

                        String condition = form.getConditionTextField().getText();

                        WebSocketInterceptionRulesTableModel rulesTableModel = (WebSocketInterceptionRulesTableModel) settingsUI.getInterceptTable().getModel();

                        // Set in model
                        tableModel.setValueAt(selectedMatchType, selectedRowIndex, 1);
                        tableModel.setValueAt(selectedDirection, selectedRowIndex, 2);
                        tableModel.setValueAt(condition, selectedRowIndex, 3);

                        popupDialog.dispose();
                    }
                });

                popupDialog.setVisible(true);
            }
        });

        // All "remove" intercept functionality
        settingsUI.getRemoveInterceptButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = settingsUI.getInterceptTable().getSelectedRows();
                WebSocketInterceptionRulesTableModel tableModel =
                        (WebSocketInterceptionRulesTableModel) settingsUI.getInterceptTable().getModel();

                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    tableModel.removeRow(selectedRows[i]);
                }
                tableModel.fireTableDataChanged();
            }
        });

        return settingsUI.getContainer();
    }
    private void bindMessageTableContextMenu(JTable table) {
        // Create a popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem commentItem = new JMenuItem("Add comment");
        JMenuItem clearItem = new JMenuItem("Clear history");
        JMenuItem intruderItem = new JMenuItem("Send to WS intruder");
        JMenuItem autoRepeaterItem = new JMenuItem("Send to WS auto-repeater");

        popupMenu.add(commentItem);
        popupMenu.add(clearItem);
        popupMenu.add(intruderItem);
        popupMenu.add(autoRepeaterItem);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Select the row that was clicked
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);
                    } else {
                        table.clearSelection();
                    }

                    // Show the popup menu
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private Component constructBurpUi() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("History", this.constructHistoryTab());
        tabs.addTab("WS Intruder", new JPanel());
        tabs.addTab("WS auto-repeater", new JPanel());
        tabs.addTab("Settings", this.constructSettingsTab());
        return tabs;
    }
    private Component constructHistoryTab() {
        /*JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        JTabbedPane tabs = new JTabbedPane();

        UserInterface ui = this.api.userInterface();

        HttpRequestEditor requestViewer = ui.createHttpRequestEditor(READ_ONLY);
        HttpResponseEditor responseViewer = ui.createHttpResponseEditor(READ_ONLY);

        tabs.addTab("Requests", requestViewer.uiComponent());
        tabs.addTab("Response", responseViewer.uiComponent());*/

        // Create new instance of UI and start preparing
        uiForm = new SleuthUI();
        JTable table = uiForm.getConnectionTable();

        // Set response editor
        UserInterface ui = this.api.userInterface();
        // Super annoying - ui.createWebSocketMessageEditor exists but isn't implemented. Use raw editor for now
        RawEditor messageViewer = ui.createRawEditor(READ_ONLY);
        uiForm.setStreamVIewSplitPane(messageViewer.uiComponent());
        messageViewer.setContents(ByteArray.byteArray("hello world"));

        // Set a dummy model for WebSocket Message / steams with no data.
        this.uiForm.getStreamTable().setModel(new WebSocketStreamTableModel());

        // Set table model for WebSocket connections
        table.setModel(this.tableModel);

        // Handle table row selections
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // valueChanged is fired twice, once when selection is first made, once when selection is cleared
                if (e.getValueIsAdjusting()) {
                    return;
                }

                int selectedRowIndex = table.getSelectedRow();
                // Not sure why this is happening
                if (selectedRowIndex == -1) {
                    api.logging().raiseInfoEvent("selectedRowIndex is -1");
                    return;
                }

                // get data out of model
                int socketId = (int) table.getValueAt(selectedRowIndex, 0);
                api.logging().logToOutput("we have clicked on socket: " + socketId);

                // Update the stream / message view
                WebSocketConnectionTableModel connectionTableModel = (WebSocketConnectionTableModel) table.getModel();
                WebsocketConnectionTableRow row = connectionTableModel.getConnection(selectedRowIndex);
                uiForm.setSelectedSocketLabel(socketId, row.getUrl());
                uiForm.getStreamTable().setModel(row.getStreamModel());
            }
        });

        // Set onchange for stream table
        ListSelectionModel messageSelectionModel = uiForm.getStreamTable().getSelectionModel();
        messageSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // valueChanged is fired twice, once when selection is first made, once when selection is cleared
                if (e.getValueIsAdjusting()) {
                    return;
                }

                JTable messageTable = uiForm.getStreamTable();
                int selectedRowIndex = messageTable.getSelectedRow();
                // Not sure why this is happening
                if (selectedRowIndex == -1) {
                    api.logging().raiseInfoEvent("selectedRowIndex is -1");
                    return;
                }


                int messageId = (int) messageTable.getValueAt(selectedRowIndex, 0);
                api.logging().logToOutput("nessage id " + messageId);
                WebSocketStreamTableModel messageTableModel = (WebSocketStreamTableModel) messageTable.getModel();
                messageViewer.setContents(ByteArray.byteArray(messageTableModel.getStream(selectedRowIndex).getRawMessage()));
                //messageViewer.setContents(ByteArray.byteArray("ffs + " + messageId));
                api.logging().logToOutput("hmmmmm");

            }
        });

        this.bindMessageTableContextMenu(uiForm.getStreamTable());

        // Autosize the table columns - Doesn't work, look at later
        uiForm.getConnectionTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        uiForm.getConnectionTable().setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        TableColumnModel cm = uiForm.getConnectionTable().getColumnModel();
        cm.getColumn(0).setPreferredWidth(30);
        cm.getColumn(1).setPreferredWidth(150);
        cm.getColumn(2).setPreferredWidth(30);
        cm.getColumn(3).setPreferredWidth(10);

        //splitPane.setLeftComponent(uiForm.getContainer());
        //splitPane.setRightComponent(tabs);
        return uiForm.getContainer();
    }
}
