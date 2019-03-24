<template>
  <div>
    <h3 style="margin-bottom: 4px;margin-top: 4px;">Photos</h3>

    <v-card id="photos">
      <div class="row">
        <div class="col-lg-4 col-md-6 col-sm-12" v-for="photo in photos" v-bind:key="photo">
          <img class="photo" :src="require(`${photo}`)" alt="Photo" />
        </div>
      </div>

      <v-btn id="add-btn" outline color="secondary"><v-icon>add</v-icon></v-btn>
    </v-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      photos: ["./beach-holiday.jpg", "./desert-holiday.jpeg", "./snow-holiday.jpg"]
    };
  } 
}
</script>

<style lang="scss" scoped>
  #photos {
    padding: 20px;
  }

  .photo {
    width: 100%;
    height: auto;
  }

  #add-btn {
    display: block;
    margin: 0 auto;
  }
</style>


