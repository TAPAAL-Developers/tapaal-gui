package dk.aau.cs.gui.undo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.undo.UndoManager;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.Template;
import pipe.gui.CreateGui;
import pipe.gui.GuiFrame;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.TimedPlaceComponent;
import dk.aau.cs.TCTL.visitors.BooleanResult;
import dk.aau.cs.TCTL.visitors.MakePlaceSharedVisitor;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.NameGenerator;
import dk.aau.cs.gui.SharedPlacesAndTransitionsPanel;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.model.tapn.LocalTimedPlace;
import dk.aau.cs.model.tapn.SharedPlace;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedToken;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.Require;

public class MakePlaceNewSharedMultiCommand extends Command {

		private final String newSharedName;
		private Context context;
		private Place place;
		private Command command;
		private List<Command> commands = new ArrayList<Command>();


		
		public MakePlaceNewSharedMultiCommand(dk.aau.cs.gui.Context context, String newSharedName, TimedPlaceComponent place){
			Require.that(context.network().allTemplates() != null, "tapns cannot be null");
			Require.that(newSharedName != null, "newSharedName cannot be null");
			Require.that(context.tabContent() != null, "currentTab cannot be null");
			
			this.place = place;
			this.context = context;
			this.newSharedName = newSharedName;
		}
		
		@Override
		public void redo() {
			SharedPlace sharedPlace = null;
			int i = 0;
			for(Template template : context.tabContent().allTemplates()) {
				TimedPlaceComponent component = (TimedPlaceComponent)template.guiModel().getPlaceByName(place.getName());
				//We make a new shared place with the first place
				if(component != null && i < 1) {
					command = new MakePlaceNewSharedCommand(template.model(), newSharedName, component.underlyingPlace(), component, context.tabContent(), true);
					command.redo();
					sharedPlace = (SharedPlace)component.underlyingPlace();
					commands.add(command);
					i++;
				//For the rest we make them shared with the recently made place
				} else if (component != null && i >= 1){
					command = new MakePlaceSharedCommand(context.activeModel(), sharedPlace, component.underlyingPlace(), component, context.tabContent(), true);
					command.redo();
					commands.add(command);
				}
			}
		}

		@Override
		public void undo() {	
			for(Command command : commands)
				command.undo();
		}
	}

