<template>
  <div id="home">
    <div>
      <h2>Welcome to </h2>
      <h1>Travel EA</h1>
    </div>
  </div>
</template>

<script>
import { resample } from "./HomeService";

export default {
  mounted() {
    try {
      resample(); 
    } catch (e) {
      console.log(e);
    }
  }  
}
</script>


<style lang="scss" scoped>
@import "../../styles/_variables.scss";
  #home {
    display: flex;
    align-items: center;
    justify-content: center;
    background-image: url("./home-background.jpg");
    background-size: cover;
    box-shadow: inset 0 0 0 1000px rgba(0,0,0,.5);
    width: 100%;
    height: 100%;

    h1 {
      font-size: 5rem;
      text-align: center;
      color: #FFF;
      text-shadow: 1px 1px 1px #000;
    }

    h2 {
      font-size: 2rem;
      text-align: center;
      color: #FFF;
    }
  }
</style>

