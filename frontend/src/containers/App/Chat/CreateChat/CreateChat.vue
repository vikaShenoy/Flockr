<template>
  <v-card flat>
    <v-text-field
      class="padding"
      label="Chat name"
    ></v-text-field>

    <v-select
      class="padding"
      label="Members"
    ></v-select>

    <v-spacer align="center">
      <v-btn
        color="secondary"
        dark

        >
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

.padding {
  margin: 20px;
  padding: 20px;
}

</style>