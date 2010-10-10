package dk.aau.cs.translations;

import java.util.List;

public interface TranslationNamingScheme {
	TransitionTranslation[] interpretTransitionSequence(List<String> firingSequence);
	String tokenClockName();
	boolean isIgnoredPlace(String location);	
	boolean isIgnoredAutomata(String automata);
	
	public class TransitionTranslation {
		private int startsAt;
		private String originalTransitionName;
		
		public TransitionTranslation(int startsAt, String originalTransitionName){
			this.startsAt = startsAt;
			this.originalTransitionName = originalTransitionName;
		}
		
		public int startsAt(){
			return startsAt;
		}
		
		public String originalTransitionName(){
			return originalTransitionName;
		}
		
		@Override
		public String toString() {
			return originalTransitionName;
		}
	}
}
