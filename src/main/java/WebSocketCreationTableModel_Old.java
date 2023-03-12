import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class WebSocketCreationTableModel_Old extends AbstractTableModel {

    private final String[] columnNames = {"socket ID", "endpoint", "listener port", "TLS", "Comment"};
    private final List<WebsocketConnectionTableRow> data;

    public WebSocketCreationTableModel_Old() {
        data = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        WebsocketConnectionTableRow rowData = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rowData.getSocketId();
            //case 1:
              //  return rowData.getEndpoint();
            case 2:
                return rowData.getListenerPort();
            case 3:
                return rowData.isTls();
            case 4:
                return rowData.getComment();
            default:
                throw new IndexOutOfBoundsException("Invalid column index");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 2:
                return Integer.class;
            case 1:
            case 4:
                return String.class;
            case 3:
                return Boolean.class;
            default:
                throw new IndexOutOfBoundsException("Invalid column index");
        }
    }

    // Add a new row to the table
    public void addRow(WebsocketConnectionTableRow rowData) {
        data.add(rowData);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    // Remove a row from the table
    public void removeRow(int rowIndex) {
        data.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
}
