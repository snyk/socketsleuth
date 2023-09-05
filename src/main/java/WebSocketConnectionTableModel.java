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

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class WebSocketConnectionTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private List<WebsocketConnectionTableRow> connections = new ArrayList<>();
    private String[] columns = { "Socket ID", "URL", "Port", "Active", "TLS", "Messages", "Comment" };
    private Class<?>[] columnTypes = { Integer.class, String.class, Integer.class, Boolean.class, Boolean.class, Integer.class, String.class };

    public void addConnection(WebsocketConnectionTableRow connection) {
        connections.add(connection);
        fireTableDataChanged();
    }

    public void removeConnection(int row) {
        connections.remove(row);
        fireTableDataChanged();
    }

    public WebsocketConnectionTableRow getConnection(int row) {
        return connections.get(row);
    }

    public List<WebsocketConnectionTableRow> getConnections() {
        return connections;
    }

    @Override
    public int getRowCount() {
        return connections.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        WebsocketConnectionTableRow connection = connections.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return connection.getSocketId();
            case 1:
                return connection.getUrl();
            case 2:
                return connection.getListenerPort();
            case 3:
                return connection.isActive();
            case 4:
                return connection.isTls();
            case 5:
                return connection.getStreamModel().getRowCount();
            case 6:
                return connection.getComment();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // For now only comment is editable - if this changes use a switch like getValueAt
        if (col == 6 && value instanceof String) {
            WebsocketConnectionTableRow connection = connections.get(row);
            connection.setComment((String) value);
            fireTableCellUpdated(row, col);
        }
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }
}

