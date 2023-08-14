package intruder.payloads.models;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class StringPayloadModel implements IPayloadModel {
    private final DefaultListModel<String> listModel;

    public StringPayloadModel() {
        listModel = new DefaultListModel<>();
    }

    public void addPayload(String payload) {
        listModel.addElement(payload);
    }

    public void removePayload(int index) {
        listModel.remove(index);
    }

    public void removeDuplicates() {
        Set<String> seen = new HashSet<>();
        for (int i = listModel.size() - 1; i >= 0; i--) {
            String payload = listModel.get(i);
            if (seen.contains(payload)) {
                listModel.remove(i);
            } else {
                seen.add(payload);
            }
        }
    }

    public DefaultListModel<String> getListModel() {
        return listModel;
    }
}
