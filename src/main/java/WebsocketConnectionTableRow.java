import java.time.LocalDateTime;

public class WebsocketConnectionTableRow {

    private int socketId;
    private String url;
    private int listenerPort;
    private boolean tls;
    private boolean active;
    private String comment;

    private WebSocketStreamTableModel streamModel;

    public WebSocketStreamTableModel getStreamModel() {
        return streamModel;
    }

    public void setStreamModel(WebSocketStreamTableModel streamModel) {
        this.streamModel = streamModel;
    }

    public WebsocketConnectionTableRow(int socketId, String url, int listenerPort, boolean active, boolean tls, String comment) {
        this.socketId = socketId;
        this.url = url;
        this.listenerPort = listenerPort;
        this.tls = tls;
        this.active = active;
        this.comment = comment;
        this.streamModel = new WebSocketStreamTableModel();
        //this.streamModel.addStream(new WebSocketStream(socketId, "test", "fake", 4, LocalDateTime.now(), ""));
    }

    public int getSocketId() {
        return socketId;
    }

    public void setSocketId(int socketId) {
        this.socketId = socketId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String endpoint) {
        this.url = endpoint;
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }

    public boolean isTls() {
        return tls;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
