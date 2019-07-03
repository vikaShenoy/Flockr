<template>
  <div id="destinations">
    <div id="map">
      <DestinationMap />
    </div>
    <DestinationSidebar />

  </div>

  
</template>

<script>
import DestinationSidebar from "./DestinationSidebar/DestinationSidebar";
import DestinationMap from "../../components/DestinationMap/DestinationMap";

export default {
  components: {
    DestinationSidebar,
    DestinationMap 
  },
}
</script>

<style lang="scss" scoped>
  #destinations {
    width: 100%;
  }

  #map {
    width: calc(100% - 300px);
    display: inline-block;
    height: 100%;
    position: fixed;
  }

</style>




