import javax.swing.*;

public interface ContainerProvider {
    JPanel getContainer();
    default void handleData(Object data) {
        // Do nothing by default
    }
}
