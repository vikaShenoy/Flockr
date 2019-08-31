<template>
  <v-card flat>
    <v-card-title>Create New Chat</v-card-title>

    <v-text-field
      label="Chat name"
    ></v-text-field>

    <v-select
      label="Members"
    ></v-select>

    <v-spacer align="center">
      <v-btn>
        <v-icon>add</v-icon>
      </v-btn>
    </v-spacer>

  </v-card>

</template>

<script>
  export default {
    name: "CreateChat",
    props: {
      isShowing: {
        type: Boolean,
        required: false
      },
    },
    data() {
      return {
      };
    },
    watch: {
      isShowing(value) {
        this.isShowing = value;
      }
    }

  }
</script>

<style scoped>

</style>