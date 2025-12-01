<script setup lang="ts">
import type { IotSceneRule } from '#/api/iot/rule/scene';

import { computed, nextTick, reactive, ref, watch } from 'vue';

import { CommonStatusEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import { Button, Drawer, Form, message } from 'ant-design-vue';

import { createSceneRule, updateSceneRule } from '#/api/iot/rule/scene';
import {
  IotRuleSceneActionTypeEnum,
  IotRuleSceneTriggerTypeEnum,
  isDeviceTrigger,
} from '#/views/iot/utils/constants';

import ActionSection from './sections/action-section.vue';
import BasicInfoSection from './sections/basic-info-section.vue';
import TriggerSection from './sections/trigger-section.vue';

/** IoT 场景联动规则表单 - 主表单组件 */
defineOptions({ name: 'RuleSceneForm' });

/** 组件属性定义 */
const props = defineProps<{
  /** 抽屉显示状态 */
  modelValue: boolean;
  /** 编辑的场景联动规则数据 */
  ruleScene?: IotSceneRule;
}>();

/** 组件事件定义 */
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'success'): void;
}>();

const drawerVisible = useVModel(props, 'modelValue', emit); // 抽屉显示状态

/**
 * 创建默认的表单数据
 * @returns 默认表单数据对象
 */
function createDefaultFormData(): IotSceneRule {
  return {
    name: '',
    description: '',
    status: CommonStatusEnum.ENABLE, // 默认启用状态
    triggers: [
      {
        type: IotRuleSceneTriggerTypeEnum.DEVICE_PROPERTY_POST.toString(),
        productId: undefined,
        deviceId: undefined,
        identifier: undefined,
        operator: undefined,
        value: undefined,
        cronExpression: undefined,
        conditionGroups: [], // 空的条件组数组
      },
    ],
    actions: [],
  };
}

const formRef = ref(); // 表单引用
const formData = ref<IotSceneRule>(createDefaultFormData()); // 表单数据

/**
 * 触发器校验器
 * @param _rule 校验规则（未使用）
 * @param value 校验值
 * @param callback 回调函数
 */
function validateTriggers(_rule: any, value: any, callback: any) {
  if (!value || !Array.isArray(value) || value.length === 0) {
    callback(new Error('至少需要一个触发器'));
    return;
  }

  for (const [i, trigger] of value.entries()) {
    // 校验触发器类型
    if (!trigger.type) {
      callback(new Error(`触发器 ${i + 1}: 触发器类型不能为空`));
      return;
    }

    // 校验设备触发器
    if (isDeviceTrigger(trigger.type)) {
      if (!trigger.productId) {
        callback(new Error(`触发器 ${i + 1}: 产品不能为空`));
        return;
      }
      if (!trigger.deviceId) {
        callback(new Error(`触发器 ${i + 1}: 设备不能为空`));
        return;
      }
      if (!trigger.identifier) {
        callback(new Error(`触发器 ${i + 1}: 物模型标识符不能为空`));
        return;
      }
      if (!trigger.operator) {
        callback(new Error(`触发器 ${i + 1}: 操作符不能为空`));
        return;
      }
      if (
        trigger.value === undefined ||
        trigger.value === null ||
        trigger.value === ''
      ) {
        callback(new Error(`触发器 ${i + 1}: 参数值不能为空`));
        return;
      }
    }

    // 校验定时触发器
    if (
      trigger.type === IotRuleSceneTriggerTypeEnum.TIMER &&
      !trigger.cronExpression
    ) {
      callback(new Error(`触发器 ${i + 1}: CRON表达式不能为空`));
      return;
    }
  }

  callback();
}

/**
 * 执行器校验器
 * @param _rule 校验规则（未使用）
 * @param value 校验值
 * @param callback 回调函数
 */
function validateActions(_rule: any, value: any, callback: any) {
  if (!value || !Array.isArray(value) || value.length === 0) {
    callback(new Error('至少需要一个执行器'));
    return;
  }

  for (const [i, action] of value.entries()) {
    // 校验执行器类型
    if (!action.type) {
      callback(new Error(`执行器 ${i + 1}: 执行器类型不能为空`));
      return;
    }

    // 校验设备控制执行器
    if (
      action.type === IotRuleSceneActionTypeEnum.DEVICE_PROPERTY_SET ||
      action.type === IotRuleSceneActionTypeEnum.DEVICE_SERVICE_INVOKE
    ) {
      if (!action.productId) {
        callback(new Error(`执行器 ${i + 1}: 产品不能为空`));
        return;
      }
      if (!action.deviceId) {
        callback(new Error(`执行器 ${i + 1}: 设备不能为空`));
        return;
      }

      // 服务调用需要验证服务标识符
      if (
        action.type === IotRuleSceneActionTypeEnum.DEVICE_SERVICE_INVOKE &&
        !action.identifier
      ) {
        callback(new Error(`执行器 ${i + 1}: 服务不能为空`));
        return;
      }

      if (!action.params || Object.keys(action.params).length === 0) {
        callback(new Error(`执行器 ${i + 1}: 参数配置不能为空`));
        return;
      }
    }

    // 校验告警执行器
    if (
      (action.type === IotRuleSceneActionTypeEnum.ALERT_TRIGGER ||
        action.type === IotRuleSceneActionTypeEnum.ALERT_RECOVER) &&
      !action.alertConfigId
    ) {
      callback(new Error(`执行器 ${i + 1}: 告警配置不能为空`));
      return;
    }
  }

  callback();
}

const formRules = reactive({
  name: [
    { required: true, message: '场景名称不能为空', trigger: 'blur' },
    {
      type: 'string',
      min: 1,
      max: 50,
      message: '场景名称长度应在1-50个字符之间',
      trigger: 'blur',
    },
  ],
  status: [
    { required: true, message: '场景状态不能为空', trigger: 'change' },
    {
      type: 'enum',
      enum: [CommonStatusEnum.ENABLE, CommonStatusEnum.DISABLE],
      message: '状态值必须为启用或禁用',
      trigger: 'change',
    },
  ],
  description: [
    {
      type: 'string',
      max: 200,
      message: '场景描述不能超过200个字符',
      trigger: 'blur',
    },
  ],
  triggers: [
    { required: true, validator: validateTriggers, trigger: 'change' },
  ],
  actions: [{ required: true, validator: validateActions, trigger: 'change' }],
}); // 表单校验规则

const submitLoading = ref(false); // 提交加载状态
const isEdit = ref(false); // 是否为编辑模式
const drawerTitle = computed(() =>
  isEdit.value ? '编辑场景联动规则' : '新增场景联动规则',
); // 抽屉标题

/** 提交表单 */
async function handleSubmit() {
  // 校验表单
  if (!formRef.value) return;
  const valid = await formRef.value.validate();
  if (!valid) {
    return;
  }

  // 提交请求
  submitLoading.value = true;
  try {
    if (isEdit.value) {
      // 更新场景联动规则
      await updateSceneRule(formData.value);
      message.success('更新成功');
    } else {
      // 创建场景联动规则
      await createSceneRule(formData.value);
      message.success('创建成功');
    }

    // 关闭抽屉并触发成功事件
    drawerVisible.value = false;
    emit('success');
  } catch (error) {
    console.error('保存失败:', error);
    message.error(isEdit.value ? '更新失败' : '创建失败');
  } finally {
    submitLoading.value = false;
  }
}

/** 处理抽屉关闭事件 */
const handleClose = () => {
  drawerVisible.value = false;
};

/** 初始化表单数据 */
function initFormData() {
  if (props.ruleScene) {
    // 编辑模式：数据结构已对齐，直接使用后端数据
    isEdit.value = true;
    formData.value = {
      ...props.ruleScene,
      // 确保触发器数组不为空
      triggers: (props.ruleScene.triggers?.length as any)
        ? props.ruleScene.triggers
        : [
            {
              type: IotRuleSceneTriggerTypeEnum.DEVICE_PROPERTY_POST,
              productId: undefined,
              deviceId: undefined,
              identifier: undefined,
              operator: undefined,
              value: undefined,
              cronExpression: undefined,
              conditionGroups: [],
            },
          ],
      // 确保执行器数组不为空
      actions: props.ruleScene.actions || [],
    };
  } else {
    // 新增模式：使用默认数据
    isEdit.value = false;
    formData.value = createDefaultFormData();
  }
}

/** 监听抽屉显示 */
watch(drawerVisible, async (visible) => {
  if (visible) {
    initFormData();
    // 重置表单验证状态
    await nextTick();
    formRef.value?.clearValidate();
  }
});

/** 监听编辑数据变化 */
watch(
  () => props.ruleScene,
  () => {
    if (drawerVisible.value) {
      initFormData();
    }
  },
  { deep: true },
);
</script>

<template>
  <Drawer
    v-model="drawerVisible"
    :title="drawerTitle"
    width="80%"
    direction="rtl"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @close="handleClose"
  >
    <Form
      ref="formRef"
      :model="formData"
      :rules="formRules as any"
      label-width="110px"
    >
      <!-- 基础信息配置 -->
      <BasicInfoSection v-model="formData" :rules="formRules" />
      <!-- 触发器配置 -->
      <TriggerSection v-model:triggers="formData.triggers as any" />
      <!-- 执行器配置 -->
      <ActionSection v-model:actions="formData.actions as any" />
    </Form>
    <template #footer>
      <div class="drawer-footer">
        <Button :disabled="submitLoading" type="primary" @click="handleSubmit">
          <IconifyIcon icon="ep:check" />
          确 定
        </Button>
        <Button @click="handleClose">
          <IconifyIcon icon="ep:close" />
          取 消
        </Button>
      </div>
    </template>
  </Drawer>
</template>
