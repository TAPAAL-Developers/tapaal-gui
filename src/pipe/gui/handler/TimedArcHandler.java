package pipe.gui.handler;

import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import dk.aau.cs.model.tapn.TimedOutputArc;
import pipe.gui.action.SplitArcAction;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.tapn.TimedInhibitorArcComponent;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

public class TimedArcHandler extends ArcHandler {

	public TimedArcHandler(Arc obj) {
		super(obj);
	}

	@Override
	public JPopupMenu getPopup(MouseEvent e) {
		int popupIndex = 0;
		JMenuItem menuItem;
		JPopupMenu popup = super.getPopup(e);

		menuItem = new JMenuItem("Properties");
		menuItem.addActionListener(e1 -> ((TimedOutputArcComponent) myObject).showTimeIntervalEditor());
		popup.insert(menuItem, popupIndex++);

		popup.insert(new JPopupMenu.Separator(), popupIndex);

		return popup;
	}
}
