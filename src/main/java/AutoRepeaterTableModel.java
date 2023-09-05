/*
 * © 2023 Snyk Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class AutoRepeaterTableModel extends AbstractTableModel {
    private final String[] columns = {"Socket ID", "URL", "Port", "Active", "TLS"};
    private final Class<?>[] columnTypes = {Integer.class, String.class, Integer.class, Boolean.class, Boolean.class};

    private AbstractTableModel originalTableModel;
    private List<Integer> filteredRowIndexes;

    public AutoRepeaterTableModel(AbstractTableModel originalTableModel) {
        this.originalTableModel = originalTableModel;
        filterData();
    }

    private void filterData() {
        filteredRowIndexes = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < originalTableModel.getRowCount(); rowIndex++) {
            if ((Boolean) originalTableModel.getValueAt(rowIndex, 3)) { // 3 is the index of the "Active" column
                filteredRowIndexes.add(rowIndex);
            }
        }
    }

    @Override
    public int getRowCount() {
        return filteredRowIndexes.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int originalRowIndex = filteredRowIndexes.get(rowIndex);
        if (columnIndex >= 3) { // Shift the column index by 1 for the removed "Comment" column
            columnIndex += 1;
        }
        return originalTableModel.getValueAt(originalRowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }
}
