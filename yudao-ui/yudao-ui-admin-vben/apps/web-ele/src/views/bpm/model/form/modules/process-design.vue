<script lang="ts" setup>
import type { Ref } from 'vue';

import { computed, inject, nextTick, ref } from 'vue';

import { BpmModelType } from '@vben/constants';

import BpmModelEditor from './bpm-model-editor.vue';
import SimpleModelDesign from './simple-model-design.vue';

const modelData = defineModel<any>(); // 创建本地数据副本
const processData = inject('processData') as Ref;

const simpleDesign = ref();

/** 表单校验 */
async function validate() {
  // 获取最新的流程数据
  if (!processData.value) {
    throw new Error('请设计流程');
  }
  if (modelData.value.type === BpmModelType.SIMPLE) {
    // 简易设计器校验
    const validateResult = await simpleDesign.value?.validateConfig();
    if (!validateResult) {
      throw new Error('请完善设计配置');
    }
  }
  return true;
}

/** 处理设计器保存成功 */
async function handleDesignSuccess(data?: any) {
  if (data) {
    // 创建新的对象以触发响应式更新
    const newModelData = {
      ...modelData.value,
      bpmnXml: modelData.value.type === BpmModelType.BPMN ? data : null,
      simpleModel: modelData.value.type === BpmModelType.BPMN ? null : data,
    };
    // 使用 emit 更新父组件的数据
    await nextTick();
    // 更新表单的模型数据部分
    modelData.value = newModelData;
  }
}

/** 是否显示设计器 */
const showDesigner = computed(() => {
  return Boolean(modelData.value?.key && modelData.value?.name);
});

defineExpose({ validate });
</script>
<template>
  <div class="h-full">
    <!-- BPMN设计器 -->
    <template v-if="modelData.type === BpmModelType.BPMN">
      <BpmModelEditor
        v-if="showDesigner"
        :model-id="modelData.id"
        :model-key="modelData.key"
        :model-name="modelData.name"
        @success="handleDesignSuccess"
      />
    </template>
    <!-- Simple设计器 -->
    <template v-else>
      <SimpleModelDesign
        v-if="showDesigner"
        :model-name="modelData.name"
        :model-form-id="modelData.formId"
        :model-form-type="modelData.formType"
        :start-user-ids="modelData.startUserIds"
        :start-dept-ids="modelData.startDeptIds"
        @success="handleDesignSuccess"
        ref="simpleDesign"
      />
    </template>
  </div>
</template>
