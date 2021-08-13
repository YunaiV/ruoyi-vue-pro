<template>
  <div>登录成功！</div>
</template>

<script>
import { getToken, setToken, removeToken } from '@/utils/auth'

export default {
  created() {
    var token = this.getOauthToken();
    setToken(token);
    this.$router.push({ path: this.redirect || "/" }).catch(() => {});
  },
  methods: {
    getOauthToken() {
      console.log(this);
      var h = window.location.href;
      var token = "token=";
      return h.substring(h.indexOf(token)+token.length);
    }
  },
}
</script>

<style>

</style>
