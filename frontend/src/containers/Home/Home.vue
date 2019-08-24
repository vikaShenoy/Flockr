<template>
  <div id="home">
    <div>
      <h2>Welcome to </h2>
      <h1>Flockr</h1>
      <v-btn
        color="secondary"
        depressed
        @click="addToRoom"
      >Connect janus
      </v-btn>

      <div id="mixedaudio"></div>
    </div>
  </div>
</template>

<script>
  import { initJanus, addToRoom } from "./audioBridge";

  export default {
    data() {
      return {
        remoteStream: null
      }
    },
    mounted() {
      initJanus(this.remoteStreamAdded);
    },
    methods: {
      addToRoom() {
        addToRoom();
      },
      remoteStreamAdded(remoteStream) {
        this.$refs.audio.srcObject = remoteStream;
      }
    }
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

