<template>
  <v-app>
    <Navbar/>
    <div id="app">

      <Sidebar/>

      <div class="container-fluid" id="content">
        <router-view></router-view>
      </div>

    </div>
  </v-app>
</template>

<script>
import Sidebar from "./Sidebar/Sidebar";
import Navbar from "./Navbar/Navbar";


export default {
  components: {
    Sidebar,
    Navbar
  },
  name: "App",
  data() {
    return {}
  }
}
</script>

<style lang="scss">
  @import url("https://fonts.googleapis.com/css?family=Roboto");
  @import "../../styles/_defaults.scss";

  #app {
    display: flex;
    flex-direction: row;
    height: 100%;
  }

  #content {
    display: flex;
    width: calc(100% - 240px);
    height: calc(100% - 64px);
    margin-left: 240px;
    padding-left: 0px;
    padding-right: 0px;
    margin-top: 64px;
  }

</style>

