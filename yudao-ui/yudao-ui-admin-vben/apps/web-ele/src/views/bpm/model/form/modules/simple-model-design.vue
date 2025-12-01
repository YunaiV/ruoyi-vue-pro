<script setup lang="ts">
import { ref } from 'vue';

import { ContentWrap } from '@vben/common-ui';

import { SimpleProcessDesigner } from '#/views/bpm/components/simple-process-design';

defineOptions({ name: 'SimpleModelDesign' });

defineProps<{
  modelFormId?: number;
  modelFormType?: number;
  modelName?: string;
  startDeptIds?: number[];
  startUserIds?: number[];
}>();

const emit = defineEmits(['success']);

const designerRef = ref();

/** 保存成功回调 */
function handleSuccess(data?: any) {
  if (data) {
    emit('success', data);
  }
}

/** 设计器配置校验 */
async function validateConfig() {
  return await designerRef.value.validate();
}

defineExpose({ validateConfig });
</script>
<template>
  <ContentWrap class="px-4 py-5">
    <SimpleProcessDesigner
      :model-form-id="modelFormId"
      :model-name="modelName"
      :model-form-type="modelFormType"
      @success="handleSuccess"
      :start-user-ids="startUserIds"
      :start-dept-ids="startDeptIds"
      ref="designerRef"
    />
  </ContentWrap>
</template>
