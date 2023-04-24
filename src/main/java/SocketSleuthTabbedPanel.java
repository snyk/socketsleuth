import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SocketSleuthTabbedPanel<T extends ContainerProvider> extends JPanel {

    private JTabbedPane tabbedPane;
    private int createdTabs;
    private String panelName;
    private Supplier<T> componentSupplier;

    private SocketSleuthTabbedPanel(String name, Supplier<T> componentSupplier) {
        this.panelName = name;
        this.componentSupplier = componentSupplier;
        this.createdTabs = 0;

        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Add initial tab
        //addNewTab();

        // Create the "new tab" button
        JButton newTabButton = new JButton("+ New " + this.panelName);
        newTabButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewTab();
            }
        });

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(newTabButton, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.NORTH);
    }

    public int getCreatedTabs() {
        return createdTabs;
    }

    public static <T extends ContainerProvider> SocketSleuthTabbedPanel<T> create(String name, Class<T> componentClass, Object... constructorArgs) {
        try {
            Constructor<?>[] constructors = componentClass.getConstructors();
            Constructor<T> constructorToUse = null;

            outerLoop:
            for (Constructor<?> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();

                if (parameterTypes.length != constructorArgs.length + 1) { // Add 1 to account for the additional int argument
                    continue;
                }

                // Check if the first parameter is an int
                if (!parameterTypes[0].isAssignableFrom(Integer.TYPE)) {
                    continue;
                }

                for (int i = 1; i < parameterTypes.length; i++) { // Start from index 1, as index 0 is the additional int argument
                    if (!parameterTypes[i].isAssignableFrom(constructorArgs[i - 1].getClass())) {
                        continue outerLoop;
                    }
                }

                //noinspection unchecked
                constructorToUse = (Constructor<T>) constructor;
                break;
            }

            if (constructorToUse == null) {
                throw new RuntimeException("No suitable constructor found for " + componentClass.getName());
            }

            Constructor<T> finalConstructorToUse = constructorToUse;
            AtomicReference<SocketSleuthTabbedPanel<T>> tabbedPanelRef = new AtomicReference<>();
            SocketSleuthTabbedPanel<T> tabbedPanel = new SocketSleuthTabbedPanel<>(name, () -> {
                try {
                    Object[] combinedArgs = new Object[constructorArgs.length + 1];
                    combinedArgs[0] = tabbedPanelRef.get().getCreatedTabs() + 1;
                    System.arraycopy(constructorArgs, 0, combinedArgs, 1, constructorArgs.length);

                    return finalConstructorToUse.newInstance(combinedArgs);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Cannot create an instance of " + componentClass.getName(), e);
                }
            });

            tabbedPanelRef.set(tabbedPanel);
            return tabbedPanel;
        } catch (SecurityException e) {
            throw new RuntimeException("Cannot create an instance of " + componentClass.getName(), e);
        }
    }


    public void addNewTab(Object data) {
        T newTabContent = componentSupplier.get();
        newTabContent.handleData(data);
        int newIndex = tabbedPane.getTabCount();
        String title = panelName + " " + (newIndex + 1);
        tabbedPane.addTab(title, newTabContent.getContainer());
        tabbedPane.setTabComponentAt(newIndex, createTabComponent(title));
        tabbedPane.setSelectedIndex(newIndex);
        tabbedPane.revalidate();
        tabbedPane.repaint();
        createdTabs++;
    }

    private void addNewTab() {
        T newTabContent = componentSupplier.get();
        int newIndex = tabbedPane.getTabCount();
        String title = panelName + " " + (newIndex + 1);
        tabbedPane.addTab(title, newTabContent.getContainer());
        tabbedPane.setTabComponentAt(newIndex, createTabComponent(title));
        tabbedPane.setSelectedIndex(newIndex);
        tabbedPane.revalidate();
        tabbedPane.repaint();
        createdTabs++;
    }


    private JPanel createTabComponent(String title) {
        JPanel tabComponent = new JPanel(new BorderLayout());
        tabComponent.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        tabComponent.add(titleLabel, BorderLayout.CENTER);

        JTextField titleEditor = new JTextField(title);
        titleEditor.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        titleEditor.setVisible(false);
        titleEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                titleEditor.setVisible(false);
                titleLabel.setVisible(true);
                int tabIndex = tabbedPane.indexOfTabComponent(tabComponent);
                String newTitle = titleEditor.getText();
                if (!newTitle.isEmpty()) {
                    titleLabel.setText(newTitle);
                    tabbedPane.setTitleAt(tabIndex, newTitle);
                }
            }
        });
        titleEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                titleEditor.setVisible(false);
                titleLabel.setVisible(true);
                int tabIndex = tabbedPane.indexOfTabComponent(tabComponent);
                String newTitle = titleEditor.getText();
                if (!newTitle.isEmpty()) {
                    titleLabel.setText(newTitle);
                    tabbedPane.setTitleAt(tabIndex, newTitle);
                }
            }
        });

        tabComponent.add(titleEditor, BorderLayout.NORTH);

        tabComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tabIndex = tabbedPane.indexOfTabComponent(tabComponent);

                if (e.getClickCount() == 1) {
                    tabbedPane.setSelectedIndex(tabIndex);
                } else if (e.getClickCount() == 2) {
                    titleLabel.setVisible(false);
                    titleEditor.setVisible(true);
                    titleEditor.requestFocus();
                    titleEditor.selectAll();
                }
            }
        });

        JButton closeButton = new JButton("x");
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int tabIndex = tabbedPane.indexOfTabComponent(tabComponent);
                if (tabIndex != -1) {
                    tabbedPane.removeTabAt(tabIndex);
                }
            }
        });

        tabComponent.add(closeButton, BorderLayout.EAST);
        return tabComponent;
    }



}
