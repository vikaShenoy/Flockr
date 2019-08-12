<template>
  <v-app>
    <Navbar/>
    <Sidebar/>
    <v-content>
      <v-container fluid fill-height id="container">
        <v-layout>
          <v-flex>
            <GlobalSnackbar />
            <router-view/>
          </v-flex>
        </v-layout>
      </v-container>
    </v-content>
  </v-app>
</template>

<script>

  import Sidebar from "./Sidebar/Sidebar";
  import Navbar from "./Navbar/Navbar";
  import GlobalSnackbar from "../../components/Snackbars/GlobalSnackbar";


  export default {
    components: {
      Sidebar,
      Navbar,
      GlobalSnackbar
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

  #container {
    padding: 0;
  }

</style>

