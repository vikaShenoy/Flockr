<script>


export default {
  data() {
    return {
      undoStack: [],
      redoStack: []  
    };
  },
  methods: {
    addUndo(command) {
      this.undoStack.push(command);
    },
    addRedo(command) {
      this.redoStack.push(command);
    },
    undoAction() {
      console.log("Undo called");
      const command = this.undoStack.pop();
      this.addRedo(command);
    },
    redoAction() {
      console.log("Redo called");
      const command = this.redoStack.pop();
      this.addUndo(command);
    }
  }
}
</script>
