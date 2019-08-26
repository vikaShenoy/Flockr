<template>
  <div id="home">
    <div>
      <h2>Welcome to </h2>
      <h1>Flockr</h1>
      <v-btn color="primary" depressed @click="joinRoom">Join Room</v-btn>
      <audio v-if="stream" :srcObject="stream" autoplay></audio>
    </div>
  </div>
</template>

<script>
  import { joinRoom, VoiceChat } from "./webrtc";

  let voiceChat;

  export default {
    data() {
      return {
        stream: null
      }
    },
    mounted() {
      const room = 1234;
      voiceChat = new VoiceChat(room);
      voiceChat.on("remoteUserConnected", stream => {
        console.log("This.stream is: " + this.stream);
        this.stream = stream;
      });

      voiceChat.on("error", error => {
        console.log(error);
      });

      voiceChat.on("participants", participants => {
        console.log(participants);
      })
    },
    methods: {
      remoteStreamAdded(stream) {
        this.stream = stream; 
      },
      joinRoom() {
        voiceChat.joinRoom();
      } 
    },
    
  }
</script>


<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #home {
    display: flex;
    align-items: center;
    justify-content: center;
    background-image: url("../../assets/background.jpg");
    background-size: cover;
    box-shadow: inset 0 0 0 1000px rgba(0, 0, 0, .5);
    width: 100%;
    height: 100%;

    h1 {
      font-size: 5rem;
      text-align: center;
      color: #FFF;
      text-shadow: 1px 1px 1px #000;
    }

    h2 {
      font-size: 2rem;
      text-align: center;
      color: #FFF;
    }
  }
</style>

