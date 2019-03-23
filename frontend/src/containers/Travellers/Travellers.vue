<template>
  <div style="width: 100%">
    <div v-if="travellers">
    <div v-if="travellers.length">
      <div
        class="row"
        v-for="traveller in travellers"
        v-bind:key="traveller.userId"
      >
        <Traveller :traveller="traveller"/>
     </div>
    </div>
    </div>
    <div v-else>Sorry there are no travellers</div>
  </div>
</template>

<script>
import Traveller from "./Traveller/Traveller";

import { getTravellers } from "./TravellersService";

export default {
  components: {
    Traveller
  },
  data() {
    return {
      travellers: null
    };
  },
  mounted() {
    this.getTravellers();
  },
  methods: {
    async getTravellers() {
      try {
        const travellers = await getTravellers();

        this.travellers = travellers;
      } catch (e) {
        console.log(e);
        // Add error handling later
      }
    }
  }
};
</script>


