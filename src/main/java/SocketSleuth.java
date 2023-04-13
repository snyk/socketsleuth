import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.WebSocketMessageEditor;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.PanelUI;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class SocketSleuth implements BurpExtension {

    MontoyaApi api;
    JTabbedPane socketSleuthTabPanel;
    SleuthUI uiForm;

    CustomTabbedPanel repeaterUI;
    SettingsUI settingsUI;
    WebSocketConnectionTableModel tableModel;

    WebSocketInterceptionRulesTableModel interceptionRulesModel;

    WebSocketMatchReplaceRulesTableModel matchReplaceRulesTableModel;
    SocketSleuthTabbedPanel<WSIntruder> intruderTab;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        // Main table model for websocket connections
        this.tableModel = new WebSocketConnectionTableModel();
        this.interceptionRulesModel = new WebSocketInterceptionRulesTableModel();
        this.matchReplaceRulesTableModel = new WebSocketMatchReplaceRulesTableModel();

        api.extension().setName("SocketSleuth");
        this.socketSleuthTabPanel = constructBurpUi();

        api.userInterface().registerSuiteTab("SocketSleuth", this.socketSleuthTabPanel);

        // Create handler for new websocket connections
        // The table might not exist yet, check if there is bugs
        MyProxyWebSocketCreationHandler exampleWebSocketCreationHandler = new MyProxyWebSocketCreationHandler(api,
                this.tableModel,
                this.uiForm.getConnectionTable(),
                this.uiForm.getStreamTable(),
                this.interceptionRulesModel,
                this.matchReplaceRulesTableModel
        );
        api.proxy().registerWebSocketCreationHandler(exampleWebSocketCreationHandler);
    }

    private Component constructSettingsTab() {
        settingsUI = new SettingsUI();
        settingsUI.getInterceptTable().setModel(this.interceptionRulesModel);
        settingsUI.getMatchReplaceTable().setModel(this.matchReplaceRulesTableModel);

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

        // Match and replace "add"
        settingsUI.getAddMatchReplaceButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MatchReplaceEditWindow form = new MatchReplaceEditWindow();
                JDialog popupDialog = new JDialog();
                popupDialog.add(form.getContainer()); // add the formPanel to the popupDialog
                popupDialog.setSize(410, 210);
                popupDialog.setLocationRelativeTo(null);

                EnumSet.allOf(WebSocketMatchReplaceRulesTableModel.MatchType.class).forEach(form.getMatchTypeCombo()::addItem);
                EnumSet.allOf(WebSocketMatchReplaceRulesTableModel.Direction.class).forEach(form.getDirectionCombo()::addItem);

                form.getOkButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Get selections
                        WebSocketMatchReplaceRulesTableModel.MatchType selectedMatchType =
                                (WebSocketMatchReplaceRulesTableModel.MatchType) form.getMatchTypeCombo().getSelectedItem();

                        WebSocketMatchReplaceRulesTableModel.Direction selectedDirection =
                                (WebSocketMatchReplaceRulesTableModel.Direction) form.getDirectionCombo().getSelectedItem();

                        String match = form.getMatchText().getText();
                        String replace = form.getReplaceText().getText();

                        // Create new row + add
                        Object[] newRow = new Object[] {
                                true, // enabled
                                selectedMatchType,
                                selectedDirection,
                                match,
                                replace
                        };
                        matchReplaceRulesTableModel.addRow(newRow);
                        matchReplaceRulesTableModel.fireTableDataChanged();

                        popupDialog.dispose();
                    }
                });

                form.getCancelButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popupDialog.dispose();
                    }
                });

                popupDialog.setVisible(true);
            }
        });

        // Edit button for match replace
        settingsUI.getEditMatchReplaceButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get row and current values
                int selectedRowIndex = settingsUI.getMatchReplaceTable().getSelectedRow();

                WebSocketMatchReplaceRulesTableModel.MatchType matchType
                        = (WebSocketMatchReplaceRulesTableModel.MatchType) matchReplaceRulesTableModel.getValueAt(selectedRowIndex, 1);

                WebSocketMatchReplaceRulesTableModel.Direction direction
                        = (WebSocketMatchReplaceRulesTableModel.Direction) matchReplaceRulesTableModel.getValueAt(selectedRowIndex, 2);

                String match = (String) matchReplaceRulesTableModel.getValueAt(selectedRowIndex, 3);
                String replace = (String) matchReplaceRulesTableModel.getValueAt(selectedRowIndex, 4);

                // Create form
                MatchReplaceEditWindow form = new MatchReplaceEditWindow();
                JDialog popupDialog = new JDialog();
                popupDialog.add(form.getContainer()); // add the formPanel to the popupDialog
                popupDialog.setSize(410, 210);
                popupDialog.setLocationRelativeTo(null);

                EnumSet.allOf(WebSocketMatchReplaceRulesTableModel.MatchType.class).forEach(form.getMatchTypeCombo()::addItem);
                EnumSet.allOf(WebSocketMatchReplaceRulesTableModel.Direction.class).forEach(form.getDirectionCombo()::addItem);

                // Assign values to form
                form.getMatchTypeCombo().setSelectedItem(matchType);
                form.getDirectionCombo().setSelectedItem(direction);
                form.getMatchText().setText(match);
                form.getReplaceText().setText(replace);

                // setup button handlers
                form.getCancelButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        popupDialog.dispose();
                    }
                });

                form.getOkButton().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Maybe validate these first?
                        matchReplaceRulesTableModel.setValueAt(form.getMatchTypeCombo().getSelectedItem(), selectedRowIndex, 1);
                        matchReplaceRulesTableModel.setValueAt(form.getDirectionCombo().getSelectedItem(), selectedRowIndex, 2);
                        matchReplaceRulesTableModel.setValueAt(form.getMatchText().getText(), selectedRowIndex, 3);
                        matchReplaceRulesTableModel.setValueAt(form.getReplaceText().getText(), selectedRowIndex, 4);
                        popupDialog.dispose();
                    }
                });

                popupDialog.setVisible(true);
            }
        });

        // Remove button for match replace
        settingsUI.getRemoveMatchReplaceButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = settingsUI.getMatchReplaceTable().getSelectedRows();

                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    matchReplaceRulesTableModel.removeRow(selectedRows[i]);
                }
                matchReplaceRulesTableModel.fireTableDataChanged();
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
        intruderItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                api.logging().logToOutput("its been clicked");
                WebSocketStreamTableModel tableModel = (WebSocketStreamTableModel) table.getModel();
                int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }

                intruderTab.addNewTab(tableModel.getStream(index).getRawMessage());

                // Go to WS intruder tab
                socketSleuthTabPanel.setSelectedIndex(1);
            }
        });

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

    private JTabbedPane constructBurpUi() {
        JTabbedPane tabs = new JTabbedPane();
        this.intruderTab = SocketSleuthTabbedPanel.create("Test tabs", WSIntruder.class, this.api);

        tabs.addTab("History", this.constructHistoryTab());
        tabs.addTab("WS Intruder", intruderTab);
        tabs.addTab("WS auto-repeater", this.constructRepeaterTab());
        tabs.addTab("Settings", this.constructSettingsTab());
        return tabs;
    }

    private Component constructRepeaterTab() {
        this.repeaterUI = new CustomTabbedPanel(this.tableModel);
        return this.repeaterUI;
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

        // Set upgrade request component
        UserInterface ui = this.api.userInterface();
        HttpRequestEditor upgradeRequestViewer = ui.createHttpRequestEditor(READ_ONLY);
        uiForm.getSocketConnectionSplit().setRightComponent(upgradeRequestViewer.uiComponent());

        // Super annoying - ui.createWebSocketMessageEditor exists but isn't implemented. Use raw editor for now
        //RawEditor messageViewer = ui.createRawEditor(READ_ONLY);
        WebSocketMessageEditor messageViewer = ui.createWebSocketMessageEditor(READ_ONLY);
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

                // Set upgrade request in right pane
                upgradeRequestViewer.setRequest(row.getUpgradeRequest());
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
