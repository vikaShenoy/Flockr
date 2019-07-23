<template>
  <div>
    <v-icon @click="undo" class="action" color="secondary" :disabled="!undoStack.length">undo</v-icon>
    <v-icon @click="redo" class="action" color="secondary" :disabled="!redoStack.length">redo</v-icon>

    <Snackbar :snackbarModel="snackbarModel" v-on:dismissSnackbar="snackbarModel.show = false" />
 </div>
</template>

<script>
import Snackbar from "../Snackbars/Snackbar";

export default {
  components: {
    Snackbar
  },
  data() {
    return {
      undoStack: [],
      redoStack: [],
      snackbarModel: {
        text: "",
        color: "",
        show: false,
        timeout: 2000
      }
    }
  },
  methods: {
    /**
     * Undo's last action and adds to redo stack
     */
    async undo() {
      const command = this.undoStack.pop();
      try {
        await command.unexecute();
        this.addRedo(command);
        this.showSuccessSnackbar("Successfully Un-did action");
      } catch (e) {
        this.showErrorSnackbar("Could not undo action");
      }
    },
    /**
     * Redo's last action and adds to undo stack
     */
    async redo() {
      const command = this.redoStack.pop();
      
      try {
        await command.execute();
        this.addUndo(command);
        this.showSuccessSnackbar("Successfully Re-did action");
      } catch (e) {
        this.showErrorSnackbar("Could not undo action");
      }
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
    },
    /**
     * Shows a success snackbar
     */
    showSuccessSnackbar(text) {
      this.snackbarModel.text = text;
      this.snackbarModel.color = "success";
      this.snackbarModel.show = true;
    },
    /**
     * Shows an error snackbar
     */
    showErrorSnackbar(text) {
      this.snackbarModel.text = text;
      this.snackbarModel.color = "error";
      this.snackbarModel.show = true;
    }
  }
}
</script>


<style lang="scss" scoped>
.action {
  cursor: pointer;
  font-size: 2rem;
}


</style>
