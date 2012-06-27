package dk.aau.cs.model.tapn.simulation;

import dk.aau.cs.model.tapn.NetworkMarking;

public abstract class TAPNNetworkTraceStep {
	private boolean loopStep = false;
	
	public void setLoopStep(){
		loopStep = true;
	}
	
	public boolean isLoopStep(){
		return loopStep;
	}
	
	public abstract NetworkMarking performStepFrom(NetworkMarking marking); // TODO: We should
															// introduce an
															// interface for
															// NetworkMarking,
															// that way this
															// trace stuff is
															// independent of a
															// specific model
}
