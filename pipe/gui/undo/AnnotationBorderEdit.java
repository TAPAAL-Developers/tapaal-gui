/*
 * AnnotationBorderEdit.java
 */

package pipe.gui.undo;

import dk.aau.cs.gui.undo.Command;
import pipe.dataLayer.Note;

/**
 *
 * @author corveau
 */
public class AnnotationBorderEdit 
        extends Command {
   
   Note note;
   
   
   /** Creates a new instance of placeRateEdit */
   public AnnotationBorderEdit(Note _note) {
      note = _note;
   }
   
   
   /** */
   @Override
public void undo() {
      note.showBorder(!note.isShowingBorder());
   }

   
   /** */
   @Override
public void redo() {
      note.showBorder(!note.isShowingBorder());
   }
   
   
   @Override
public String toString(){
      return super.toString() + " " + note.getClass().getSimpleName();
   }
   
}
