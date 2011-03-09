package pipe.dataLayer;

import java.awt.Container;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.undo.ArcTimeIntervalEdit;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.GuardDialogue;

public class TimedArc extends NormalArc{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8263782840119274756L;
	protected String timeInterval; 
	
	public TimedArc(PlaceTransitionObject source){
		super(source);
		init();
	}

	private void init() {
		timeInterval="[0,inf)";
		
		updateWeightLabel();
	}
	
	public TimedArc(NormalArc arc){
		super(arc);
		init();
	}

	public TimedArc(NormalArc arc, String guard) {
		super(arc);
		timeInterval = guard;
		updateWeightLabel();
	}

	public static boolean validateTimeInterval(String timeInterval) {
		if (Pattern.matches("((\\(\\d+)|(\\[\\d+)),((inf\\))|((\\d+\\))|(\\d+\\])))",timeInterval)){
			String[] range = timeInterval.split(",");
			String firstNumber = "";
			String secondNumber = "";
			for (int i=1; i<range[0].length(); i++){
				firstNumber = "" + firstNumber + range[0].charAt(i);
			}
			for (int i=0; i<range[1].length()-1; i++){
				secondNumber = "" + secondNumber + range[1].charAt(i);
			}
			if (secondNumber.equals("inf")) secondNumber = ""+Integer.MAX_VALUE; 
			return Integer.parseInt(firstNumber)<=Integer.parseInt(secondNumber);	
		}
		return false;
	}
	public String getGuard() {
		
		return timeInterval;
	}
	public UndoableEdit setGuard(String timeInteval) {
	
		String oldTimeInterval = this.timeInterval;
		this.timeInterval = timeInteval;

		//hacks - I use the weight to display the TimeInterval
		updateWeightLabel();
		repaint();

		return new ArcTimeIntervalEdit(this, oldTimeInterval, this.timeInterval);
	}
	//hacks - I use the weight to display the TimeInterval
	@Override
	public void updateWeightLabel(){   
		if(!CreateGui.getModel().netType().equals(NetType.UNTIMED)){
			weightLabel.setText(timeInterval);

			this.setWeightLabelPosition();	
		}
	}
	
	
	@Override
	public TimedArc copy(){
		NormalArc copy = new NormalArc(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedArc timedCopy = new TimedArc(copy.copy(), this.timeInterval);
		return timedCopy;
	}
	
	@Override
	public TimedArc paste(double despX, double despY, boolean toAnotherView){
		NormalArc copy = new NormalArc(this);
		copy.setSource(this.getSource());
		copy.setTarget(this.getTarget());
		TimedArc timedCopy = new TimedArc(copy.paste(despX, despY, toAnotherView), this.timeInterval);
		return timedCopy;
	}

	public void showTimeIntervalEditor() {
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.TOOL + " " + Pipe.VERSION, true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add Place editor
		contentPane.add( new GuardDialogue(guiDialog.getRootPane(), this) );

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

	}

	public boolean satisfiesGuard(BigDecimal token) {
		boolean satisfies = true;
		String[] partedTimeInteval = timeInterval.split(",");
		if ((""+partedTimeInteval[0].charAt(0)).contains("[") ){
			if (token.compareTo(BigDecimal.valueOf(Long.parseLong( partedTimeInteval[0].substring(1) ))) < 0){
				return false;
			}
		}else {
			if ( token.compareTo(BigDecimal.valueOf(Long.parseLong( partedTimeInteval[0].substring(1) )))  <= 0){
				return false;
			}
		}
		int guardMaxValue = 0;
		
		int lastIndexOfNumber = partedTimeInteval[1].length()-1;
		if ( partedTimeInteval[1].substring(0, lastIndexOfNumber).contains("inf") ){
			guardMaxValue = Integer.MAX_VALUE;
		} else {
			guardMaxValue = Integer.parseInt( partedTimeInteval[1].substring(0, lastIndexOfNumber) );
		}
		
		if ((""+partedTimeInteval[1].charAt(lastIndexOfNumber)).contains("]") ){
			if ( token.compareTo(BigDecimal.valueOf((Long.parseLong(""+guardMaxValue)))) > 0 ){
				return false;
			}
		}else {
			if ( token.compareTo(BigDecimal.valueOf((Long.parseLong(""+guardMaxValue)))) >= 0){
				return false;
			}
		}
		
		return satisfies;
	}
	
	@Override
	public void setWeightLabelPosition() {
		weightLabel.setPosition(
				(int)(myPath.midPoint.x) + weightLabel.getWidth()/2 - 4, 
				(int)(myPath.midPoint.y) - ((zoom/55)*(zoom/55)) );
	}

	public static boolean validateTimeInterval(String leftDelim,
			String leftInterval, String rightInterval, String rightDelim) {
		boolean isFirstNumber = true;
		boolean isSecondNumber = true;
		int firstValue = 0;
		int secondValue = 0;
	
		try{
			firstValue = Integer.parseInt(leftInterval);
		}catch(NumberFormatException e){
			isFirstNumber = false;
		}
		try{
			secondValue = Integer.parseInt(rightInterval);
		}catch(NumberFormatException e){
			isSecondNumber = false;
		}
		
		if(!isFirstNumber){
			firstValue = CreateGui.getModel().getConstantValue(leftInterval);
		}
		
		if(!isSecondNumber){
			if(rightInterval.equals("inf"))
				secondValue = Integer.MAX_VALUE;
			else
				secondValue = CreateGui.getModel().getConstantValue(rightInterval);
		}
		
		return firstValue <= secondValue;
	}
}