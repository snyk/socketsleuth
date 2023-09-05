/*
 * © 2023 Snyk Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.WebSocketMessageEditor;
import socketsleuth.WebSocketInterceptionRulesTableModel;
import socketsleuth.utils.CommentManager;
import websocket.MessageProvider;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static burp.api.montoya.ui.editor.EditorOptions.READ_ONLY;

public class SocketSleuth implements BurpExtension {

    MontoyaApi api;
    JSONRPCResponseMonitor responseMonitor;
    WebSocketAutoRepeater webSocketAutoRepeater;
    JTabbedPane socketSleuthTabPanel;
    SleuthUI uiForm;
    CustomTabbedPanel repeaterUI;
    SettingsUI settingsUI;
    WebSocketConnectionTableModel tableModel;
    WebSocketInterceptionRulesTableModel interceptionRulesModel;
    WebSocketMatchReplaceRulesTableModel matchReplaceRulesTableModel;
    Map<Integer, WebSocketContainer> wsConnections;
    MessageProvider socketProvider;
    SocketSleuthTabbedPanel<WSIntruder> intruderTab;
    SocketSleuthTabbedPanel<AutoRepeaterTab> autoRepeaterTab;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        // Main table model for websocket connections
        this.tableModel = new WebSocketConnectionTableModel();
        this.interceptionRulesModel = new WebSocketInterceptionRulesTableModel();
        this.matchReplaceRulesTableModel = new WebSocketMatchReplaceRulesTableModel();
        this.responseMonitor = new JSONRPCResponseMonitor(api);
        this.wsConnections = new HashMap<>();
        this.socketProvider = new MessageProvider(api);
        this.webSocketAutoRepeater = new WebSocketAutoRepeater(api, this.wsConnections);

        api.extension().setName("SocketSleuth");
        this.socketSleuthTabPanel = constructBurpUi();

        api.userInterface().registerSuiteTab("SocketSleuth", this.socketSleuthTabPanel);

        // Create handler for new websocket connections
        // The table might not exist yet, check if there is bugs
        WebSocketCreationHandler webSocketCreationHandler = new WebSocketCreationHandler(api,
                this.tableModel,
                this.wsConnections,
                this.uiForm.getConnectionTable(),
                this.uiForm.getStreamTable(),
                this.interceptionRulesModel,
                this.matchReplaceRulesTableModel,
                this.responseMonitor,
                this.webSocketAutoRepeater,
                this.socketProvider
        );
        api.proxy().registerWebSocketCreationHandler(webSocketCreationHandler);
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
                JDialog popupDialog = new JDialog(api.userInterface().swingUtils().suiteFrame());
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
                        rulesTableModel.fireTableDataChanged();
                        popupDialog.dispose();
                    }
                });

                EnumSet.allOf(WebSocketInterceptionRulesTableModel.MatchType.class).forEach(form.getMatchTypeCombo()::addItem);
                EnumSet.allOf(WebSocketInterceptionRulesTableModel.Direction.class).forEach(form.getDirectionCombo()::addItem);

                popupDialog.setLocationRelativeTo(api.userInterface().swingUtils().suiteFrame());
                popupDialog.setVisible(true);
            }
        });

        // All "edit" button related stuff
        settingsUI.getEditInterceptButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRowIndex = settingsUI.getInterceptTable().getSelectedRow();

                WebSocketInterceptionRulesTableModel tableModel
                        = (WebSocketInterceptionRulesTableModel) settingsUI.getInterceptTable().getModel();

                // Get current values
                WebSocketInterceptionRulesTableModel.MatchType matchType
                        = (WebSocketInterceptionRulesTableModel.MatchType) tableModel.getValueAt(selectedRowIndex, 1);

                WebSocketInterceptionRulesTableModel.Direction direction
                        = (WebSocketInterceptionRulesTableModel.Direction) tableModel.getValueAt(selectedRowIndex, 2);

                String condition = (String) tableModel.getValueAt(selectedRowIndex, 3);

                // Prepare and show window
                InterceptEditWindow form = new InterceptEditWindow();
                JDialog popupDialog = new JDialog(api.userInterface().swingUtils().suiteFrame());
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

                popupDialog.setLocationRelativeTo(api.userInterface().swingUtils().suiteFrame());
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
                JDialog popupDialog = new JDialog(api.userInterface().swingUtils().suiteFrame());
                popupDialog.add(form.getContainer());
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

                popupDialog.setLocationRelativeTo(api.userInterface().swingUtils().suiteFrame());
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
                JDialog popupDialog = new JDialog(api.userInterface().swingUtils().suiteFrame());
                popupDialog.add(form.getContainer());
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

                popupDialog.setLocationRelativeTo(api.userInterface().swingUtils().suiteFrame());
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
        commentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }
                AbstractTableModel tm = (AbstractTableModel) table.getModel();
                CommentManager.addEditComment(api.userInterface().swingUtils().suiteFrame(),table, index, 5);
            }
        });

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

        popupMenu.add(commentItem);
        popupMenu.add(intruderItem);

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
        this.intruderTab = SocketSleuthTabbedPanel.create("WS Intruder", WSIntruder.class, this.api, this.tableModel, this.responseMonitor, this.socketProvider);
        this.autoRepeaterTab = SocketSleuthTabbedPanel.create("Repeater", AutoRepeaterTab.class, this.api, this.tableModel, this.webSocketAutoRepeater);

        tabs.addTab("History", this.constructHistoryTab());
        tabs.addTab("WS Intruder", this.intruderTab);
        tabs.addTab("WS auto-repeater", this.autoRepeaterTab);
        tabs.addTab("Settings", this.constructSettingsTab());
        return tabs;
    }

    private Component constructRepeaterTab() {
        this.repeaterUI = new CustomTabbedPanel(this.tableModel);
        return this.repeaterUI;
    }

    private Component constructHistoryTab() {
        // Create new instance of UI and start preparing
        uiForm = new SleuthUI();
        JTable table = uiForm.getConnectionTable();

        // Set upgrade request component
        UserInterface ui = this.api.userInterface();
        HttpRequestEditor upgradeRequestViewer = ui.createHttpRequestEditor(READ_ONLY);
        uiForm.getSocketConnectionSplit().setRightComponent(upgradeRequestViewer.uiComponent());

        WebSocketMessageEditor messageViewer = ui.createWebSocketMessageEditor(READ_ONLY);
        uiForm.setStreamVIewSplitPane(messageViewer.uiComponent());

        // Set a dummy model for WebSocket Message / steams with no data.
        this.uiForm.getStreamTable().setModel(new WebSocketStreamTableModel());

        // Set table model for WebSocket connections
        table.setModel(this.tableModel);
        this.bindConnectionTableContextMenu(table);

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

                int selectedViewIndex = table.getSelectedRow();
                int selectedRowIndex = table.convertRowIndexToView(selectedViewIndex);
                if (selectedRowIndex == -1) {
                    api.logging().raiseInfoEvent("selectedRowIndex is -1");
                    return;
                }

                // get data out of model
                int socketId = (int) table.getValueAt(selectedRowIndex, 0);

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
                int selectedViewIndex = messageTable.getSelectedRow();
                int selectedRowIndex = messageTable.convertRowIndexToView(selectedViewIndex);
                if (selectedRowIndex == -1) {
                    api.logging().raiseInfoEvent("selectedRowIndex is -1");
                    return;
                }


                int messageId = (int) messageTable.getValueAt(selectedRowIndex, 0);
                WebSocketStreamTableModel messageTableModel = (WebSocketStreamTableModel) messageTable.getModel();
                messageViewer.setContents(ByteArray.byteArray(messageTableModel.getStream(selectedRowIndex).getRawMessage()));
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

        return uiForm.getContainer();
    }

    private void bindConnectionTableContextMenu(JTable table) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem commentItem = new JMenuItem("Add comment");

        popupMenu.add(commentItem);

        commentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index == -1) {
                    return;
                }
                CommentManager.addEditComment(api.userInterface().swingUtils().suiteFrame(), table, index, 6);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.setRowSelectionInterval(row, row);
                    } else {
                        table.clearSelection();
                    }

                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}
