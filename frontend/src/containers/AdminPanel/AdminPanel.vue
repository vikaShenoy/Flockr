<template>
  <div class="admin-panel">
    <h2>Admin Panel</h2>

    <v-card>
      <v-list two-line>
        <v-subheader>
          Manage users
        </v-subheader>
        <template v-for="(item) in items">
          <v-list-tile v-if="item.title" :key="item.title" avatar @click="console.log('Click on tile')">
            <v-list-tile-avatar>
              <img :src="item.avatar">
            </v-list-tile-avatar>

            <v-list-tile-content>
              <v-list-tile-title v-html="item.title"></v-list-tile-title>
              <v-list-tile-sub-title v-html="item.subtitle"></v-list-tile-sub-title>
            </v-list-tile-content>
          </v-list-tile>
        </template>
      </v-list>
    </v-card>
  </div>
</template>


<script>

export default {
  data() {
    return {
      items: [
        {
          avatar: "https://cdn.vuetifyjs.com/images/lists/1.jpg",
          title: "Tom Belmonte",
          subtitle: "Joined on 12/03/2007"
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/2.jpg',
          title: 'Roger Belmonte',
          subtitle: "Joined on 12/03/2008"
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/3.jpg',
          title: 'Teresa Rojas',
          subtitle: "Joined on 12/03/2004"
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/4.jpg',
          title: 'Juan Galindo',
          subtitle: "Joined on 12/03/2017"
        }
      ]
    };
  },
  mounted() {
    
  },
  methods: {
    
  }
};
</script>

<style lang="scss" scoped>
    .admin-panel {
        height: 100%;
        width: 100%;
        padding: 10px;
    }
</style>
