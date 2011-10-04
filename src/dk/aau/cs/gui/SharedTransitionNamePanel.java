package dk.aau.cs.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import pipe.gui.undo.UndoManager;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel.SharedTransitionsListModel;
import dk.aau.cs.gui.undo.AddSharedTransitionCommand;
import dk.aau.cs.gui.undo.RenameSharedTransitionCommand;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.util.RequireException;

public class SharedTransitionNamePanel extends JPanel {
	private static final long serialVersionUID = -8099814326394422263L;

	private final JRootPane rootPane;
	private final SharedTransitionsListModel listModel;
	private JTextField nameField;
	private SharedTransition transitionToEdit;

	private final UndoManager undoManager;
	private final NameGenerator nameGenerator;

	public SharedTransitionNamePanel(JRootPane rootPane, SharedTransitionsListModel sharedTransitionsListModel, UndoManager undoManager, NameGenerator nameGenerator) {
		this(rootPane, sharedTransitionsListModel, undoManager, nameGenerator, null);
	}
	
	public SharedTransitionNamePanel(JRootPane rootPane, SharedTransitionsListModel sharedTransitionsListModel, UndoManager undoManager, NameGenerator nameGenerator, SharedTransition transitionToEdit) {
		this.rootPane = rootPane;
		this.listModel = sharedTransitionsListModel;
		this.undoManager = undoManager;
		this.nameGenerator = nameGenerator;
		this.transitionToEdit = transitionToEdit;
		initComponents();	
	}

	public void initComponents(){
		setLayout(new BorderLayout());
		
		JPanel namePanel = createNamePanel();
		JPanel buttonPanel = createButtonPanel();
		
		add(namePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.PAGE_END);
	}

	private JPanel createNamePanel() {
		JPanel namePanel = new JPanel(new GridBagLayout());
		
		JLabel label = new JLabel("Enter a shared transition name:");
		GridBagConstraints gbc = new GridBagConstraints();
		namePanel.add(label, gbc);
		
		String initialText = transitionToEdit == null ? "" : transitionToEdit.name();
		nameField = new JTextField(initialText);
		nameField.setMinimumSize(new Dimension(150,27));
		nameField.setPreferredSize(new Dimension(200, 27));
		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		namePanel.add(nameField, gbc);
		return namePanel;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		
		JButton okButton = new JButton("OK");
		okButton.setMaximumSize(new java.awt.Dimension(100, 25));
		okButton.setMinimumSize(new java.awt.Dimension(100, 25));
		okButton.setPreferredSize(new java.awt.Dimension(100, 25));

		rootPane.setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				String name = nameField.getText();
						
				if(name == null || name.isEmpty()){
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "You must specify a name.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}else{
					boolean success = true;
					if(transitionToEdit == null){
						success = addNewSharedTransition(name);
					}else if(!name.equals(transitionToEdit.name())){
						success = updateExistingTransition(name);
					}
					
					if(success){
						nameGenerator.updateIndicesForAllModels(name);
						exit();
					}
				}
			}

			private boolean updateExistingTransition(String name) {
				
				String oldName = transitionToEdit.name();
				
				if(transitionToEdit.network().isNameUsed(name) && !oldName.equalsIgnoreCase(name)) {
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "The specified name is already used by a place or transition in one of the components.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				
				try{
					transitionToEdit.setName(name);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "The specified name is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				listModel.updatedName();
				undoManager.addNewEdit(new RenameSharedTransitionCommand(transitionToEdit, oldName, name, listModel));
				return true;
			}
			private boolean addNewSharedTransition(String name) {
				SharedTransition transition = null;
				
				try{
					transition = new SharedTransition(name);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "The specified name is invalid.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				try{
					listModel.addElement(transition);
				}catch(RequireException e){
					JOptionPane.showMessageDialog(SharedTransitionNamePanel.this, "A transition or place with the specified name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				undoManager.addNewEdit(new AddSharedTransitionCommand(listModel, transition));
				return true;
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setMaximumSize(new java.awt.Dimension(100, 25));
		cancelButton.setMinimumSize(new java.awt.Dimension(100, 25));
		cancelButton.setPreferredSize(new java.awt.Dimension(100, 25));

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);
		
		return buttonPanel;
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

}
