import burp.api.montoya.core.Annotations;
import burp.api.montoya.proxy.websocket.InterceptedBinaryMessage;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.websocket.Direction;

public class InterceptedMessageFacade {
    private final boolean isText;
    private final InterceptedTextMessage textMessage;
    private final InterceptedBinaryMessage binaryMessage;

    public InterceptedMessageFacade(InterceptedTextMessage textMessage) {
        this.isText = true;
        this.textMessage = textMessage;
        this.binaryMessage = null;
    }

    public InterceptedMessageFacade(InterceptedBinaryMessage binaryMessage) {
        this.isText = false;
        this.textMessage = null;
        this.binaryMessage = binaryMessage;
    }

    public String stringPayload() {
        return isText ? textMessage.payload() : new String(binaryMessage.payload().getBytes());
    }

    public String stringPreviewPayload() {
        return isText ? textMessage.payload() : new String("binary");
    }

    public byte[] binaryPayload() {
        return isText ? textMessage.payload().getBytes() : binaryMessage.payload().getBytes();
    }

    public Direction direction() {
        return isText ? textMessage.direction() : binaryMessage.direction();
    }

    public Annotations annotations() {
        return isText ? textMessage.annotations() : binaryMessage.annotations();
    }
}
