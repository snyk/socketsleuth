package intruder.payloads.models;

import javax.swing.*;

public interface IPayloadModel {
    void addPayload(String payload);
    void removePayload(int index);
    void removeDuplicates();
    DefaultListModel<String> getListModel();
}
