import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;

public class CustomTabbedPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private TableModel tableModel;

    public CustomTabbedPanel(WebSocketConnectionTableModel tableModel) {
        this.tableModel = tableModel;

        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Add initial tab
        addNewTab();

        // Create the "new tab" button
        JButton newTabButton = new JButton("+ New Repeater");
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

    //TODO: remove this class. Its not used anymore (I think)
    private void addNewTab() {
        AutoRepeaterTab newTabContent = new AutoRepeaterTab(tabbedPane.getTabCount(), null, this.tableModel, null);
        int newIndex = tabbedPane.getTabCount();
        String title = "Tab " + (newIndex + 1);
        tabbedPane.addTab(title, newTabContent.getContainer());
        tabbedPane.setTabComponentAt(newIndex, createTabComponent(title));
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
