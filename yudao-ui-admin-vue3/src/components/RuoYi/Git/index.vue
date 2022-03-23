<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('https://gitee.com/y_project/RuoYi-Vue');

function goto() {
  window.open(url.value)
}
</script>
