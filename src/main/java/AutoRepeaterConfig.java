import burp.api.montoya.websocket.Direction;

public class AutoRepeaterConfig {
    private int sourceSocketId;
    private int targetSocketId;
    private Direction direction;
    private int tabId;
    private boolean isActive;

    public AutoRepeaterConfig(int sourceSocketId, int targetSocketId, Direction direction, int tabId) {
        this.sourceSocketId = sourceSocketId;
        this.targetSocketId = targetSocketId;
        this.direction = direction;
        this.tabId = tabId;
        this.isActive = false;
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
}
