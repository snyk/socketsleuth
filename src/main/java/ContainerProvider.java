import javax.swing.*;

public interface ContainerProvider {
    JPanel getContainer();
    void setTabId(int tabId);
    default void handleData(Object data) {
        // Do nothing by default
    }
}
