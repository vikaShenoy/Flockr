<template>
  <v-toolbar id="navbar" >
    <v-toolbar-title>Flockr</v-toolbar-title>
    <v-spacer/>
    <v-toolbar-items class="hidden-sm-and-down">
    </v-toolbar-items>
  </v-toolbar>
</template>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

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
</style>

