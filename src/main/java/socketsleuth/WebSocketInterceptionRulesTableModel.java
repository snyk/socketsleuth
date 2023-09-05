package socketsleuth;/*
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

import javax.swing.table.DefaultTableModel;

public class WebSocketInterceptionRulesTableModel extends DefaultTableModel {
    public WebSocketInterceptionRulesTableModel() {
        super(new Object[][]{}, new String[]{"Enabled", "Match type", "Direction", "Condition (regex)"});
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return MatchType.class;
            case 2:
                return Direction.class;
            case 3:
                return String.class;
            default:
                return super.getColumnClass(columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    public enum MatchType {
        CONTAINS("Contains"),
        DOES_NOT_CONTAIN("Does not contain"),
        EXACT_MATCH("Exact match");

        private final String text;

        MatchType(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum Direction {
        BIDIRECTIONAL("Bidirectional"),
        CLIENT_TO_SERVER("Client to server"),
        SERVER_TO_CLIENT("Server to client");

        private final String text;

        Direction(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
