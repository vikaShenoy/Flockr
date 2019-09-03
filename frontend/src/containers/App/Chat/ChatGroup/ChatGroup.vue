<template>
  <div>
    <div id="contents" ref="contents" v-on:scroll="handleScroll()">
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
      message: "",
        shouldSend: false
    };
  },
  mounted() {
    if (!this.chatGroup.messages) {
      this.getChatMessages();
    } else {
      contents.scrollTop = contents.scrollHeight;
    }
  },
  methods: {
    async sendMessage() {
      try {
          if (!this.message.length) {
              return;
          }
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
        const messages = await getChatMessages(this.chatGroup.chatGroupId, null, null);
        this.$emit("messagesRetrieved", messages);
      } catch (e) {
        console.log(e);
        this.$root.$emit("show-snackbar", {
          message: "Could not get chat messages",
          color: "error",
          timeout: 2000
        });
      }
    },
      async handleScroll() {
        const contents = this.$refs.contents;
        const nearTop = contents.scrollTop <= 50;
        if (nearTop && !this.shouldSend) {
            this.shouldSend = true;
            console.log("Passing an offset of length: " + this.chatGroup.messages.length)
            const messages = await getChatMessages(this.chatGroup.chatGroupId, this.chatGroup.messages.length, 20);
            console.log(messages);
            this.$emit("newMessages", messages);
            this.shouldSend = false;
        }
        //console.log("scrolling");
        //console.log(nearTop);

      }
  },
  watch: {
    chatGroup: {
      handler(newValue) {
        const contents = this.$refs.contents;
        const isAtBottom = contents.scrollTop + contents.clientHeight > contents.scrollHeight - 50;

        if (isAtBottom) {
          console.log("is it true: " + isAtBottom);
          setTimeout(() => {
            contents.scrollTop = contents.scrollHeight;
          }, 0);
        }
      },
      deep: true
    }
  }
}
</script>

<style lang="scss">

    #new-message .v-text-field__details {
        display: none;
    }

    #new-message .v-input__slot {
        margin-bottom: 0px;
    }

</style>

<style lang="scss" scoped>
#new-message {
  width: 100%;
  display: flex;
  position: absolute;
  bottom: 5px;
  justify-content: center;
  align-items: center;
  height: 57px;
}

#new-message-input {
  width: 90%;
  height: 57px;
    padding-left: 7px;
    padding-bottom: 0px;
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