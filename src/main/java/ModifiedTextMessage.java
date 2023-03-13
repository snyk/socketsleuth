import burp.api.montoya.core.Annotations;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.websocket.Direction;

public class ModifiedTextMessage implements InterceptedTextMessage {
    String payload;
    Direction direction;
    Annotations annotations;

    public ModifiedTextMessage() {}

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setAnnotations(Annotations annotations) {
        this.annotations = annotations;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String payload() {
        return this.payload;
    }

    @Override
    public Direction direction() {
        return this.direction;
    }

    @Override
    public Annotations annotations() {
        return this.annotations;
    }
}
