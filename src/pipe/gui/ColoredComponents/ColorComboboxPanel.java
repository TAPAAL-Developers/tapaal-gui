package pipe.gui.ColoredComponents;

import dk.aau.cs.model.CPN.*;
import dk.aau.cs.model.CPN.Color;
import net.tapaal.swinghelpers.GridBagHelper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

public abstract class ColorComboboxPanel extends JPanel {

    private ColorType colorType;
    private String panelName;

    private JPanel colorcomboBoxPanel;
    private JComboBox[] colorTypeComboBoxesArray;
    JScrollPane colorTypesScrollPane;
    JPanel comboBoxPanel;

    public ColorComboboxPanel(ColorType colorType, String panelName) {
        this.colorType = colorType;
        this.panelName = panelName;
        this.setLayout(new GridBagLayout());
        initPanel();
    }

    public ColorType getColorType() {
        return colorType;
    }

    public JComboBox[] getColorTypeComboBoxesArray() {
        return colorTypeComboBoxesArray;
    }

    private void initPanel() {
        colorcomboBoxPanel = new JPanel();
        colorcomboBoxPanel.setLayout(new GridBagLayout());


        //This panel contains all comboboxes, there can be more than one with ProductTypes
        comboBoxPanel = new JPanel(new GridBagLayout());
        //In case it is a really large product type we have a scrollPane
        colorTypesScrollPane = new JScrollPane(comboBoxPanel);
        colorTypesScrollPane.setViewportView(comboBoxPanel);
        colorTypesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        colorTypesScrollPane.setBorder(BorderFactory.createTitledBorder("Color Type elements"));
        updateColorType(colorType);

        Dimension scrollPaneSize = new Dimension(260, 150);
        colorTypesScrollPane.setMaximumSize(scrollPaneSize);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.SOUTH;
        colorcomboBoxPanel.add(colorTypesScrollPane, gbc);
        add(colorcomboBoxPanel, gbc);
    }

    public void removeScrollPaneBorder(){
        colorTypesScrollPane.setBorder(null);
    }

    public abstract void changedColor(JComboBox[] comboBoxes);
    public void updateSelection(Color color){
        if (colorType instanceof ProductType) {
            for (int i = 0; i < colorTypeComboBoxesArray.length; i++) {
                for (Color element : ((ProductType) colorType).getColorTypes().get(i)) {
                    colorTypeComboBoxesArray[i].setSelectedItem(element);
                }
            }
        }
        else if (colorType != null){
            colorTypeComboBoxesArray[0].setSelectedItem(color);
        }
    }

    public void updateColorType(ColorType ct){
        removeOldComboBoxes();
        colorType = ct;
        Dimension comboSize = new Dimension(230, 30);
        if (colorType instanceof ProductType) {
            colorTypeComboBoxesArray = new JComboBox[((ProductType) colorType).getColorTypes().size()];

            for (int i = 0; i < colorTypeComboBoxesArray.length; i++) {
                colorTypeComboBoxesArray[i] = new JComboBox();
                for (Color element : ((ProductType) colorType).getColorTypes().get(i)) {
                    colorTypeComboBoxesArray[i].addItem(element);
                }

                colorTypeComboBoxesArray[i].setPreferredSize(comboSize);
                colorTypeComboBoxesArray[i].setMinimumSize(comboSize);
                colorTypeComboBoxesArray[i].setMaximumSize(comboSize);
                colorTypeComboBoxesArray[i].addActionListener(actionEvent -> changedColor(colorTypeComboBoxesArray));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = i;
                gbc.anchor = GridBagConstraints.SOUTH;
                gbc.insets = new Insets(5 , 0,0,0);
                comboBoxPanel.add(colorTypeComboBoxesArray[i], gbc);
            }
        }
        else if (colorType != null){
            colorTypeComboBoxesArray = new JComboBox[1];
            colorTypeComboBoxesArray[0] = new JComboBox();
            for (Iterator<Color> iter = colorType.iterator(); iter.hasNext();) {
                Color element = iter.next();
                colorTypeComboBoxesArray[0].addItem(element);
            }
            colorTypeComboBoxesArray[0].setPreferredSize(comboSize);
            colorTypeComboBoxesArray[0].setMinimumSize(comboSize);
            colorTypeComboBoxesArray[0].setMaximumSize(comboSize);
            colorTypeComboBoxesArray[0].addActionListener(actionEvent -> changedColor(colorTypeComboBoxesArray));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;

            gbc.anchor = GridBagConstraints.SOUTH;
            gbc.insets = new Insets(5 , 0,0,0);
            comboBoxPanel.add(colorTypeComboBoxesArray[0], gbc);
        }
        colorTypesScrollPane.setPreferredSize(new Dimension(260,colorTypeComboBoxesArray.length*40));
        colorTypesScrollPane.setMinimumSize(new Dimension(260, 60));
        revalidate();
    }
    private void removeOldComboBoxes(){
        comboBoxPanel.removeAll();
    }
}
