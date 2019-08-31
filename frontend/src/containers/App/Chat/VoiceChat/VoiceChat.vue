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
    this.voiceChat.on("remoteUserConnected", stream => {
        Janus.attachMediaStream(this.$refs.roomAudio , stream)
    });

    this.voiceChat.on("error", error => {
        console.log(error);
    });
  },
  methods: {
    toggleVoiceChat() {
      console.log(this.chatGroup);
      if (!this.isInChat) {
        this.voiceChat.joinRoom(this.chatGroup.chatGroupId);
      }
      this.isInChat = !this.isInChat;
    }
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