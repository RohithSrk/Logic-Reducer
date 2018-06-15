package LogicReducer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;


public class GUI extends JFrame implements ActionListener {

    private JSplitPane subSplitPane;
    private JSplitPane mainSplitPane;
    private JPanel bottomPannel;
    private JPanel leftPannel;
    private JPanel rightPannel;
    private JPanel pane5;
    private JTable table1;
    private JLabel rightLabel1;
    private JButton solveButton;
    private JComboBox inputTypeComboBox1;
    private JLabel inputLabel;
    private JLabel solutionLabel;
    private JLabel ePrimeImplicantsLabel;
    private JLabel primeImplicantsLabel;
    private JScrollPane tableScroll;
    private JMenuBar mb;

    int nMinTerms = 0, nDontCares = 0, nMaxTerms = 0;
    int minTerms[], dontCares[];
    int nVar;
    JTextArea minTextArea, dcTextArea;
    JLabel minL, dcL;

    GUI(){
        super("Logic Reducer");
        setSize(800,500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMenu();
        setContentPane(pane5);

        minTextArea = new JTextArea(4,50);
        dcTextArea = new JTextArea(4,50);
        minTextArea.setVisible(false);
        minL = new JLabel("Minterms: ");
        dcL = new JLabel("Don't Cares: ");
        minL.setPreferredSize(new Dimension(9000, 30));
        dcL.setPreferredSize(new Dimension(9000, 30));

        for(int i = 1; i <= 26; i++) {
            inputTypeComboBox1.addItem(new String(i + ""));
        }

        inputTypeComboBox1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == 1){

                    int nVar = Integer.parseInt((String) e.getItem());

                    if(nVar > 5){
                        tableScroll.setVisible(false);
                        leftPannel.setLayout(new FlowLayout(FlowLayout.LEFT));
                        leftPannel.add(minL);
                        leftPannel.add(minTextArea);
                        leftPannel.add(dcL);
                        leftPannel.add(dcTextArea);
                        minTextArea.setVisible(true);
                    } else {
                        tableScroll.setVisible(true);
                        minTextArea.setVisible(false);
                        leftPannel.setLayout(new BorderLayout());
                        updateTable(nVar);
                    }

                    validate();
                    repaint();
                }

            }
        });

        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                calcTerms();

                rightLabel1.setText("<html>"+ QuineMcCluskey.getQuestion(nVar,minTerms, dontCares ) +"</html>");

                QuineMcCluskey qm = new QuineMcCluskey(nVar, minTerms, dontCares);

                primeImplicantsLabel.setText("Prime Implicants: " + qm.getPrimeImplicants());
                ePrimeImplicantsLabel.setText("EssentialPrime Implicants: " + qm.getEPrimeImplicants());

                solutionLabel.setText("Solution in SOP: " + qm.getReducedSOP());
            }
        });
    }

    public void setMenu()
    {
        mb = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menu.add(new JMenuItem("Exit"));
        mb.add(menu);
        setJMenuBar(mb);
    }

    public void showGUI()
    {
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    public void displayTable()
    {

        table1 = new JTable(new CustomTableModel(QuineMcCluskey.getColumnHeaders(1),
                QuineMcCluskey.getKmap(1)));

        for (int i = 0; i < table1.getRowCount(); i++)
            for (int j = 0; j < table1.getColumnCount(); j++)
                if(j != 0) table1.setValueAt("0", i, j);

        table1.getTableHeader().setReorderingAllowed(false);
        table1.getColumnModel().getColumn(0).setCellRenderer(new RowHeaderRenderer());

        table1.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                table1 = (JTable)e.getSource();
                int row = table1.getSelectedRow();
                int column = table1.getSelectedColumn();

                if(column == 0) return;

                String v = String.valueOf( table1.getValueAt( row, column ) );

                if(v.equals(""))
                    table1.setValueAt("0", row, column );
                else if(v.equals("0"))
                    table1.setValueAt("1", row, column );
                else if(v.equals("1"))
                    table1.setValueAt("X", row, column );
                else if(v.equals("X"))
                    table1.setValueAt("0", row, column );

                calcTerms();
                rightLabel1.setText("<html>"+ QuineMcCluskey.getQuestion(nVar,minTerms, dontCares ) +"</html>");
            }
        });

        for (int i = 0; i < table1.getColumnCount(); i++)
            table1.getColumnModel().getColumn(i).setMaxWidth(46);


        table1.setRowHeight(44);
        table1.getTableHeader().setPreferredSize(new Dimension(46,46));


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table1.setDefaultRenderer(String.class, centerRenderer);

    }

    public void updateTable(int nVar)
    {
        table1.setModel(new CustomTableModel(QuineMcCluskey.getColumnHeaders(nVar),
                QuineMcCluskey.getKmap(nVar)));

        for (int i = 0; i < table1.getColumnCount(); i++)
            table1.getColumnModel().getColumn(i).setMaxWidth(46);

        table1.setRowHeight(44);
        table1.getTableHeader().setPreferredSize(new Dimension(46,46));

        for (int i = 0; i < table1.getRowCount(); i++)
            for (int j = 0; j < table1.getColumnCount(); j++)
                if(j != 0) table1.setValueAt("0", i, j);

        table1.getColumnModel().getColumn(0).setCellRenderer(new RowHeaderRenderer());
    }

    public void calcTerms(){
        nMaxTerms = 0;
        nDontCares = 0;
        nMinTerms = 0;
        nVar = Integer.parseInt( inputTypeComboBox1.getSelectedItem().toString() );

        if(nVar <= 5)
        {
            for (int i = 0; i < table1.getRowCount(); i++) {
                for (int j = 0; j < table1.getColumnCount(); j++) {

                    if(j > 0){
                        String v = String.valueOf( table1.getValueAt( i, j ) );

                        if(v.equals("0"))
                            nMaxTerms++;
                        else if(v.equals("1"))
                            nMinTerms++;
                        else if(v.equals("X"))
                            nDontCares++;
                    }

                }
            }

            minTerms = new int[nMinTerms];
            dontCares = new int[nDontCares];
            int mC = 0, dC = 0, t = 0;
            Object kmap[][] = QuineMcCluskey.getKmap(nVar);

            for (int i = 0; i < table1.getRowCount(); i++) {
                for (int j = 0; j < table1.getColumnCount(); j++) {

                    if(j > 0){
                        String v = String.valueOf( table1.getValueAt( i, j ) );

                        if(v.equals("0"))
                            t++;
                        else if(v.equals("1"))
                            minTerms[mC++] = Integer.parseInt((String) kmap[i][j]);
                        else if(v.equals("X"))
                            dontCares[dC++] = Integer.parseInt((String) kmap[i][j]);
                    }

                }
            }
        } else {
            if(!minTextArea.getText().isEmpty()){
                String min[] = minTextArea.getText().split(",");
                minTerms = new int[min.length];
                for (int i = 0; i < min.length; i++)
                    minTerms[i] = Integer.parseInt(min[i]);
            } else {
                minTerms = new int[0];
            }

            if(!dcTextArea.getText().isEmpty()){
                String dc[] = dcTextArea.getText().split(",");
                dontCares = new int[dc.length];
                for (int i = 0; i < dc.length; i++)
                    dontCares[i] = Integer.parseInt(dc[i]);
            } else {
                dontCares = new int[0];
            }

        }

    }

    static class RowHeaderRenderer extends DefaultTableCellRenderer {
        public RowHeaderRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();

                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (isSelected) {
                setFont(getFont().deriveFont(Font.BOLD));
            }

            setValue(value);
            return this;
        }
    }

    private void createUIComponents() {
        displayTable();
    }

}

class CustomTableModel extends AbstractTableModel {

    private String[] columnNames;
    private Object[][] data;

    CustomTableModel(String[] columnNames, Object[][] data){
        this.columnNames = columnNames;
        this.data = data;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column
     * would contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
        return  false;
    }

    /*
     * Don't need to implement this method unless your table's data can
     * change.
     */
    public void setValueAt(Object value, int row, int col) {
//        if (DEBUG) {
//            System.out.println("Setting value at " + row + "," + col
//                    + " to " + value + " (an instance of "
//                    + value.getClass() + ")");
//        }

        data[row][col] = value;
        fireTableCellUpdated(row, col);

//        if (DEBUG) {
//            System.out.println("New value of data:");
//            printDebugData();
//        }
    }
}
