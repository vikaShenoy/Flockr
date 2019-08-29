<template>
  <v-app>
    <Navbar/>
    <Sidebar/>
    <v-content>
      <v-container fluid fill-height id="container">
        <v-layout>
          <v-flex>
            <GlobalSnackbar />
            <Chat />
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
import Chat from "./Chat/Chat";

export default {
  components: {
    Sidebar,
    Navbar,
    GlobalSnackbar,
    Chat
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
    margin-top: 60px;
    height: calc(100% - 60px);
  }

</style>

