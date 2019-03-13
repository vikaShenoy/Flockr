<template>
    <v-card class="col-md-12 offset-md-0 d-flex">
        Hello
        <v-card class="col-md-3 offset-md-0 d-inline-block">
            <h2 class="name-header">{{ dest.name }}</h2>
        </v-card>
        <v-card class="col-md-12">
            <v-img src="src/assets/logo.png"></v-img>
        </v-card>
    </v-card>
</template>

<script>
  export default {
    name: "DestinationCard"
  }
</script>

<style scoped>

</style>