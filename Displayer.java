import java.awt.GridLayout;

import javax.swing.*;

/**
 * This class acts as the JFrame that will display the results of user queries
 * @author Alex Perinetti
 *
 */
public class Displayer extends JFrame{
    JPanel panel;
    
    /**
     * Initializes the JFrame and the JPanel
     * @param query The user inputted query that will display in the header of the window
     */
    public Displayer (String query) {
        super("Search Results for: " + query);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new JPanel(new GridLayout(0,1));
    }
    
    /**
     * Displays the list of items for queries that do not have stemming
     * @param out The list of results to be displayed
     */
    public void displayNonStem(Object[] out) {
        JList<Object> list = new JList<>(out);
        list.setVisibleRowCount(10);
        list.setPreferredSize(null);
        JScrollPane scroll = new JScrollPane(list);
        panel.add(scroll);
        add(panel);
        pack();
        setSize(700, 500);
        setVisible(true);
    }
    
    /**
     * Displays the list of items, first for the original query then for the stemmed query
     * @param stemQuery The stem of the original query
     * @param out The list of results from the original query
     * @param stemOut The list of results from the stemmed query
     */
    public void displayStem(String stemQuery, Object[] out, Object[] stemOut) {
        JList<Object> list1 = new JList<>(out);
        list1.setVisibleRowCount(10);
        list1.setPreferredSize(null);
        JScrollPane scroll1 = new JScrollPane(list1);
        
        JLabel label = new JLabel("Results for similar query " + stemQuery);
        
        JList<Object> list2 = new JList<>(stemOut);
        list2.setVisibleRowCount(10);
        list2.setPreferredSize(null);
        JScrollPane scroll2 = new JScrollPane(list2);
        
        panel.add(scroll1);
        panel.add(label);
        panel.add(scroll2);
        
        add(panel);
        pack();
        setSize(700, 500);
        setVisible(true);
    }
}
