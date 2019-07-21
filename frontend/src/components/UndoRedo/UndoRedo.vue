<template>
  <div>
    <v-icon @click="undo" class="action" color="secondary" :disabled="!undoStack.length">undo</v-icon>
    <v-icon @click="redo" class="action" color="secondary" :disabled="!redoStack.length">redo</v-icon>
 </div>
</template>

<script>
import UndoRedoStack from "./UndoRedoStack";

export default {
  data() {
    return {
      undoStack: [],
      redoStack: []
    }
  },
  mixins: [
    UndoRedoStack
  ],
  methods: {
    /**
     * Undo's last action and adds to redo stack
     */
    undo() {
      const command = this.undoStack.pop();
      command.unexecute();
      this.addRedo(command);
    },
    /**
     * Redo's last action and adds to undo stack
     */
    redo() {
      const command = this.redoStack.pop();
      command.execute();
      this.addUndo(command);
    },
    /**
     * Adds a command to the undo stack
     */
    addUndo(command) {
      this.undoStack.push(command);
    },
    /**
     * Adds a command to the redo stack
     */
    addRedo(command) {
      this.redoStack.push(command);
    }
  }
}
</script>


<style lang="scss" scoped>
.action {
  cursor: pointer;
}


</style>
