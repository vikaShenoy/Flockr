<template>
  <v-app-bar id="navbar">
    <v-toolbar-title>Travel EA</v-toolbar-title>
    <v-spacer/>
    <v-toolbar-items class="hidden-sm-and-down">
    </v-toolbar-items>
  </v-app-bar>
</template>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  #navbar {
    width: 100%;
    background-color: $secondary;
    color: white;
    z-index: 3;
  }
</style>

