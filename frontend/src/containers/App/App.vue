<template>
  <v-app>
    <div id="app">
      <Navbar />

      <div class="container-fluid" id="content">
        <router-view></router-view>
      </div>

    </div>
  </v-app>
</template>

<script>

import Navbar from "./Navbar/Navbar";

export default {
  components: {
    Navbar
  },
  name: "App",
  data () {
    return {
    }
  }
}
</script>

<style lang="scss">
@import url("https://fonts.googleapis.com/css?family=Roboto");
@import "../../styles/_defaults.scss";

#app {
  display: flex;
  flex-direction: row;
  height: 200%;
}

#content {
  display: flex;
  height: 100%;
  width: calc(100% - 240px);
  margin-left: 240px;
}

</style>

