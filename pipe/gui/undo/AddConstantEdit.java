package pipe.gui.undo;

import pipe.dataLayer.Constant;
import pipe.dataLayer.ConstantStore;
import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;

public class AddConstantEdit extends Command {
	private Constant constant;
	private ConstantStore store;
	
	public AddConstantEdit(Constant constant, ConstantStore store){
		this.constant = constant;
		this.store = store;
	}
		
	@Override
	public void redo() {
		store.add(constant);
		CreateGui.updateConstantsList();
	}

	@Override
	public void undo() {
		store.remove(constant);
		CreateGui.updateConstantsList();
	}

}
