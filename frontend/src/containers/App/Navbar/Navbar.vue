<template>
  <v-toolbar id="navbar" >
    <v-toolbar-side-icon>
      <v-img :src="flockrLogo" contain />
    </v-toolbar-side-icon>
    <v-toolbar-title id="flockr-title">Flockr</v-toolbar-title>
    <v-spacer/>
    <v-toolbar-items class="hidden-sm-and-down">
    </v-toolbar-items>
  </v-toolbar>
</template>

<script>
  export default {
    data() {
      return {
        flockrLogo: require("../../../assets/flockr-logo.svg")
      }
    }
  }
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";
  @import "../../../styles/_defaults.scss";

  #navbar {
    display: flex;
    flex-direction: row;
    justify-content: flex-start;
    align-items: center;
    position: fixed;
    padding-left: 15px;
    width: 100%;
    background-color: $secondary;
    color: white;
    z-index: 3;
    height: 60px;
  }

  #flockr-title {
    font-family: 'Comfortaa', cursive;
  }
</style>

