<template>
  <v-dialog
          v-model="showDialog"
          width="80%"
  >
    <v-card>
      View photo stuff here.\n
      {{ photo }}
    </v-card>
  </v-dialog>
</template>

<script>

  export default {

    name: "photo-panel",

    props: {
      photo: {
        type: Object,
        required: true,
        endpoint: {
          type: String,
          required: false
        },
        isPrimary: {
          type: Boolean,
          required: true
        },
        isPublic: {
          type: Boolean,
          required: true
        }
      },
      showDialog: {
        type: Boolean,
        required: true
      }
    },

    methods: {
      closeDialog() {
        this.$emit("closeDialog");
      }
    }
  }
</script>

<style scoped>

</style>