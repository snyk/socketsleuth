import burp.api.montoya.websocket.Direction;

public class AutoRepeaterConfig {
    private int sourceSocketId;
    private int targetSocketId;
    private Direction direction;
    private int tabId;
    private boolean isActive;

    private WebSocketAutoRepeaterStreamTableModel streamTableModel;

    public AutoRepeaterConfig(int sourceSocketId, int targetSocketId, Direction direction, int tabId, WebSocketAutoRepeaterStreamTableModel streamTableModel) {
        this.sourceSocketId = sourceSocketId;
        this.targetSocketId = targetSocketId;
        this.direction = direction;
        this.tabId = tabId;
        this.isActive = false;
        this.streamTableModel = streamTableModel;
    }

    public int getSourceSocketId() {
        return sourceSocketId;
    }

    public void setSourceSocketId(int sourceSocketId) {
        this.sourceSocketId = sourceSocketId;
    }

    public int getTargetSocketId() {
        return targetSocketId;
    }

    public void setTargetSocketId(int targetSocketId) {
        this.targetSocketId = targetSocketId;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getTabId() {
        return tabId;
    }

    public void setTabId(int tabId) {
        this.tabId = tabId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public WebSocketAutoRepeaterStreamTableModel getStreamTableModel() {
        return streamTableModel;
    }
}
