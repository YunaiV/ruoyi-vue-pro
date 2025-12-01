<script setup lang="ts">
import { onBeforeUnmount, onMounted, provide, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { confirm, Page } from '@vben/common-ui';
import { AiModelTypeEnum, CommonStatusEnum } from '@vben/constants';
import { useTabs } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import { Button, Card, message } from 'ant-design-vue';

import { getModelSimpleList } from '#/api/ai/model/model';
import { createWorkflow, getWorkflow, updateWorkflow } from '#/api/ai/workflow';
import { createModel, deployModel, updateModel } from '#/api/bpm/model';

import BasicInfo from './modules/basic-info.vue';
import WorkflowDesign from './modules/workflow-design.vue';

defineOptions({ name: 'AiWorkflowCreate' });

const router = useRouter();

const route = useRoute();

const workflowId = ref<string>('');
const actionType = ref<string>('');

const basicInfoRef = ref<InstanceType<typeof BasicInfo>>(); // 基础信息组件引用
const workflowDesignRef = ref<InstanceType<typeof WorkflowDesign>>(); // 工作流设计组件引用

const currentStep = ref(-1); // 步骤控制。-1 用于，一开始全部不展示等当前页面数据初始化完成
const steps = [
  { title: '基本信息', validator: validateBasic },
  { title: '工作流设计', validator: validateWorkflow },
];

const formData: any = ref({
  id: undefined,
  name: '',
  code: '',
  remark: '',
  graph: '',
  status: CommonStatusEnum.ENABLE,
}); // 表单数据

const llmProvider = ref<any>([]);
const workflowData = ref<any>({});
provide('workflowData', workflowData);

/** 步骤校验函数 */
async function validateBasic() {
  await basicInfoRef.value?.validate();
}

/** 工作流设计校验 */
async function validateWorkflow() {
  await workflowDesignRef.value?.validate();
}

async function initData() {
  if (actionType.value === 'update' && workflowId.value) {
    formData.value = await getWorkflow(workflowId.value as any);
    workflowData.value = JSON.parse(formData.value.graph);
  }
  const models = await getModelSimpleList(AiModelTypeEnum.CHAT);
  llmProvider.value = {
    llm: () =>
      models.map(({ id, name }) => ({
        value: id,
        label: name,
      })),
    knowledge: () => [],
    internal: () => [],
  };

  // 设置当前步骤
  currentStep.value = 0;
}

/** 校验所有步骤数据是否完整 */
async function validateAllSteps() {
  // 基本信息校验
  try {
    await validateBasic();
  } catch {
    currentStep.value = 0;
    throw new Error('请完善基本信息');
  }

  // 表单设计校验
  try {
    await validateWorkflow();
  } catch {
    currentStep.value = 1;
    throw new Error('请完善工作流信息');
  }
  return true;
}

/** 保存操作 */
async function handleSave() {
  try {
    // 保存前校验所有步骤的数据
    await validateAllSteps();

    // 更新表单数据
    const data = {
      ...formData.value,
      graph: JSON.stringify(workflowData.value),
    };
    await (actionType.value === 'update'
      ? updateWorkflow(data)
      : createWorkflow(data));

    // 保存成功，提示并跳转到列表页
    message.success('保存成功');
    await tabs.closeCurrentTab();
    await router.push({ name: 'AiWorkflow' });
  } catch (error: any) {
    console.error('保存失败:', error);
    message.warning(error.message || '请完善所有步骤的必填信息');
  }
}

/** 发布操作 */
async function handleDeploy() {
  try {
    // 修改场景下直接发布，新增场景下需要先确认
    if (!formData.value.id) {
      await confirm('是否确认发布该流程？');
    }
    // 校验所有步骤
    await validateAllSteps();

    // 更新表单数据
    const modelData = {
      ...formData.value,
    };

    // 先保存所有数据
    if (formData.value.id) {
      await updateModel(modelData);
    } else {
      const result = await createModel(modelData);
      formData.value.id = result.id;
    }

    // 发布
    await deployModel(formData.value.id);
    message.success('发布成功');
    // 返回列表页
    await router.push({ name: 'AiWorkflow' });
  } catch (error: any) {
    console.error('发布失败:', error);
    message.warning(error.message || '发布失败');
  }
}

/** 步骤切换处理 */
async function handleStepClick(index: number) {
  try {
    if (index !== 0) {
      await validateBasic();
    }
    if (index !== 1) {
      await validateWorkflow();
    }

    // 切换步骤
    currentStep.value = index;
  } catch (error) {
    console.error('步骤切换失败:', error);
    message.warning('请先完善当前步骤必填信息');
  }
}

const tabs = useTabs();

/** 返回列表页 */
function handleBack() {
  // 关闭当前页签
  tabs.closeCurrentTab();
  // 跳转到列表页，使用路径， 目前后端的路由 name： 'name'+ menuId
  router.push({ path: '/ai/workflow' });
}

/** 初始化 */
onMounted(async () => {
  workflowId.value = route.params.id as string;
  actionType.value = route.params.type as string;
  await initData();
});

/** 添加组件卸载前的清理 */
onBeforeUnmount(() => {
  // 清理所有的引用
  basicInfoRef.value = undefined;
  workflowDesignRef.value = undefined;
});
</script>

<template>
  <Page auto-content-height>
    <div class="mx-auto">
      <!-- 头部导航栏 -->
      <div
        class="absolute inset-x-0 top-0 z-10 flex h-12 items-center border-b bg-card px-5"
      >
        <!-- 左侧标题 -->
        <div class="flex w-48 items-center overflow-hidden">
          <IconifyIcon
            icon="lucide:arrow-left"
            class="size-5 flex-shrink-0 cursor-pointer"
            @click="handleBack"
          />
          <span
            class="ml-2.5 truncate text-base"
            :title="formData.name || '创建AI 工作流'"
          >
            {{ formData.name || '创建AI 工作流' }}
          </span>
        </div>

        <!-- 步骤条 -->
        <div class="flex h-full flex-1 items-center justify-center">
          <div class="flex h-full w-96 items-center justify-between">
            <div
              v-for="(step, index) in steps"
              :key="index"
              class="relative mx-4 flex h-full cursor-pointer items-center"
              :class="[
                currentStep === index
                  ? 'border-b-2 border-solid border-blue-500 text-blue-500'
                  : 'text-gray-500',
              ]"
              @click="handleStepClick(index)"
            >
              <div
                class="mr-2 flex h-7 w-7 items-center justify-center rounded-full border-2 border-solid text-base"
                :class="[
                  currentStep === index
                    ? 'border-blue-500 bg-blue-500 text-white'
                    : 'border-gray-300 bg-white text-gray-500',
                ]"
              >
                {{ index + 1 }}
              </div>
              <span class="whitespace-nowrap text-base font-bold">
                {{ step.title }}
              </span>
            </div>
          </div>
        </div>

        <!-- 右侧按钮 -->
        <div class="flex w-48 items-center justify-end gap-2">
          <Button
            v-if="actionType === 'update'"
            type="primary"
            @click="handleDeploy"
          >
            发 布
          </Button>
          <Button type="primary" @click="handleSave">
            <span v-if="actionType === 'definition'">恢 复</span>
            <span v-else>保 存</span>
          </Button>
        </div>
      </div>
      <!-- 主体内容 -->
      <Card class="mb-4 p-4">
        <div class="mt-12">
          <!-- 第一步：基本信息 -->
          <div v-if="currentStep === 0" class="mx-auto w-4/6">
            <BasicInfo v-model="formData" ref="basicInfoRef" />
          </div>
          <!-- 第二步：表单设计  -->
          <WorkflowDesign
            v-if="currentStep === 1"
            v-model="formData"
            :provider="llmProvider"
            ref="workflowDesignRef"
          />
        </div>
      </Card>
    </div>
  </Page>
</template>
