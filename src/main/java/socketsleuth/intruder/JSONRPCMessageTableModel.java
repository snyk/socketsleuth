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
package socketsleuth.intruder;

import burp.api.montoya.websocket.Direction;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class JSONRPCMessage {
    private int id;
    private String message;
    private Direction direction;
    private LocalDateTime time;

    public JSONRPCMessage(int id, String message, Direction direction, LocalDateTime time) {
        this.id = id;
        this.message = message;
        this.direction = direction;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getMessagePreview() {
        return message;
    }

    public Direction getDirection() {
        return direction;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public int getLength() {
        return message.length();
    }
}

public class JSONRPCMessageTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private List<JSONRPCMessage> streams = new ArrayList<>();
    private String[] columns = { "Message ID", "Message", "Direction", "Length", "Time" };
    private Class<?>[] columnTypes = { Integer.class, String.class, String.class, Integer.class, LocalDateTime.class };

    public void addMessage(String message, Direction direction) {
        streams.add(new JSONRPCMessage(
                streams.size(),
                message,
                direction,
                LocalDateTime.now()
        ));
        fireTableDataChanged();
    }

    public void removeMessage(int row) {
        streams.remove(row);
        fireTableDataChanged();
    }

    public JSONRPCMessage getMessage(int row) {
        return streams.get(row);
    }

    public List<JSONRPCMessage> getMessages() {
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
        JSONRPCMessage stream = streams.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return stream.getId();
            case 1:
                return stream.getMessage();
            case 2:
                return stream.getDirection();
            case 3:
                return stream.getLength();
            case 4:
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
