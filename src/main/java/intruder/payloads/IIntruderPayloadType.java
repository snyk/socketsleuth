package intruder.payloads;

import intruder.payloads.models.IPayloadModel;
import javax.swing.*;

public interface IIntruderPayloadType {
    IPayloadModel getPayloadModel();
    JPanel getContainer();
}
