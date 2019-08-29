<template>
  <div>
    <div id="contents">
      <Messages v-if="chatGroup.messages" :messages="chatGroup.messages" />

      <div v-else id="loading">
        <v-progress-circular
          indeterminate
          color="secondary">
        </v-progress-circular>
      </div>
    </div>

  <div id="new-message">
    <div id="new-message-input">
      <v-text-field
        v-model="message"
        label="Type a message"
        single-line
        outline
        color="secondary"
        maxlength="100"
        @keyup.enter="sendMessage()"
      ></v-text-field>
    </div>

    <v-icon id="send-btn" color="secondary" @click="sendMessage()">send</v-icon>
  </div>

  </div>
</template>

<script>
import { sendMessage, getChatMessages } from "./ChatGroupService";
import Messages from "./Messages/Messages";

export default {
  components: {
    Messages
  },
  props: {
    chatGroup: Object
  },
  data() {
    return {
      message: ""
    };
  },
  mounted() {
    if (!this.chatGroup.messages) {
      this.getChatMessages();
    }
  },
  methods: {
    async sendMessage() {
      console.log(this.chatGroup);
      try {
        const message = await sendMessage(this.chatGroup.chatGroupId, this.message);
        this.$emit("newMessage", message);
        this.message = "";
      } catch (e) {
        this.$root.$emit("show-error-snackbar", "Error sending message", 2000);
      }
    },
    /**
     * Gets chat messages and emits them back to parent
     */
    async getChatMessages() {
      try {
        const messages = await getChatMessages(this.chatGroup.chatGroupId);
        this.$emit("messagesRetrieved", messages);
      } catch (e) {
        console.log(e);
        this.$root.$emit("show-snackbar", {
          message: "Could not get chat messages",
          color: "error",
          timeout: 2000
        });
      }
    }
  }  
}
</script>



<style lang="scss">
// Override vue text field so height is same as parent
.v-input__slot {
  height: 57px;
  margin-bottom: 0px;
}

.v-input__control {
  height: 57px;
}
</style>

<style lang="scss" scoped>
#new-message {
  width: 100%;
  display: flex;
  position: absolute;
  bottom: 0;
  justify-content: center;
  align-items: center;
  height: 57px;
}

#new-message-input {
  width: 90%;
  height: 57px;
}

#send-btn {
  width: 10%;
}

#contents {
  height: 340px;
  overflow: auto;
}


#loading {
  justify-content: center;
  align-items: center;
  display: flex;
  height: 100%;
}
</style>