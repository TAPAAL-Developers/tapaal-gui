package dk.aau.cs.gui;

import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.Variable;
import dk.aau.cs.model.tapn.*;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;
import pipe.gui.graphicElements.*;
import pipe.gui.graphicElements.tapn.TimedInputArcComponent;
import pipe.gui.graphicElements.tapn.TimedOutputArcComponent;
import pipe.gui.graphicElements.tapn.TimedTransportArcComponent;

import java.util.ArrayList;
import java.util.Vector;

public class TabTransformer {
    static public void removeTimingInformation(TabContent tab){
        for(Template template : tab.allTemplates()){
            ArrayList<TimedTransportArcComponent> transportArcComponents = new ArrayList<TimedTransportArcComponent>();
            // Make place token age invariant infinite
            for(TimedPlace place : template.model().places()){
                place.setInvariant(TimeInvariant.LESS_THAN_INFINITY);
            }
            // Make transitions non-urgent
            for(TimedTransition transition : template.model().transitions()){
                transition.setUrgent(false);
            }

            for(Arc arc : template.guiModel().getArcs()){
                // Make output arc guards infinite
                if(arc instanceof TimedOutputArcComponent) {
                    TimedOutputArcComponent arcComp = (TimedOutputArcComponent) arc;
                    arcComp.setGuardAndWeight(TimeInterval.ZERO_INF, arcComp.getWeight());
                }

                // Add and process transport arcs in separate list to avoid delete errors
                if(arc instanceof TimedTransportArcComponent){
                    TimedTransportArcComponent arcComp = (TimedTransportArcComponent) arc;
                    transportArcComponents.add(arcComp);
                }
            }

            // Replace transport arcs with regular arcs
            for(TimedTransportArcComponent arc : transportArcComponents){
                // Input arc
                if(arc.getSource() instanceof Place) {
                    TimedPlace source = template.model().getPlaceByName(arc.getSource().getName());
                    TimedTransition destination = template.model().getTransitionByName(arc.getTarget().getName());

                    TimedInputArc addedArc = new TimedInputArc(source, destination, TimeInterval.ZERO_INF, arc.getWeight(), arc.underlyingArc().getExpression());


                    // GUI
                    DataLayer guiModel = template.guiModel();
                    Place guiSource = guiModel.getPlaceByName(arc.getSource().getName());
                    Transition guiTarget = guiModel.getTransitionByName(arc.getTarget().getName());
                    TimedInputArcComponent newArc = new TimedInputArcComponent(
                        new TimedOutputArcComponent(
                            guiSource,
                            guiTarget,
                            arc.getWeight().value(),
                            arc.getSource().getName() + "_to_" + arc.getTarget().getName()
                        ),
                        tab.getLens()
                    );

                    // Build ArcPath
                    Place oldGuiSource = guiModel.getPlaceByName(arc.getSource().getName());
                    Transition oldGuiTarget = guiModel.getTransitionByName(arc.getTarget().getName());
                    ArcPath newArcPath = createArcPath(guiModel, oldGuiSource, oldGuiTarget, newArc);

                    // Set arcPath, guiModel and connectors
                    newArc.setUnderlyingArc(addedArc);
                    newArc.setArcPath(newArcPath);
                    newArc.updateArcPosition();
                    guiModel.addPetriNetObject(newArc);

                    //Change the partner

                    TimedOutputArcComponent arc2 = convertPartner(arc.getConnectedTo(), template, guiModel);
                    removeTransportArc(arc, guiModel);

                    //Add arc to model and GUI
                    template.model().add(addedArc);
                    template.model().add(arc2.underlyingArc());

                }

            }
        }
    }
    static void removeTransportArc(TimedTransportArcComponent arc, DataLayer guiModel){
        // Delete the transport arc
        arc.underlyingTransportArc().delete();
        TimedTransportArcComponent partner = arc.getConnectedTo();

        guiModel.removePetriNetObject(arc);
        guiModel.removePetriNetObject(partner);
    }
    static TimedOutputArcComponent convertPartner(TimedTransportArcComponent arc, Template template, DataLayer guiModel) {
        //Add new arc

        TimedPlace destination = template.model().getPlaceByName(arc.getTarget().getName());
        TimedTransition source = template.model().getTransitionByName(arc.getSource().getName());

        TimedOutputArc addedArc = new TimedOutputArc(source, destination, arc.getWeight(), arc.underlyingArc().getExpression());
        //template.model().add(addedArc);

        // GUI

        Place guiTarget = guiModel.getPlaceByName(arc.getTarget().getName());
        Transition guiSource = guiModel.getTransitionByName(arc.getSource().getName());
        TimedOutputArcComponent newArc = new TimedOutputArcComponent(
            guiSource,
                guiTarget,
                arc.getWeight().value(),
                arc.getSource().getName() + "_to_" + arc.getTarget().getName()
        );

        // Build ArcPath
        Place oldGuiTarget = guiModel.getPlaceByName(arc.getTarget().getName());
        Transition oldGuiSource = guiModel.getTransitionByName(arc.getSource().getName());
        ArcPath newArcPath = createArcPath(guiModel, oldGuiSource, oldGuiTarget, newArc);

        // Set arcPath, guiModel and connectors
        newArc.setUnderlyingArc(addedArc);
        newArc.setArcPath(newArcPath);
        newArc.updateArcPosition();
        guiModel.addPetriNetObject(newArc);

        return newArc;

    }

    private static ArcPath createArcPath(DataLayer currentGuiModel, PlaceTransitionObject source, PlaceTransitionObject target, Arc arc) {
        Arc guiArc = currentGuiModel.getArcByEndpoints(source, target);
        ArcPath arcPath = guiArc.getArcPath();
        int arcPathPointsNum = arcPath.getNumPoints();

        // Build ArcPath
        ArcPath newArcPath = new ArcPath(arc);
        newArcPath.purgePathPoints();
        for(int k = 0; k < arcPathPointsNum; k++) {
            ArcPathPoint point = arcPath.getArcPathPoint(k);
            newArcPath.addPoint(
                    point.getPoint().x,
                    point.getPoint().y,
                    point.getPointType()
            );
        }

        return newArcPath;
    }

    static public void removeGameInformation(TabContent tab) {
        for (Template template : tab.allTemplates()) {
            for (TimedTransition transition : template.model().transitions()) {
                if (transition.isUncontrollable()) {
                    transition.setUncontrollable(false);
                }
            }
        }
    }
    static public void removeColorInformation(TabContent tab) {
        tab.network().setColorTypes(new ArrayList<ColorType>());
        tab.network().setVariables(new ArrayList<Variable>());
        for (Template template : tab.allTemplates()) {
            for(TimedPlace place : template.model().places()){
                place.setCtiList(new ArrayList<>());
                place.setColorType(ColorType.COLORTYPE_DOT);
                int numberOfTokens = place.tokens().size();

                //kind of hack to convert from coloredTokens to uncolored
                for(TimedToken token : place.tokens()){
                    token.setColor(place.getColorType().getFirstColor());
                }
            }

            for (TimedTransition transition : template.model().transitions()) {
                //TODO: what is the default guard
                transition.setGuard(null);
            }

            for(TimedInputArc arc : template.model().inputArcs()){
                arc.setColorTimeIntervals(new ArrayList<>());
                int expressionWeight = arc.getArcExpression().weight();
                ColorType ct = arc.source().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(expressionWeight));
            }

            for(TimedOutputArc arc : template.model().outputArcs()){
                ColorType ct = arc.destination().getColorType();
                int expressionWeight = arc.getExpression().weight();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(expressionWeight));
            }

            for(TransportArc arc : template.model().transportArcs()){
                ColorType ct = arc.source().getColorType();
                ArcExpression oldInputExpr = arc.getInputExpression();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(1, vecColorExpr);
                arc.setInputExpression(numbExpr);
                arc.setOutputExpression(numbExpr);
                arc.setWeight(new IntWeight(oldInputExpr.weight()));
            }
        }
    }

    static public void addColorInformation(TabContent tab){
        for (Template template : tab.allTemplates()) {
            for(TimedInputArc arc : template.model().inputArcs()){
                arc.setColorTimeIntervals(new ArrayList<>());
                ColorType ct = arc.source().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(arc.getWeight().value(), vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(0));
            }

            for(TimedOutputArc arc : template.model().outputArcs()){
                ColorType ct = arc.destination().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(arc.getWeight().value(), vecColorExpr);
                arc.setExpression(numbExpr);
                arc.setWeight(new IntWeight(0));
            }

            for(TransportArc arc : template.model().transportArcs()){
                ColorType ct = arc.source().getColorType();
                UserOperatorExpression userOperatorExpression = new UserOperatorExpression(ct.getFirstColor());
                Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
                vecColorExpr.add(userOperatorExpression);
                NumberOfExpression numbExpr = new NumberOfExpression(arc.getWeight().value(), vecColorExpr);
                arc.setInputExpression(numbExpr);
                arc.setOutputExpression(numbExpr);
                arc.setWeight(new IntWeight(0));

            }
        }
    }
}
