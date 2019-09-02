<template>
  <div>
    <v-icon class="hover-white" @click="toggleVoiceChat" >
      {{ isInChat ? "call_end" : "speaker_phone" }}
    </v-icon>

      <audio ref="roomAudio" autoplay></audio>
      <audio ref="join" src="user_join.mp3" autoplay></audio>

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
      voiceChat: null,
        soundEffects: null
    };
  },
  mounted() {

      this.soundEffects = {
          join: new Audio(require("../../../../assets/user_join.mp3")),
          leave: new Audio(require("../../../../assets/user_leave.mp3"))
      };


    this.voiceChat = new VoiceChat();
    
    // Event gets emitted when a new user connects
    this.voiceChat.on("remoteUserConnected", stream => {
        Janus.attachMediaStream(this.$refs.roomAudio , stream)
    });

    this.voiceChat.on("joined", this.handleJoin);

    this.voiceChat.on("left", this.handleLeave);

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
    },
      handleJoin() {
          this.isInChat = true;
          this.soundEffects.join.play();
      },
      handleLeave() {
          this.isInChat = false;
          this.soundEffects.leave.play();
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