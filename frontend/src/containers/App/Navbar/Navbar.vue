<template>
  <v-navigation-drawer permanent id="navbar" :width="240">
    <v-toolbar flat id="title-box">
      <v-list>
        <v-list-tile>
          <v-list-tile-title id="title">
            Travel EA
          </v-list-tile-title>
        </v-list-tile>
      </v-list>
    </v-toolbar>

    <v-divider class="light-divider"></v-divider>

    <v-list dense class="pt-0">
      <v-list-tile
        v-for="item in items"
        :key="item.title"
        class="nav-item"
        @click="$router.push(item.url)"
        v-if="shouldShowNavbar(item)"
      >
        <v-list-tile-action>
          <v-icon class="nav-icon">{{ item.icon }}</v-icon>
        </v-list-tile-action>

        <v-list-tile-content>
          <v-list-tile-title>{{ item.title }}</v-list-tile-title>
        </v-list-tile-content>
      </v-list-tile>
    </v-list>

    <v-divider class="light-divider"></v-divider>
    
    <v-list dense class="pt-0">
      <v-list-tile
        v-for="item in supplementaryItems"
        :key="item.title"
        class="nav-item"
        @click="$router.push(item.url)"
      >
        <v-list-tile-action>
          <v-icon class="nav-icon">{{ item.icon }}</v-icon>
        </v-list-tile-action>

        <v-list-tile-content>
          <v-list-tile-title>{{ item.title }}</v-list-tile-title>
        </v-list-tile-content>
      </v-list-tile>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
export default {
  data() {
    return {
      items: [
        {
          title: "Home",
          icon: "dashboard",
          url: "/",
          loggedIn: true,
          loggedOut: true
        },
        {
          title: "Search Travellers",
          icon: "search",
          url: "/search",
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Destinations",
          icon: "location_on",
          url: "/destinations",
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Trips",
          icon: "navigation",
          url: "/trips",
          loggedIn: true,
          loggedOut: false
        }
      ],
      supplementaryItems: [
        {
          title: "Sign up",
          icon: "person_add",
          url: "/signup",
          loggedIn: false,
          loggedOut: true 
        },
        {
          title: "Log in",
          icon: "exit_to_app",
          url: "/login",
          loggedIn: false,
          loggedOut: true
        },
        {
          title: "Profile",
          icon: "face",
          url: "/profile",
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Log out",
          icon: "power_settings_new",
          loggedIn: true,
          loggedOut: false
        }
      ]
    };
  },
  methods: {
    /**
     * Decides if a nav-item should be shown or not
     * @param {string} loggedIn - Indicates if nav item should be shown when signed in
     * @param {string} loggedOut - indicates if the nav item should be shown when logged out
     */
    shouldShowNavbar(loggedIn, loggedOut) {
      return loggedIn || loggedOut;
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  #navbar {
    background-color: $primary;
  }

  #title-box {
    background-color: $primary;
  }

  #title {
    color:$darker-white;  
  }

  .nav-icon {
    color:$darker-white;  
  }

  .nav-item {
    color: $darker-white;
  }

  .light-divider {
    background-color: $lighter-primary;
  }

</style>


