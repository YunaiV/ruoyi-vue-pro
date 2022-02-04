<template>
  <div>
    <svg-icon icon-class="question" @click="goto"/>
  </div>
</template>

<script>
export default {
  name: 'YudaoDoc',
  data() {
    return {
      url: 'http://www.iocoder.cn/Yudao/build-debugger-environment/?yudao'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
