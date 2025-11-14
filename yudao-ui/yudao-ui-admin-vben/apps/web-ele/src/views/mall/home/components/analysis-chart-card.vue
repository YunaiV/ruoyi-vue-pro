<script setup lang="ts">
interface Props {
  title: string;
}

defineOptions({
  name: 'AnalysisChartCard',
});

withDefaults(defineProps<Props>(), {});
</script>

<template>
  <el-card>
    <template #header>
      <div class="my--1.5 flex flex-row items-center justify-between">
        <div class="text-xl">{{ title }}</div>
        <slot name="header-suffix"></slot>
      </div>
    </template>
    <template #default>
      <slot></slot>
    </template>
  </el-card>
</template>
