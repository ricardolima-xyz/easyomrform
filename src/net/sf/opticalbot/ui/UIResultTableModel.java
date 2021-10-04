package net.sf.opticalbot.ui;

import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class UIResultTableModel implements TableModel {

    private final List<String> headers;
    private final List<Map<String, String>> rows;

    public UIResultTableModel(List<String> headers, List<Map<String, String>> rows) {
        this.headers = headers;
        this.rows = rows;
    }
    
    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return headers.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return headers.get(columnIndex);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).get(headers.get(columnIndex));
    }

    @Override
    public boolean isCellEditable(int arg0, int arg1) {
        return false;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {  
    }
    
}
