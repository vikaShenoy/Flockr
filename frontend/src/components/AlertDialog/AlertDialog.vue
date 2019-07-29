<template>
  <v-dialog
          v-model="dialog"
          width="500"
          persistent
  >
    <v-card>
      <v-card-title class="error headline">
        <v-spacer align="center">
          <h3 style="color: #FFF">{{ title }}</h3>
        </v-spacer>
      </v-card-title>
      <v-card-text>
        <v-flex>
          <slot></slot>
        </v-flex>
      </v-card-text>
      <v-responsive>
        <v-layout row>
          <v-spacer align="center">
            <v-btn color="secondary" flat @click="$emit('update:dialog', false)">OK</v-btn>
          </v-spacer>
        </v-layout>
      </v-responsive>
    </v-card>
  </v-dialog>
</template>

<script>
  export default {
    name: "prompt-dialog",
    data() {
      return {
        isLoading: false
      };
    },
    props: {
      dialog: Boolean,
      title: String
    }
  }
</script>

<style lang="scss" scoped>

  pre {
    white-space: pre-wrap;      /* Since CSS 2.1 */
    white-space: -moz-pre-wrap; /* Mozilla, since 1999 */
    white-space: -o-pre-wrap;   /* Opera 7 */
    word-wrap: break-word;      /* Internet Explorer 5.5+ */
    font-family: 'Roboto', sans-serif;
    font-size: 1em;
    text-align: center;
  }

</style>
