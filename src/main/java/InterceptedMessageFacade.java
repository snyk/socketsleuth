import burp.api.montoya.core.Annotations;
import burp.api.montoya.proxy.websocket.InterceptedBinaryMessage;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.websocket.Direction;

public class InterceptedMessageFacade {
    private final boolean isText;
    private InterceptedTextMessage textMessage;
    private InterceptedBinaryMessage binaryMessage;

    public InterceptedMessageFacade(InterceptedTextMessage textMessage) {
        this.isText = true;
        this.textMessage = textMessage;
        this.binaryMessage = null;
    }

    public Object getInterceptedMessage() {
        return isText ? textMessage : binaryMessage;
    }

    public InterceptedMessageFacade(InterceptedBinaryMessage binaryMessage) {
        this.isText = false;
        this.textMessage = null;
        this.binaryMessage = binaryMessage;
    }

    public void setStringPayload(String payload) {
        if (isText) {
            modifyPayload(textMessage, payload);
        } else {
            modifyPayload(binaryMessage, payload);
        }
    }

    public void setBytesPayload(byte[] payload) {
        if (isText) {
            modifyPayload(textMessage, new String(payload));
        } else {
            modifyPayload(binaryMessage, new String(payload));
        }
    }

    private void modifyPayload(InterceptedTextMessage message, String payload) {
        ModifiedTextMessage newMessage = new ModifiedTextMessage();
        newMessage.setDirection(message.direction());
        newMessage.setAnnotations(message.annotations());
        newMessage.setPayload(payload);

        this.textMessage = newMessage;
    }

    private void modifyPayload(InterceptedBinaryMessage message, String payload) {

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

    public boolean isText() {
        return isText;
    }
}
