<template>
  <div>
    <v-icon class="hover-white" @click="toggleVoiceChat" >
      {{ isInChat ? "call_end" : "speaker_phone" }}
    </v-icon>

      <audio ref="roomAudio" autoplay></audio>

  </div>
</template>

<script>
import { VoiceChat } from "./voice";
import Janus from "./janus";

export default {
  props: {
    chatGroup: Object
  },
  data() {
    return {
      isInChat: false,
      voiceChat: null 
    };
  },
  mounted() {
    this.voiceChat = new VoiceChat();
    
    // Event gets emitted when a new user connects
    this.voiceChat.on("remoteUserConnected", stream => {
        Janus.attachMediaStream(this.$refs.roomAudio , stream)
    });

    this.voiceChat.on("error", error => {
        console.log(error);
    });
  },
  methods: {
    /**
     * Handles the user joining and leaving the chat
     */
    toggleVoiceChat() {
      if (!this.isInChat) {
        this.voiceChat.joinRoom(this.chatGroup.chatGroupId);
      } else {
        this.voiceChat.leaveRoom();
      }
      this.isInChat = !this.isInChat;
    }
  },
  /**
   * Make sure that if you leave the chat group page, that the user leaves the room
   */
  beforeDestroy() {
    this.voiceChat.leaveRoom();
  }
  
}
</script>

<style lang="scss" scoped>
#voice-chat {
  position: absolute;
  right: 70px;
  cursor: pointer;
}

.hover-white {

  transition: 0.1s ease-in all;
  &:hover {
    color: white;
  } 
}
</style>