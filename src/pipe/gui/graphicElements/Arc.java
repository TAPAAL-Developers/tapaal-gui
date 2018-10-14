package pipe.gui.graphicElements;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.*;

import pipe.gui.CreateGui;
import pipe.gui.DrawingSurfaceImpl;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.handler.LabelHandler;
import pipe.gui.undo.AddArcPathPointEdit;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Weight;

/**
   Implementation of Element for drawing an arc
 */
public abstract class Arc extends PetriNetObject {

	private static final long serialVersionUID = 6527845538091358791L;

	protected NameLabel label;

	private static Point2D.Double point;

	/** References to the objects this arc connects */
	private PlaceTransitionObject source = null;
	private PlaceTransitionObject target = null;

	protected ArcPath myPath = new ArcPath(this);

	// Bounds of arc need to be grown in order to avoid clipping problems
	protected int zoomGrow = 10;

	/**
	 * Create Petri-Net Arc object
	 * 
	 */
	public Arc(double startPositionXInput, double startPositionYInput,
			double endPositionXInput, double endPositionYInput,
			PlaceTransitionObject sourceInput,
			PlaceTransitionObject targetInput, int weightInput, String idInput) {
		label = new NameLabel(zoom);
		myPath.addPoint((float) startPositionXInput,
				(float) startPositionYInput, ArcPathPoint.STRAIGHT);
		myPath.addPoint((float) endPositionXInput, (float) endPositionYInput,
				ArcPathPoint.STRAIGHT);
		myPath.createPath();
		updateBounds();
		id = idInput;
		setSource(sourceInput);
		setTarget(targetInput);

		//XXX see comment in function
		setLableHandler();
	}


	/**
	 * Create Petri-Net Arc object
	 */
	public Arc(PlaceTransitionObject newSource) {
		label = new NameLabel(zoom);
		source = newSource;
		myPath.addPoint();
		myPath.addPoint();
		myPath.createPath();

		//XXX see comment in function
		setLableHandler();
	}

	public Arc() {
		super();

		label = new NameLabel(zoom);
		//XXX see comment in function
		setLableHandler();
	}


	private void setLableHandler() {

		//XXX: kyrke 2018-09-06, this is bad as we leak "this", think its ok for now, as it alwas constructed when
		//XXX: handler is called. Make static constructor and add handler from there, to make it safe.
		LabelHandler labelHandler = new LabelHandler(this.getNameLabel(), this);

		getNameLabel().addMouseListener(labelHandler);
		getNameLabel().addMouseMotionListener(labelHandler);
		getNameLabel().addMouseWheelListener(labelHandler);

	}

	abstract public void setWeight(Weight weight);
	abstract public Weight getWeight();

	/**
	 * Set source
	 * 
	 * @param sourceInput
	 *            PlaceTransitionObject value for Arc source;
	 */
	public void setSource(PlaceTransitionObject sourceInput) {
		source = sourceInput;
	}

	/**
	 * Set target
	 * 
	 * @param targetInput
	 *            PlaceTransitionObject value for Arc target;
	 */
	public void setTarget(PlaceTransitionObject targetInput) {
		target = targetInput;
	}

	public void setLabelPosition() {
		label.setPosition(Grid.getModifiedX((double) (myPath.midPoint.x + Zoomer.getZoomedValue(nameOffsetX, zoom))), 
						  Grid.getModifiedY((double) (myPath.midPoint.y + Zoomer.getZoomedValue(nameOffsetY, zoom))));
	}
	@Override
	public void updateLabelLocation() {
		setLabelPosition();
	}

	/**
	 * Get id
	 * 
	 * @return String value for Arc id;
	 */
	@Override
	public String getId() {
		if (id != null) {
			return id;
		} else {
			if (source != null && target != null) {
				return source.getId() + " to " + target.getId();
			}
		}
		return "";
	}

	@Override
	public String getName() {
		return getId();
	}
	
	@Override 
	public NameLabel getNameLabel() {
		return label;
	}

	/**
	 * Get source returns null if value not yet entered
	 * 
	 * @return String value for Arc source;
	 */
	public PlaceTransitionObject getSource() {
		return source;
	}

	/**
	 * Get target returns null if value not yet entered
	 * 
	 * @return String value for Arc target;
	 */
	public PlaceTransitionObject getTarget() {
		return target;
	}

	/**
	 * Get X-axis value of start position
	 * 
	 * @return Double value for X-axis of start position
	 */
	public double getStartPositionX() {
		return myPath.getPoint(0).getX();
	}

	/**
	 * Get Y-axis value of start position
	 * 
	 * @return Double value for Y-axis of start position
	 */
	public double getStartPositionY() {
		return myPath.getPoint(0).getY();
	}
	/**
	 * Updates the start position of the arc, resets the arrowhead and updates
	 * the bounds
	 */
	public void updateArcPosition() {
		if (source != null) {
			source.updateEndPoint(this);
		}
		if (target != null) {
			target.updateEndPoint(this);
		}
		myPath.createPath();
	}

	public void setEndPoint(double x, double y, boolean type) {
		myPath.setPointLocation(myPath.getEndIndex(), x, y);
		myPath.setPointType(myPath.getEndIndex(), type);
		updateArcPosition();
	}

	public void setTargetLocation(double x, double y) {
		myPath.setPointLocation(myPath.getEndIndex(), x, y);
		myPath.createPath();
		updateBounds();
		repaint();
	}

	public void setSourceLocation(double x, double y) {
		myPath.setPointLocation(0, x, y);
		myPath.createPath();
		updateBounds();
		repaint();
	}

	public void updateLabel(boolean displayConstantNames) {
		//No label to update for this type
	}

	/** Updates the bounding box of the arc component based on the arcs bounds */
	public void updateBounds() {
		bounds = myPath.getBounds();
		bounds.grow(COMPONENT_DRAW_OFFSET + zoomGrow, COMPONENT_DRAW_OFFSET
				+ zoomGrow);
		setBounds(bounds);
	}

	public ArcPath getArcPath() {
		return myPath;
	}
	
	public void setArcPath(ArcPath newPath) {
		myPath = newPath;
	}

	@Override
	public boolean contains(int x, int y) {
		point = new Point2D.Double(x + myPath.getBounds().getX()
				- COMPONENT_DRAW_OFFSET - zoomGrow, y
				+ myPath.getBounds().getY() - COMPONENT_DRAW_OFFSET - zoomGrow);
		if (!CreateGui.getDrawingSurface().isInAnimationMode()) {
			if (myPath.proximityContains(point) || selected) {
				// show also if Arc itself selected
				myPath.showPoints();
			} else {
				myPath.hidePoints();
			}
		}
		return myPath.contains(point);
	}

	@Override
	public void addedToGui() {
		// called by GuiView / State viewer when adding component.
		deleted = false;

		myPath.addPointsToGui((DrawingSurfaceImpl) getParent());

		updateArcPosition();
		if (getParent() != null && label.getParent() == null) {
			getParent().add(label);
		}
	}

	@Override
	public void delete() {
		if (!deleted) {
			if (getParent() != null) {
				getParent().remove(label);
			}
			if(source != null) source.removeFromArc(this);
			if(target != null) target.removeToArc(this);
			myPath.forceHidePoints();
			super.delete();
			deleted = true;
		}
	}

	public void setPathToTransitionAngle(int angle) {
		myPath.setTransitionAngle(angle);
	}

	public Command split(Point2D.Float mouseposition) {
		ArcPathPoint newPoint = myPath.splitSegment(mouseposition);
		return new AddArcPathPointEdit(this, newPoint);
	}

	public Transition getTransition() {
		if (getTarget() instanceof Transition) {
			return (Transition) getTarget();
		} else {
			return (Transition) getSource();
		}
	}

	public void removeFromView() {
		if (getParent() != null) {
			getParent().remove(label);
		}
		myPath.forceHidePoints();
		removeFromContainer();
	}

	public boolean getsSelected(Rectangle selectionRectangle) {
		if (selectable) {
			ArcPath arcPath = getArcPath();
			if (arcPath.proximityIntersects(selectionRectangle)) {
				arcPath.showPoints();
			} else {
				arcPath.hidePoints();
			}
			if (arcPath.intersects(selectionRectangle)) {
				select();
				return true;
			}
		}
		return false;
	}
	
	public void selectPath(){
		if (selectable) {
			myPath.selectAllPoints();
		}
	}

	@Override
	public int getLayerOffset() {
		return Pipe.ARC_LAYER_OFFSET;
	}

	public void translate(int x, int y) {
		// We don't translate an arc, we translate each selected arc point
	}

	public void zoomUpdate(int percent) {
		zoom = percent;
		this.updateArcPosition();
		this.updateOnMoveOrZoom();
		label.zoomUpdate(percent);
		label.updateSize();
	}

	public void setZoom(int percent) {
		zoom = percent;
	}

	@Override
	public void undelete(DrawingSurfaceImpl view) {
		if (this.isDeleted()) {
			super.undelete(view);
			getSource().addConnectFrom(this);
			getTarget().addConnectTo(this);
			deleted = false;
		}
	}


	/**
	 * Handles keyboard input when drawing arcs in the GUI. Keys are bound to action names,
	 * and action names are mapped to action objects. The key bindings are disabled when the
	 * arc object is deleted, or the arc is connected to a place/transition.
	 */
	public void enableDrawingKeyBindings() {
		InputMap iMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap aMap = this.getActionMap();

		// Bind keyboard keys to action names.
		iMap.put(KeyStroke.getKeyStroke("ESCAPE"), "deleteArc");
		iMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteArc");

		// Associate action names with actions.
		aMap.put("deleteArc", new DeleteAction(this));
	}

	public void disableDrawingKeyBindings() {
		this.getInputMap().clear();
		this.getActionMap().clear();
	}

	private class DeleteAction extends AbstractAction {
		Arc arcBeingDraw;

		DeleteAction(Arc arc) {
			arcBeingDraw = arc;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			DrawingSurfaceImpl aView = CreateGui.getDrawingSurface();
			if (aView.createArc == arcBeingDraw) {
				aView.createArc = null;
				delete();

				if ((CreateGui.getApp().getMode() == Pipe.ElementType.FAST_PLACE)
						|| (CreateGui.getApp().getMode() == Pipe.ElementType.FAST_TRANSITION)) {
					CreateGui.getApp().endFastMode();
				}
				aView.repaint();
			}
		}
	}
}
