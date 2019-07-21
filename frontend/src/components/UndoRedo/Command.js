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