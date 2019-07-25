<template>
  <v-card id="destination-details"> 
    <v-layout row wrap>
      <v-flex class="destination-item" xs3>
        <v-icon color="secondary" style="font-size: 30px;">location_on</v-icon>
        <b>Location</b>
      </v-flex>

      <v-flex class="destination-value" xs9>
        {{ destination.destinationDistrict.districtName }},&nbsp;<CountryDisplay v-bind:country="destination.destinationCountry.countryName" />
      </v-flex>


      <v-flex class="destination-item" xs3>
        <v-icon color="secondary" style="font-size: 30px;">category</v-icon>
        <b>Destination Type</b>
      </v-flex>

      <v-flex class="destination-value" xs9>
        {{ destination.destinationType.destinationTypeName }}
      </v-flex>

      <v-flex class="destination-item" xs3>
        <v-icon color="secondary" style="font-size: 30px;">navigation</v-icon>
        <b>Latitude</b>
      </v-flex>

      <v-flex class="destination-value" xs9>
        {{ destination.destinationLat }}
      </v-flex>

      <v-flex class="destination-item" xs3>
        <v-icon color="secondary" style="font-size: 30px;">navigation</v-icon>
        <b>Longitude</b>
      </v-flex>

      <v-flex class="destination-value" xs9>
        {{ destination.destinationLon }}
      </v-flex>

      <v-flex class="destination-item" xs3>
        <v-icon color="secondary" style="font-size: 30px;">visibility</v-icon>
        <b>Public</b>
      </v-flex>

      <v-flex class="destination-value" xs9>
        {{ destination.isPublic ? "Yes" : "No" }}
      </v-flex>

      <v-flex class="destination-item" xs3>
        <v-icon color="secondary" style="font-size: 30px;">directions_walk</v-icon>
        <b>Traveller Types</b>
      </v-flex>

      <v-flex class="destination-value" xs9>
        <v-chip
          v-for="travellerType in destination.travellerTypes"
          v-bind:key="travellerType.travellerTypeId"
          color="primary"
          text-color="white"
        >
          {{ travellerType.travellerTypeName }}
        </v-chip>
      </v-flex>






    </v-layout>


   
  </v-card>
</template>


<script>
import CountryDisplay from "../../../components/Country/CountryDisplay";
export default {
  components: {CountryDisplay},
  props: ["destination"]
}
</script>

<style lang="scss" scoped>
#destination-details {
  padding: 10px;
}

.destination-item, .destination-value {
  display: flex;
  align-items: center;
  margin-top: 13px;
}

.destination-value {
  text-align: center;
  justify-content: center;
}

</style>


