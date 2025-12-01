<script lang="ts" setup>
import { ref, watch } from 'vue';

import { Button, Input } from 'ant-design-vue';

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
  <Input v-model:value="appLink" placeholder="输入或选择链接">
    <template #addonAfter>
      <Button
        @click="handleOpenDialog"
        class="!border-none !bg-transparent !p-0"
      >
        选择
      </Button>
    </template>
  </Input>

  <AppLinkSelectDialog ref="dialogRef" @change="handleLinkSelected" />
</template>
