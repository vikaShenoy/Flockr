<template>
  <div>
    <div>
      <div id="basic-info-header">
    <h3>Basic Info</h3>
  </div>

  <div id="edit">
  <v-btn small flat id="edit-btn" color="secondary"><v-icon>edit</v-icon></v-btn>
  </div>
  </div>

  <v-card id="basic-info">

    <div class="profile-item"> 
      <div class="item-signifier">
        <b>First Name</b>
      </div>
      
      <div class="item-value">
        <span>{{ firstName }}</span>
      </div>
    </div>

    <v-divider />


    <div class="profile-item">
      <div class="item-signifier">
        <b>Middle Name</b> 
      </div>

      <div class="item-value">
        <span> {{ middleName }} </span>
      </div>
    </div>

    <v-divider />


    <div class="profile-item">
      <div class="item-signifier">
        <b>Last Name</b>
      </div>

      <div class="item-value"> 
      <span>{{ lastName }}</span>
    </div>

    <v-divider />

    <div class="profile-item">
      <div class="item-signifier">
        <b>Date of Birth</b>
      </div>

      <div class="item-value">
        <span>{{ dateOfBirth }}</span>
      </div>
      </div>
    </div>

    <v-divider />

    <div class="profile-item">

      <div class="item-signifier">
        <b>Gender</b>
      </div>

      <div class="item-value">
        <span>{{ gender.genderName }}</span>
      </div>
    </div>
  </v-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      firstName: "Rafael",
      middleName: "Hello World",
      lastName: "Goesmann",
      dateOfBirth: 1552879628181,
      gender: {
        genderName: "Male"
      },
      "nationalities": [
        {
          "nationalityId": 1,
          "nationalityName": "NZ"
        }
      ],
      "passports": [
        {
          "passportId": 1,
          "passportCountry": "Passports"
        }
      ],
      "travellerTypes": [
        {
          "travellerTypeId": 1,
          "travellerTypeName": "string"
        }
      ]
    };
  }
}
</script>

<style lang="scss" scoped>
  h3 {
    font-size: 1.2rem;
  }

  b {
    font-size: 1.05rem;
  }
  
  #basic-info {
    padding: 10px;
  }

  .profile-item {
    margin-top: 7px;
    margin-bottom: 7px;
  }

  .item-signifier {
    display: inline-block;
    width: 50%;
  }

  .item-value {
    display: inline-block;
    width: 50%;
  }

  #basic-info-header {
    width: 40%;
    display: inline-block;

    h3 {
      margin-bottom: 0 !important;
    }
  }

  #edit {
    width: 60%;
    display: inline-block;

    #edit-btn {
      float: right;
    }
  }

</style>


