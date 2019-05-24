<template>
  <div>
    <h3 style="margin-bottom: 4px;margin-top: 4px;">Photos</h3>

    <v-card id="photos">
      <v-card-actions>
        <v-spacer align="left">
          <v-btn v-if="photos.length" color="secondary" outline v-on:click="openGallery">View Gallery</v-btn>
        </v-spacer>
      </v-card-actions>
      <v-responsive>
        <v-container grid-list-sm fluid>
          <!-- users photos -->
          <v-layout v-if="photos && photos.length" row wrap>
            <v-flex sm12 md6 lg4 v-for="(photo, index) in photos" v-bind:key="photo.photoId">
              <v-img v-if="index < 6" class="photo clickable"
                     :src="thumbnailUrl(photo.photoId)"></v-img>
            </v-flex>
          </v-layout>

          <!-- default photos -->
          <v-layout v-else row wrap>
            <v-spacer align="center">
              <h3 class="font-weight-regular">This user has no photos.</h3>
            </v-spacer>
          </v-layout>

        </v-container>
      </v-responsive>

      <v-card-actions>
        <v-spacer align="center">
          <add-photo-dialog
                  :dialog="addPhotoDialog"
                  v-on:closeDialog="closeAddPhotoDialog"
                  v-on:addImage="addImage"
          ></add-photo-dialog>
        </v-spacer>
      </v-card-actions>
    </v-card>

    <!--<view-photo-dialog-->
            <!--:dialog="viewPhotoDialog"-->
            <!--:photo="currentPhoto"-->
            <!--v-on:dialogChanged="closeViewPhotoDialog"-->
    <!--&gt;</view-photo-dialog>-->

  </div>
</template>

<script>
  import AddPhotoDialog from "./AddPhotoDialog/AddPhotoDialog";
  // import ViewPhotoDialog from "./ViewPhotoDialog/ViewPhotoDialog";
  import { endpoint } from "../../../utils/endpoint";

  export default {
    props: {
      photos: Array 
    },
    components: {
      AddPhotoDialog,
      // ViewPhotoDialog
    },

    data() {
      return {
        addPhotoDialog: false,
        userId: null,
        currentPhoto: null,
        viewPhotoDialog: false,
        hasPhotos: false
      };
    },

    methods: {

      // /**
      //  * Called when a photo is clicked on.
      //  * Sets the current photo as the one clicked on.
      //  * Opens the view photo dialog by setting viewPhotoDialog to true
      //  *
      //  * @param photo the photo clicked on by the user
      //  */
      // openViewPhotoDialog: function (photo) {
      //   this.currentPhoto = photo;
      //   this.viewPhotoDialog = true;
      // },
      //
      // /**
      //  * Called when the view photo dialog is closed by the user.
      //  * Sets the current photo to null again.
      //  * Closes the view photo dialog by setting viewPhotoDialog to false
      //  */
      // closeViewPhotoDialog: function (newValue) {
      //   this.currentPhoto = null;
      //   this.viewPhotoDialog = newValue;
      // },

      /**
       * Called when the add photo dialog is closed by the user.
       * Closes the add photo dialog by setting addPhotoDialog to false.
       */
      closeAddPhotoDialog: function () {
        this.addPhotoDialog = false;
      },
      /**
       * Called when the view gallery button is clicked.
       * Directs user to the photos page for this user.
       */
      openGallery() {
        this.$router.push(`/profile/${this.userId}/photos`);
      },
      thumbnailUrl(photoId) {
        return endpoint(`/users/photos/${photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
      },
      addImage(image) {
        this.$emit("addImage", image);
      }
    },

    mounted: async function () {
      this.userId = this.$route.params.id;
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

  /*.clickable :hover {*/
    /*cursor: pointer;*/
  /*}*/

</style>


