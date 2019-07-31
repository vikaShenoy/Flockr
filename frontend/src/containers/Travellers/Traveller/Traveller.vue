<template>
  <v-card class="traveller col-md-10 offset-md-1" @click="$router.push(`/profile/${traveller.userId}`)">
    <div class="row">
      <div class="col-lg-4">
        <img
                class="profile-pic"
                src="./tempPic.jpg"
        >
      </div>
      <div class="col-lg-4">

        <div class="attribute-key"><b>First Name</b></div>
        <div class="attribute-value"><span>{{traveller.firstName}}</span></div>
        <div class="attribute-key"><b>Middle Name</b></div>
        <div class="attribute-value"><span>{{traveller.middleName}}</span></div>
        <div class="attribute-key"><b>Last Name</b></div>
        <div class="attribute-value"><span>{{traveller.lastName}}</span></div>
        <div class="attribute-key"><b>Gender</b></div>
        <div class="attribute-value"><span>{{traveller.gender}}</span></div>
        <div class="attribute-key"><b>Date of Birth</b></div>
        <div class="attribute-value"><span>{{ formatDateOfBirth(traveller.dateOfBirth) }}</span></div>
      </div>

      <div
              class="col-lg-4"
              style="border-left: 1px solid rgba(0,0,0,0.12)"
      >
        <div>
          <b>Nationalities</b>
          <br/>

          <v-chip
                  v-for="nationality in traveller.nationalities"
                  v-bind:key="nationality.nationalityId"
                  color="primary"
                  text-color="white"
          >{{ nationality.nationalityName }}
          </v-chip>
        </div>

        <div>
          <b>Passports</b>
          <br/>

          <v-chip
                  v-for="passport in traveller.passports"
                  v-bind:key="passport.passportId"
                  color="primary"
                  text-color="white"
          >{{ passport.passportCountry }}
          </v-chip>
        </div>

      </div>
    </div>
  </v-card>

</template>

<script>
  import moment from "moment";


  export default {
    props: ["traveller"],
    methods: {
      /**
       * Formats a timestamp to a readable date
       * @param {string} dateTimestamp The timestamp to format
       * @return {string} The formatted date
       */
      formatDateOfBirth(dateTimestamp) {
        return moment(dateTimestamp).format("YYYY-MM-DD");
      }
    }
  };
</script>


<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  .traveller {
    padding: 20px;
    margin-top: 20px;
    cursor: pointer;

    &:hover {
      background-color: #f4f4f4;
    }
  }

  .profile-pic {
    width: 100%;
    height: auto;
  }

  .attribute-key {
    margin-bottom: 10px;
    width: 50%;
    display: inline-block;
  }

  .attribute-value {
    margin-bottom: 10px;
    width: 50%;
    display: inline-block;
  }
</style>

