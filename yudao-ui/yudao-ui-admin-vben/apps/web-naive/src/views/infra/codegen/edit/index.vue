<script lang="ts" setup>
import type { InfraCodegenApi } from '#/api/infra/codegen';

import { ref, unref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { NButton, NStep, NSteps } from 'naive-ui';

import { message } from '#/adapter/naive';
import { getCodegenTable, updateCodegenTable } from '#/api/infra/codegen';
import { $t } from '#/locales';

import BasicInfo from '../modules/basic-info.vue';
import ColumnInfo from '../modules/column-info.vue';
import GenerationInfo from '../modules/generation-info.vue';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const currentStep = ref(1);
const formData = ref<InfraCodegenApi.CodegenDetail>({
  table: {} as InfraCodegenApi.CodegenTable,
  columns: [],
});

/** 表单引用 */
const basicInfoRef = ref<InstanceType<typeof BasicInfo>>();
const columnInfoRef = ref<InstanceType<typeof ColumnInfo>>();
const generateInfoRef = ref<InstanceType<typeof GenerationInfo>>();

/** 获取详情数据 */
async function getDetail() {
  const id = route.query.id as any;
  if (!id) {
    return;
  }
  loading.value = true;
  try {
    formData.value = await getCodegenTable(id);
  } finally {
    loading.value = false;
  }
}

/** 提交表单 */
async function submitForm() {
  // 表单验证
  const basicInfoValid = await basicInfoRef.value?.validate();
  if (!basicInfoValid) {
    message.warning('保存失败，原因：基本信息表单校验失败请检查！！！');
    return;
  }
  const generateInfoValid = await generateInfoRef.value?.validate();
  if (!generateInfoValid) {
    message.warning('保存失败，原因：生成信息表单校验失败请检查！！！');
    return;
  }

  // 提交表单
  const hideLoading = message.loading($t('ui.actionMessage.updating'), {
    duration: 0,
  });
  try {
    // 拼接相关信息
    const basicInfo = await basicInfoRef.value?.getValues();
    const columns = columnInfoRef.value?.getData() || unref(formData).columns;
    const generateInfo = await generateInfoRef.value?.getValues();
    await updateCodegenTable({
      table: { ...unref(formData).table, ...basicInfo, ...generateInfo },
      columns,
    });
    // 关闭并提示
    message.success($t('ui.actionMessage.operationSuccess'));
    close();
  } catch (error) {
    console.error('保存失败', error);
  } finally {
    hideLoading.destroy();
  }
}

/** 返回列表 */
const tabs = useTabs();
function close() {
  tabs.closeCurrentTab();
  router.push({ name: 'InfraCodegen' });
}

/** 下一步 */
function nextStep() {
  currentStep.value += 1;
}

/** 上一步 */
function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value -= 1;
  }
}

/** 步骤配置 */
const steps = [
  {
    title: '基本信息',
    key: 1,
  },
  {
    title: '字段信息',
    key: 2,
  },
  {
    title: '生成信息',
    key: 3,
  },
];

// 初始化
getDetail();
</script>

<template>
  <Page auto-content-height v-loading="loading">
    <div class="flex h-[95%] flex-col rounded-md bg-card p-4">
      <NSteps :current="currentStep" class="mb-8 rounded shadow-sm">
        <NStep v-for="step in steps" :key="step.key" :title="step.title" />
      </NSteps>

      <div class="flex-1 overflow-auto py-4">
        <!-- 根据当前步骤显示对应的组件 -->
        <BasicInfo
          v-show="currentStep === 1"
          ref="basicInfoRef"
          :table="formData.table"
        />
        <ColumnInfo
          v-show="currentStep === 2"
          ref="columnInfoRef"
          :columns="formData.columns"
        />
        <GenerationInfo
          v-show="currentStep === 3"
          ref="generateInfoRef"
          :table="formData.table"
          :columns="formData.columns"
        />
      </div>

      <div class="mt-4 flex justify-end space-x-2">
        <NButton :disabled="currentStep === 1" @click="prevStep">
          上一步
        </NButton>
        <NButton :disabled="currentStep === steps.length" @click="nextStep">
          下一步
        </NButton>
        <NButton type="primary" :loading="loading" @click="submitForm">
          保存
        </NButton>
      </div>
    </div>
  </Page>
</template>
