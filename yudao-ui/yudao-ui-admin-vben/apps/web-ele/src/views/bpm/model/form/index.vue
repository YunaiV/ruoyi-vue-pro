<script setup lang="ts">
import type { BpmCategoryApi } from '#/api/bpm/category';
import type { BpmProcessDefinitionApi } from '#/api/bpm/definition';
import type { BpmFormApi } from '#/api/bpm/form';
import type { SystemDeptApi } from '#/api/system/dept';
import type { SystemUserApi } from '#/api/system/user';

import { onBeforeUnmount, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { confirm, Page } from '@vben/common-ui';
import {
  BpmAutoApproveType,
  BpmModelFormType,
  BpmModelType,
} from '@vben/constants';
import { useTabs } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { useUserStore } from '@vben/stores';

import { ElButton, ElCard, ElMessage } from 'element-plus';

import { getCategorySimpleList } from '#/api/bpm/category';
import { getProcessDefinition } from '#/api/bpm/definition';
import { getFormSimpleList } from '#/api/bpm/form';
import {
  createModel,
  deployModel,
  getModel,
  updateModel,
} from '#/api/bpm/model';
import { getSimpleDeptList } from '#/api/system/dept';
import { getSimpleUserList } from '#/api/system/user';

import BasicInfo from './modules/basic-info.vue';
import ExtraSetting from './modules/extra-setting.vue';
import FormDesign from './modules/form-design.vue';
import ProcessDesign from './modules/process-design.vue';

defineOptions({ name: 'BpmModelCreate' });

type BpmProcessDefinitionType = Omit<
  BpmProcessDefinitionApi.ProcessDefinition,
  'modelId' | 'modelType'
> & {
  id?: string;
  type?: number;
};

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const tabs = useTabs();

const basicInfoRef = ref<InstanceType<typeof BasicInfo>>(); // 基础信息组件引用
const formDesignRef = ref<InstanceType<typeof FormDesign>>(); // 表单设计组件引用
const processDesignRef = ref<InstanceType<typeof ProcessDesign>>(); // 流程设计组件引用
const extraSettingRef = ref<InstanceType<typeof ExtraSetting>>(); // 更多设置组件引用

const actionType = route.params.type as string; // 操作类型：create、copy、update
const currentStep = ref(-1); // 步骤控制。-1 用于，一开始全部不展示等当前页面数据初始化完成
const steps = [
  { title: '基本信息', validator: validateBasic },
  { title: '表单设计', validator: validateForm },
  { title: '流程设计', validator: validateProcess },
  { title: '更多设置', validator: validateExtra },
];

const formData: any = ref({
  id: undefined,
  name: '',
  key: '',
  category: undefined,
  icon: undefined,
  description: '',
  type: BpmModelType.SIMPLE,
  formType: BpmModelFormType.NORMAL,
  formId: '',
  formCustomCreatePath: '',
  formCustomViewPath: '',
  visible: true,
  startUserType: undefined,
  startUserIds: [],
  startDeptIds: [],
  managerUserIds: [],
  allowCancelRunningProcess: true,
  processIdRule: {
    enable: false,
    prefix: '',
    infix: '',
    postfix: '',
    length: 5,
  },
  autoApprovalType: BpmAutoApproveType.NONE,
  titleSetting: {
    enable: false,
    title: '',
  },
  summarySetting: {
    enable: false,
    summary: [],
  },
  allowWithdrawTask: false,
}); // 表单数据
const processData = ref<any>(); // 流程数据

const formList = ref<BpmFormApi.Form[]>([]);
const categoryList = ref<BpmCategoryApi.Category[]>([]);
const userList = ref<SystemUserApi.User[]>([]);
const deptList = ref<SystemDeptApi.Dept[]>([]);

provide('processData', processData);
provide('modelData', formData);

/** 步骤校验函数 */
async function validateBasic() {
  await basicInfoRef.value?.validate();
}

/** 表单设计校验 */
async function validateForm() {
  await formDesignRef.value?.validate();
}

/** 流程设计校验 */
async function validateProcess() {
  await processDesignRef.value?.validate();
}

/** 更多设置校验 */
async function validateExtra() {
  await extraSettingRef.value?.validate();
}

/** 初始化数据 */
async function initData() {
  if (actionType === 'definition') {
    // 情况一：流程定义场景（恢复）
    const definitionId = route.params.id as string;
    const data = await getProcessDefinition(definitionId);
    const processDefinition: BpmProcessDefinitionType = data;
    // 将 definition => model
    processDefinition.type = data.modelType;
    processDefinition.id = data.modelId;
    if (data.simpleModel) {
      processDefinition.simpleModel = JSON.parse(data.simpleModel);
    }
    formData.value = processDefinition;

    // 设置 startUserType
    if (formData.value.startUserIds?.length > 0) {
      formData.value.startUserType = 1;
    } else if (formData.value.startDeptIds?.length > 0) {
      formData.value.startUserType = 2;
    } else {
      formData.value.startUserType = 0;
    }
  } else if (['copy', 'update'].includes(actionType)) {
    // 情况二：修改场景/复制场景
    const modelId = route.params.id as string;
    formData.value = await getModel(modelId);

    // 设置 startUserType
    if (formData.value.startUserIds?.length > 0) {
      formData.value.startUserType = 1;
    } else if (formData.value.startDeptIds?.length > 0) {
      formData.value.startUserType = 2;
    } else {
      formData.value.startUserType = 0;
    }

    // 特殊：复制场景
    if (route.params.type === 'copy') {
      delete formData.value.id;
      if (formData.value.bpmnXml) {
        formData.value.bpmnXml = formData.value.bpmnXml.replaceAll(
          formData.value.name,
          `${formData.value.name}副本`,
        );
        formData.value.bpmnXml = formData.value.bpmnXml.replaceAll(
          formData.value.key,
          `${formData.value.key}_copy`,
        );
      }
      formData.value.name += '副本';
      formData.value.key += '_copy';
    }
  } else {
    // 情况三：新增场景
    formData.value.startUserType = 0; // 全体
    formData.value.managerUserIds.push(userStore.userInfo?.id);
  }

  // 获取表单列表
  formList.value = await getFormSimpleList();
  categoryList.value = await getCategorySimpleList();
  // 获取用户列表
  userList.value = await getSimpleUserList();
  // 获取部门列表
  deptList.value = await getSimpleDeptList();

  // 最终，设置 currentStep 切换到第一步
  currentStep.value = 0;
  // 以前未配置更多设置的流程
  extraSettingRef.value?.initData();
}

/** 根据类型切换流程数据 */
watch(
  async () => formData.value.type,
  () => {
    if (formData.value.type === BpmModelType.BPMN) {
      processData.value = formData.value.bpmnXml;
    } else if (formData.value.type === BpmModelType.SIMPLE) {
      processData.value = formData.value.simpleModel;
    }
  },
  {
    immediate: true,
  },
);

/** 校验所有步骤数据是否完整 */
async function validateAllSteps() {
  // 基本信息校验
  try {
    await validateBasic();
  } catch {
    currentStep.value = 0;
    ElMessage.warning('请完善基本信息');
    return false;
  }

  // 表单设计校验
  try {
    await validateForm();
  } catch {
    currentStep.value = 1;
    ElMessage.warning('请完善自定义表单信息');
    return false;
  }

  // 流程设计校验
  try {
    await validateProcess();
  } catch {
    currentStep.value = 2;
    return false;
  }

  // 更多设置校验
  try {
    await validateExtra();
  } catch {
    currentStep.value = 3;
    return false;
  }

  return true;
}

/** 保存操作 */
async function handleSave() {
  try {
    // 保存前校验所有步骤的数据
    const result = await validateAllSteps();
    if (!result) {
      return;
    }

    // 更新表单数据
    const modelData = {
      ...formData.value,
    };

    switch (actionType) {
      case 'copy': {
        // 情况三：复制场景
        formData.value.id = await createModel(modelData);
        // 提示成功
        ElMessage.success('复制成功，可点击【发布】按钮，进行发布模型');
        break;
      }
      case 'definition': {
        // 情况一：流程定义场景（恢复）
        await updateModel(modelData);
        // 提示成功
        ElMessage.success('恢复成功，可点击【发布】按钮，进行发布模型');
        break;
      }
      case 'update': {
        // 情况二：修改场景
        await updateModel(modelData);
        // 提示成功
        ElMessage.success('修改成功，可点击【发布】按钮，进行发布模型');
        break;
      }
      default: {
        // 情况四：新增场景
        formData.value.id = await createModel(modelData);
        // 提示成功
        ElMessage.success('新建成功，可点击【发布】按钮，进行发布模型');
      }
    }

    // 返回列表页（排除更新的情况）
    if (actionType !== 'update') {
      await router.push({ name: 'BpmModel' });
    }
  } catch (error: any) {
    console.error('保存失败:', error);
  }
}

/** 发布操作 */
async function handleDeploy() {
  try {
    // 1.1 修改场景下直接发布，新增场景下需要先确认
    if (!formData.value.id) {
      await confirm('是否确认发布该流程？');
    }
    // 1.2 校验所有步骤
    await validateAllSteps();

    // 2.1 更新表单数据
    const modelData = {
      ...formData.value,
    };
    // 2.2 先保存所有数据
    if (formData.value.id) {
      await updateModel(modelData);
    } else {
      const result = await createModel(modelData);
      formData.value.id = result.id;
    }
    // 2.3 发布
    await deployModel(formData.value.id);

    // 3. 路由并提示
    ElMessage.success('发布成功');
    await router.push({ name: 'BpmModel' });
  } catch (error: any) {
    console.error('发布失败:', error);
    ElMessage.warning(error.message || '发布失败');
  }
}

/** 步骤切换处理 */
async function handleStepClick(index: number) {
  try {
    if (index !== 0) {
      await validateBasic();
    }
    if (index !== 1) {
      await validateForm();
    }
    if (index !== 2) {
      await validateProcess();
    }
    if (index !== 3) {
      await validateExtra();
    }
    // 切换步骤
    currentStep.value = index;
  } catch (error) {
    console.error('步骤切换失败:', error);
    if (currentStep.value !== 2) {
      ElMessage.warning('请先完善当前步骤必填信息');
    }
  }
}

/** 返回列表页 */
function handleBack() {
  tabs.closeCurrentTab();
  router.push({ name: 'BpmModel' });
}

/** 初始化 */
onMounted(async () => {
  await initData();
});

/** 添加组件卸载前的清理 */
onBeforeUnmount(() => {
  // 清理所有的引用
  basicInfoRef.value = undefined;
  formDesignRef.value = undefined;
  processDesignRef.value = undefined;
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
            :title="formData.name || '创建流程'"
          >
            {{ formData.name || '创建流程' }}
          </span>
        </div>

        <!-- 步骤条 -->
        <div class="flex h-full flex-1 items-center justify-center">
          <div class="flex h-full w-auto items-center justify-center">
            <div
              v-for="(step, index) in steps"
              :key="index"
              class="relative mx-6 flex h-full cursor-pointer items-center"
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
          <ElButton
            v-if="actionType === 'update'"
            type="primary"
            @click="handleDeploy"
          >
            发 布
          </ElButton>
          <ElButton type="primary" @click="handleSave">
            <span v-if="actionType === 'definition'">恢 复</span>
            <span v-else>保 存</span>
          </ElButton>
        </div>
      </div>
      <!-- 主体内容 -->
      <ElCard body-style="padding: 10px" class="mb-4">
        <div class="mt-12">
          <!-- 第一步：基本信息 -->
          <div v-if="currentStep === 0" class="mx-auto w-4/6">
            <BasicInfo
              v-model="formData"
              :category-list="categoryList"
              :user-list="userList"
              :dept-list="deptList"
              ref="basicInfoRef"
            />
          </div>
          <!-- 第二步：表单设计  -->
          <div v-if="currentStep === 1" class="mx-auto w-4/6">
            <FormDesign
              v-model="formData"
              :form-list="formList"
              ref="formDesignRef"
            />
          </div>

          <!-- 第三步：流程设计 -->
          <ProcessDesign
            v-if="currentStep === 2"
            v-model="formData"
            ref="processDesignRef"
          />

          <!-- 第四步：更多设置 -->
          <div v-if="currentStep === 3" class="mx-auto w-4/6">
            <ExtraSetting v-model="formData" ref="extraSettingRef" />
          </div>
        </div>
      </ElCard>
    </div>
  </Page>
</template>
