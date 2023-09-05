package socketsleuth.utils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

public final class CommentManager {

    public static void addEditComment(Frame parentFrame, JTable table, int row, int commentColumn) {
        AbstractTableModel tableModel = (AbstractTableModel) table.getModel();
        String existingComment = (String) tableModel.getValueAt(row, commentColumn);
        String newComment = JOptionPane.showInputDialog(
                parentFrame,
                "Enter comment:",
                existingComment
        );

        if (newComment != null) {
            tableModel.setValueAt(newComment, row, commentColumn);
        }
    }

    private CommentManager() {}
}
