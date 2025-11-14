<script setup lang="ts"></script>

<template>
  <div class="vp-doc vben-contributors">
    <p>Contributors</p>
    <a href="https://github.com/vbenjs/vue-vben-admin/graphs/contributors">
      <img
        alt="Contributors"
        src="https://opencollective.com/vbenjs/contributors.svg?button=false"
      />
    </a>
  </div>
</template>

<style scoped>
.vben-contributors {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 60px;

  p {
    margin-bottom: 50px;
    font-size: 30px;
    font-weight: 700;
  }
}
</style>
