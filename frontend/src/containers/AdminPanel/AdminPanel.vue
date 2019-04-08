<template>
  <div class="admin-panel">
    <h2>Admin Panel</h2>
    <ManageUsers />
    
  </div>
</template>


<script>
import {deleteUsers} from "./AdminPanelService";
import ManageUsers from "./ManageUsers/ManageUsers.vue";

export default {
  components: {
    ManageUsers
  },
};
</script>


<style lang="scss" scoped>
  .admin-panel {
    width: 100%;
    height: 100%;
    padding: 10px;
  }
</style>
