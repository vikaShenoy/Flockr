<template>
  <v-card flat>
    <v-flex>

      <v-text-field
        class="light-pad"
        v-model="chatGroup.name"
      >
      </v-text-field>

      <v-select
              class="light-pad"
      >

      </v-select>

      <UserSummary
        class="light-pad"
        v-for="user in chatGroup.users"
        v-bind:key="user.userId"
        :user=user
      ></UserSummary>

      <v-spacer align="center">
        <v-btn
          color="secondary">
          Delete Chat
        </v-btn>
      </v-spacer>

    </v-flex>
  </v-card>
  
</template>

<script>
  import UserSummary from "./UserSummary"
  export default {
    name: "ManageChat",
    components: {
      UserSummary,
    },
    methods: {

    },
    mounted() {
      console.log(this.chatGroup);
    },
    props: {
      isShowing: {
        type: Boolean,
        required: false,
      },
      chatGroup: {
        type: Object,
        required: false,
      }
    },
    data() {
      return {

      }
    },
    watch: {
      isShowing(value) {
        this.isShowing = value;
      }
    }
  }
</script>

<style scoped>

.padding {
  padding: 20px;
  margin: 20px
}

.light-pad {
  padding: 5px;
  margin: 5px;
}

</style>