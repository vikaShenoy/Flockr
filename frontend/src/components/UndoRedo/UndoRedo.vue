<template>
  <div>
    <v-icon @click="undo" class="action" color="secondary" :disabled="!undoStack.length">undo</v-icon>
    <v-icon @click="redo" class="action" color="secondary" :disabled="!redoStack.length">redo</v-icon>
  </div>
</template>

<script>
  const Z_KEY_CODE = 90;
  const Y_KEY_CODE = 89;

  export default {
    data() {
      return {
        undoStack: [],
        redoStack: [],
      }
    },
    mounted() {
      document.addEventListener("keydown", this.keyDown)
    },
    beforeDestroy() {
      document.removeEventListener("keydown", this.keyDown);
    },
    methods: {
      /**
       * Binds keyboard buttons to the undo and redo commands. Executes undo/redo depending on which keys
       * were pressed.
       * @param event the keyboard button click event.
       */
      keyDown(event) {
        const shouldUndo = event.keyCode === Z_KEY_CODE && event.ctrlKey && !event.shiftKey;
        const shouldRedo = (event.keyCode === Y_KEY_CODE && event.ctrlKey) ||
            (event.keyCode === Z_KEY_CODE && event.shiftKey && event.ctrlKey);
        if (shouldUndo) {
          if (this.undoStack.length) {
            this.undo();
          }
        } else if (shouldRedo) {
          if (this.redoStack.length) {
            this.redo();
          }
        }
      },
      /**
       * Undo's last action and adds to redo stack.
       */
      async undo() {
        const command = this.undoStack.pop();
        try {
          await command.unexecute();
          this.redoStack.push(command);
          this.showSuccessSnackbar("Successfully Un-did action");
        } catch (e) {
          // eslint-disable-next-line
          console.log(e);
          this.showErrorSnackbar("Could not undo action");
        }
      },
      /**
       * Redo's last action and adds to undo stack.
       */
      async redo() {
        const command = this.redoStack.pop();

        try {
          await command.execute();
          this.undoStack.push(command);
          console.log("I made it here");
          this.showSuccessSnackbar("Successfully Re-did action");
        } catch (e) {
          this.showErrorSnackbar("Could not undo action");
        }
      },
      /**
       * Adds a command to the undo stack.
       */
      addUndo(command) {
        this.redoStack = [];
        this.undoStack.push(command);
      },
      /**
       * Shows a success snackbar
       */
      showSuccessSnackbar(text) {
        this.$root.$emit("show-snackbar", {
          message: text,
          color: "success",
          timeout: 5000
        });
      },
      /**
       * Shows an error snackbar
       */
      showErrorSnackbar(text) {
        this.$root.$emit("show-snackbar", {
          message: text,
          color: "error",
          timeout: 5000
        });
      }
    }
  }
</script>


<style lang="scss" scoped>
  .action {
    cursor: pointer;
    font-size: 2rem;
    z-index: 5;
  }


</style>
