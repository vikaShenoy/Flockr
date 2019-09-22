<template>
  <div>
  <div class="destination-summary" @click="$router.push(`/destinations/${destination.destinationId}`)">

    <v-avatar :size="40"><img
            :src="imageSrc"
            alt="avatar"
            class="avatar"
    />


    </v-avatar>

    <div class="content">
      <h4 class="destination-title">{{ this.destination.destinationName }}</h4>
      <br/>
      <span class="destination-district">{{ this.destination.destinationDistrict.districtName }}</span>
    </div>


    <v-icon color="secondary" class="destination-lock">
      {{ this.destination.isPublic ? "lock_open" : "lock" }}
    </v-icon>

    <v-icon color="error" class="delete-destination" v-if="this.destination.destinationOwner ===  userStore.data.userId || userStore.methods.isAdmin()"
            @click="event => showDeletePrompt(event, destination.destinationId)">
      delete
    </v-icon>

  </div>
    <v-divider></v-divider>
    </div>
</template>

<script>
  import superagent from "superagent";
  import {endpoint} from "../../../../utils/endpoint";
  import UserStore from "../../../../stores/UserStore";

  export default {
    mounted() {
      this.getDestinationPhoto(this.destination.destinationId)
    },
    props: {
      destination: {
        type: Object
      }
    },
    data() {
      return {
        imageSrc: "https://www.tibs.org.tw/images/default.jpg",
        userStore: UserStore
      }
    },
    methods: {
      getDestinationPhoto: async function (destinationId) {
        const res = await superagent.get(endpoint(`/destinations/${destinationId}/photos`))
            .set("Authorization", localStorage.getItem("authToken"));
        if (res.body.length) {
          const photoId = res.body[0].personalPhoto.photoId;
          this.imageSrc = endpoint(`/users/photos/${photoId}`) + '?Authorization=' + localStorage.getItem("authToken");
        }
      },
      showDeletePrompt(event, destinationId) {
        event.stopPropagation();
        this.$emit("showDeleteDestination", destinationId);
      }
    }
  };
</script>

<style lang="scss" scoped>
  @import "../../../../styles/_variables.scss";


  .content {
    display: inline-block;
    margin-left: 5px;
    display: flex;
    align-items: center;
    margin-left: 10px;
  }

  .destination-summary {
    cursor: pointer;
    transition: 0.2s background-color linear;
    height: 50px;
    display: flex;
    align-items: center;
    padding-left: 5px;


    &:hover {
      background-color: #dce6ef;
    }
  }

  .destination-lock {
    opacity: 0.7;
    margin-left: auto;
    margin-right: 15px;
    
  }

  .destination-title {
    display: inline-block;
  }

  .destination-district {
    font-size: 0.9rem;
    color: $text-dark-grey;
  }


  .delete-destination {
    transition: background-color 0.1s linear;
    z-index: 100;
    margin-left: auto;
    margin-right: 5px;

    &:hover {
      color: #c53e3e !important;
    }
  }

</style>


