import burp.api.montoya.proxy.websocket.InterceptedBinaryMessage;
import burp.api.montoya.proxy.websocket.InterceptedTextMessage;
import burp.api.montoya.websocket.Direction;

import java.time.LocalDateTime;

public class WebSocketStream {
    private int messageID;
    private InterceptedMessageFacade interceptedMessage;
    private LocalDateTime time;
    private String comment;

    public WebSocketStream(int messageID, InterceptedTextMessage interceptedTextMessage, LocalDateTime time, String comment) {
        this.messageID = messageID;
        this.interceptedMessage = new InterceptedMessageFacade(interceptedTextMessage);
        this.time = time;
        this.comment = comment;
    }

    public WebSocketStream(int messageID, InterceptedBinaryMessage interceptedBinaryMessage, LocalDateTime time, String comment) {
        this.messageID = messageID;
        this.interceptedMessage = new InterceptedMessageFacade(interceptedBinaryMessage);
        this.time = time;
        this.comment = comment;
    }

    public int getMessageID() {
        return messageID;
    }

    /*public void setMessageID(int messageID) {
        this.messageID = messageID;
    }*/

    public String getMessage() {
        return this.interceptedMessage.stringPreviewPayload();
    }

    public byte[] getRawMessage() {
        return  this.interceptedMessage.binaryPayload();
    }

    /*public void setMessage(String message) {
        this.message = message;
    }*/

    public Direction getDirection() {
        return this.interceptedMessage.direction();
    }

    /*public void setDirection(String direction) {
        this.direction = direction;
    }*/

    public int getLength() {
        return this.interceptedMessage.binaryPayload().length;
    }

    /*public void setLength(int length) {
        this.length = length;
    }*/

    public LocalDateTime getTime() {
        return time;
    }

    /*public void setTime(LocalDateTime time) {
        this.time = time;
    }*/

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
