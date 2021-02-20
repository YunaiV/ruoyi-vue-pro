<template>
  <div>
    <svg-icon icon-class="github" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'YudaoGit',
  data() {
    return {
      url: 'https://github.com/YunaiV/ruoyi-vue-proe'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
