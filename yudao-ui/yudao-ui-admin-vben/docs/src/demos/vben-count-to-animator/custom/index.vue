<script lang="ts" setup>
import { VbenCountToAnimator } from '@vben/common-ui';
</script>
<template>
  <VbenCountToAnimator
    :duration="3000"
    :end-val="2000000"
    :start-val="1"
    prefix="$"
    separator="/"
  />
</template>
