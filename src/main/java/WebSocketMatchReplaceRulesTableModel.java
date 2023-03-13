import javax.swing.table.DefaultTableModel;

public class WebSocketMatchReplaceRulesTableModel extends DefaultTableModel {
    public WebSocketMatchReplaceRulesTableModel() {
        super(new Object[][]{}, new String[]{"Enabled", "Match type", "Direction", "Match", "Replace"});
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
            case 4:
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
        REPLACE("Replace"),
        DROP("Drop");

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
