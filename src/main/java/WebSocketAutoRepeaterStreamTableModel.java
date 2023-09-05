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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WebSocketAutoRepeaterStreamTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private List<WebSocketStream> streams = new ArrayList<>();
    private String[] columns = { "Message ID", "Message", "Injected", "Direction", "Length", "Time" };
    private Class<?>[] columnTypes = { Integer.class, String.class, Boolean.class, String.class, Integer.class, LocalDateTime.class };

    public void addStream(WebSocketStream stream) {
        streams.add(stream);
        fireTableDataChanged();
    }

    public void removeStream(int row) {
        streams.remove(row);
        fireTableDataChanged();
    }

    public WebSocketStream getStream(int row) {
        return streams.get(row);
    }

    public List<WebSocketStream> getStreams() {
        return streams;
    }

    @Override
    public int getRowCount() {
        return streams.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        WebSocketStream stream = streams.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return stream.getMessageID();
            case 1:
                return stream.getMessage();
            case 2:
                return stream.isInjected();
            case 3:
                return stream.getDirection();
            case 4:
                return stream.getLength();
            case 5:
                return stream.getTime();
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
