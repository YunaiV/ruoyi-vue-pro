<script lang="ts" setup>
import { ref, watch } from 'vue';

import { ElButton, ElInput } from 'element-plus';

import AppLinkSelectDialog from './select-dialog.vue';

/** APP 链接输入框 */
defineOptions({ name: 'AppLinkInput' });

/** 定义属性 */
const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  }, // 当前选中的链接
});

const emit = defineEmits<{
  'update:modelValue': [link: string];
}>();

const dialogRef = ref(); // 选择对话框

const appLink = ref(''); // 当前的链接

/** 处理打开对话框 */
function handleOpenDialog() {
  return dialogRef.value?.open(appLink.value);
}

/** 处理 APP 链接选中 */
function handleLinkSelected(link: string) {
  appLink.value = link;
}

watch(
  () => props.modelValue,
  () => (appLink.value = props.modelValue),
  { immediate: true },
);

watch(
  () => appLink.value,
  () => emit('update:modelValue', appLink.value),
);
</script>
<template>
  <ElInput v-model="appLink" placeholder="输入或选择链接">
    <template #append>
      <ElButton @click="handleOpenDialog">选择</ElButton>
    </template>
  </ElInput>

  <AppLinkSelectDialog ref="dialogRef" @change="handleLinkSelected" />
</template>
