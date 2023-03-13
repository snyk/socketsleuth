import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class WebSocketConnectionTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private List<WebsocketConnectionTableRow> connections = new ArrayList<>();
    private String[] columns = { "Socket ID", "URL", "Port", "Active", "TLS", "Comment" };
    private Class<?>[] columnTypes = { Integer.class, String.class, Integer.class, Boolean.class, Boolean.class, String.class };

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
                return connection.getComment();
            default:
                return null;
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

