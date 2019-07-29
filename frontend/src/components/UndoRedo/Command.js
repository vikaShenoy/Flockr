/**
 * Class to handle the command pattern for UndoRedo functionality.
 * Allow users to add undo/redo actions which will be called by the UndoRedo component.
 */
class Command {
  constructor(undoAction, redoAction) {
    this.undoAction = undoAction;
    this.redoAction = redoAction; 
  }

  execute() {
    this.redoAction();
  }

  unexecute() {
    this.undoAction();
  }
}

export default Command;