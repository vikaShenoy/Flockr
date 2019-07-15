<template>
  <div class="destination-summary" @click="$router.push(`/destinations/${destination.destinationId}`)">
    <v-avatar> <img
          src="https://vuetifyjs.com/apple-touch-icon-180x180.png"
          alt="avatar"
          class="avatar"
        />


    </v-avatar>

    <div class="content">
      <h4 class="destination-title">{{ this.destination.destinationName }}</h4>
      <br />
      <span class="destination-district">{{ this.destination.destinationDistrict.districtName }}</span>
    </div>

    <v-icon color="secondary" class="destination-lock">
      {{ this.destination.isPublic ? "lock_open" : "lock" }}
    </v-icon>
    <v-divider></v-divider>
  </div>
</template>

<script>
import superagent from "superagent";
import { endpoint } from "../../../../utils/endpoint";

export default {
  mounted() {
    this.testingPrintPhotos();
  },
  props: {
    destination: {
      type: Object
    }
  },
  methods: {
    testingPrintPhotos: function() {
      console.log("Testing");
    },

    getDestinationPhoto: async function(destinationId) {
      const res = await superagent.get(endpoint(`/destinations/${destinationId}/photos`))
        .set("Authorization", localStorage.getItem("authToken"));
      let destinationPhotoId = res.body[0].destinationPhotoId  
      return endpoint(`/destinations/${destinationId}/photos/${destinationPhotoId}`);
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../../../styles/_variables.scss";


.content {
  width: 160px;
  display: inline-block;
  margin-top: 5px;
}

.destination-summary {
  cursor: pointer;
  transition: 0.2s background-color linear;

  &:hover {
    background-color: #dce6ef;
  }
}

.destination-lock {
  opacity: 0.7;
  float: right;
  margin-top: 17px;
  margin-right: 10px;
}

.destination-title {
  display: inline-block;
  margin-top: 0px;
}

.destination-district {
  font-size: 0.9rem; 
  color: $text-dark-grey;
}

.avatar {
  margin-top: -10px;
}

</style>


