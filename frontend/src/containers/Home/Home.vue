<template>
  <div id="home">
    <div>
      <h2>Welcome to </h2>
      <h1>Flockr</h1>
      <v-btn color="primary" depressed @click="hasJoined ? leaveRoom() : joinRoom()">{{ hasJoined ? "Leave" : "Join" }} Room</v-btn>
      
      <audio id="roomaudio" autoplay></audio>

      <ul>
        <li v-for="participant in participants" v-bind:key="participant.id">{{ participant.id }}</li>
      </ul>

      
    </div>
  </div>
</template>

<script>
  import { joinRoom, VoiceChat } from "./webrtc";
  import Janus from "./janus";

  let voiceChat;

  export default {
    data() {
      return {
        streamFound: false,
        participants: [],
        hasJoined: false
      }
    },
    mounted() {
        const room = 1234;
        voiceChat = new VoiceChat(room);
        voiceChat.on("remoteUserConnected", stream => {
          Janus.attachMediaStream(document.querySelector('#roomaudio'), stream);
        });

        voiceChat.on("error", error => {
          console.log(error);
        });

        voiceChat.on("participants", participants => {
          console.log("The participants are: " + participants);
          // Add yourself to the participants
          participants.push({id: 1}); 
          this.participants = participants;
        });

        voiceChat.on("participantLeft", userId => {
          this.participants = this.participants.filter(participant => {
            return participant.id !== userId;
          });
        });
    },
    methods: {
      remoteStreamAdded(stream) {
        this.stream = stream; 
      },
      joinRoom() {
        this.hasJoined = true;
        voiceChat.joinRoom();
      },
      leaveRoom() {
        this.hasJoined = false;
        voiceChat.destroySession();
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

