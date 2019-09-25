<template>
  <div>
    <v-icon class="hover-white" @click="toggleVoiceChat" color="secondary">
      {{ isInChat ? "call_end" : "speaker_phone" }}
    </v-icon>

      <audio ref="roomAudio" autoplay></audio>
      <audio ref="join" src="user_join.mp3" autoplay></audio>
  </div>
</template>

<script>
import { VoiceChat } from "./voice";
import Janus from "./janus";
import UserStore from "../../../../stores/UserStore.js";

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
    /**
     * The sound effects setup
     * @type {{leave: HTMLAudioElement, join: HTMLAudioElement}}
     */
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

    this.voiceChat.on("participants", this.handleParticipants);

    this.voiceChat.on("participantLeft", this.handleParticipantLeft);

    this.voiceChat.on("left", this.handleLeave);

    this.voiceChat.on("error", error => {
        this.$root.$emit("show-snackbar", {
          timeout: 3000,
          color: "error",
          message: error.message
        });
    });
  },
  methods: {
    /**
     * Handles the user joining and leaving the chat
     */
    toggleVoiceChat() {
      if (!this.isInChat) {
        this.voiceChat.joinRoom(this.chatGroup.chatGroupId, UserStore.data.userId);

      } else {
        this.voiceChat.leaveRoom();
      }
    },
    /**
     * This function is called when a user joins the chat
     */
      handleJoin() {
          this.isInChat = true;
          this.soundEffects.join.play();
      },
    /**
     * This function is called when a user leaves the chat
     */
      handleLeave() {
        this.$emit("participants", []);
        this.isInChat = false;
        this.soundEffects.leave.play();
      },
      handleParticipants(participants) {
        // Have to add self to participant ID's
        const participantIds = participants.map(participant => participant.id);
        participantIds.push(UserStore.data.userId);
        this.$emit("participants", participantIds);
      },
      handleParticipantLeft(userId) {
        this.$emit("participantLeft", userId);
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
    color: white !important;
  } 
}

</style>