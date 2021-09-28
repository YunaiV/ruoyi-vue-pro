<template>
</template>

<script>
import { setToken } from '@/utils/auth'

export default {
  created() {
    const {token} = this.$route.query;
    setToken(token);
    this.$router.push({ path: this.redirect || "/" }).catch(() => {});
  },
}
</script>

<style>

</style>
