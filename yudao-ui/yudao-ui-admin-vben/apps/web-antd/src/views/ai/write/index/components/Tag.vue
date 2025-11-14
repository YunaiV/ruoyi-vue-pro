<!-- 标签选项 -->
<script setup lang="ts">
// TODO @gjd：应该是 modules 模块，然后小写
const props = withDefaults(
  defineProps<{
    [k: string]: any;
    modelValue: string;
    tags?: { label: string; value: string }[];
  }>(),
  {
    tags: () => [],
  },
);

const emits = defineEmits<{
  (e: 'update:modelValue', value: string): void;
}>();
</script>

<template>
  <div class="flex flex-wrap gap-2">
    <span
      v-for="tag in props.tags"
      :key="tag.value"
      class="bg-card border-card-100 mb-2 cursor-pointer rounded border-2 border-solid px-1 text-xs leading-6"
      :class="
        modelValue === tag.value && '!border-primary-500 !text-primary-500'
      "
      @click="emits('update:modelValue', tag.value)"
    >
      {{ tag.label }}
    </span>
  </div>
</template>
