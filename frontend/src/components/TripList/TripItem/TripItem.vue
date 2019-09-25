<template>
  <div>
    <v-layout>
      <v-flex shrink>
        <v-btn
          v-bind:class="{'hide-delete-btn' : !hasPermissionToDelete}"
          v-if="!viewOnly"
          fab
          small
          color="secondary"
          style="margin: 20px;"
          @click="deleteTrip"
        >
          <v-icon>delete</v-icon>
        </v-btn>
      </v-flex>

      <v-flex style="margin-bottom: 0px">
        <v-card
          :class="{'trip-item': true, 'elevation-0': viewOnly, 'trip-border': viewOnly}"
          @click="$router.push(`/trips/${trip.tripId}`)"
        >
          <div class="status">
            <v-icon
              v-if="trip.status === 'Upcoming'"
              style="font-size: 40px;color: #FFF;"
            >flight_takeoff</v-icon>
            <v-icon
              v-else-if="trip.status === 'Passed'"
              style="font-size: 40px;color: #FFF;"
            >flight_landing</v-icon>
            <v-icon
              v-else-if="trip.status === 'Ongoing'"
              style="font-size: 40px;color: #FFF;"
            >flight</v-icon>
          </div>

          <div class="content">
            <h2>{{ trip.tripName }}</h2>

            <b>Status:</b>
            <span>{{ trip.status }}</span>
          </div>
        </v-card>
      </v-flex>
    </v-layout>
  </div>
</template>

<script>
import UserStore from '../../../stores/UserStore';
import roleType from '../../../stores/roleType';
export default {
  props: {
    trip: {
      type: Object,
      required: true
    },
    viewOnly: {
      type: Boolean, // whether to hide action buttons
      required: false
    }
  },
  methods: {
    /**
     * Delete the trip being viewed. Emit calls to the tripList component to visually update.
     * @returns {Promise<void>}
     */
    async deleteTrip() {
      const { tripId } = this.trip;
      this.$emit("refreshList");
      this.$emit("handleDelete", tripId);
    }
  },
  computed: {
    /**
     * Check if the user can delete a trip.
     * Permission granted if they have the trip owner role.
     */
    hasPermissionToDelete() {
      console.log(this.trip);
      const userRole = this.trip.userRoles.find(
        userRole => userRole.user.userId === UserStore.data.userId
      );

      const isTripManager =
        userRole && userRole.role.roleType === roleType.TRIP_MANAGER;
      const isTripOwner =
        userRole && userRole.role.roleType === roleType.TRIP_OWNER;

      return isTripManager || isTripOwner;
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../../styles/_variables.scss";

.trip-item {
  margin: 7px;
  cursor: pointer;
  height: 100%;

  &:hover {
    background-color: #f4f4f4;
  }

  .status {
    display: flex;
    align-items: center;
    padding-left: 5px;
    padding-right: 5px;
    float: right;
    height: 100%;
    background-color: $secondary;
    color: #fff;
    width: 50px;
  }
}

.content {
  padding-top: 10px;
  padding-left: 10px;
}

.hide-delete-btn {
  visibility: hidden;
}

.trip-border {
  border: 1px solid rgba(0, 0, 0, 0.12);
}
</style>



