<script setup lang="ts">
import type { FormRules } from 'element-plus';

import type { SimpleFlowNode } from '../../consts';

import { reactive, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import {
  ElDatePicker,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElSelect,
} from 'element-plus';

import {
  DELAY_TYPE,
  DelayTypeEnum,
  TIME_UNIT_TYPES,
  TimeUnitType,
} from '../../consts';
import { useNodeName, useWatchNode } from '../../helpers';
import { convertTimeUnit } from './utils';

defineOptions({ name: 'DelayTimerNodeConfig' });

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});

// 当前节点
const currentNode = useWatchNode(props);
// 节点名称
const { nodeName, showInput, clickIcon, changeNodeName, inputRef } =
  useNodeName(BpmNodeTypeEnum.DELAY_TIMER_NODE);
// 抄送人表单配置
const formRef = ref(); // 表单 Ref

// 表单校验规则
const formRules: FormRules = reactive({
  delayType: [
    { required: true, message: '延迟时间不能为空', trigger: 'change' },
  ],
  timeDuration: [
    { required: true, message: '延迟时间不能为空', trigger: 'change' },
  ],
  dateTime: [
    { required: true, message: '延迟时间不能为空', trigger: 'change' },
  ],
});

// 配置表单数据
const configForm = ref({
  delayType: DelayTypeEnum.FIXED_TIME_DURATION,
  timeDuration: 1,
  timeUnit: TimeUnitType.HOUR,
  dateTime: '',
});

// 获取显示文本
function getShowText(): string {
  let showText = '';
  if (configForm.value.delayType === DelayTypeEnum.FIXED_TIME_DURATION) {
    showText = `延迟${configForm.value.timeDuration}${TIME_UNIT_TYPES?.find((item) => item.value === configForm.value.timeUnit)?.label}`;
  }
  if (configForm.value.delayType === DelayTypeEnum.FIXED_DATE_TIME) {
    showText = `延迟至${configForm.value.dateTime.replace('T', ' ')}`;
  }
  return showText;
}

// 获取ISO时间格式
function getIsoTimeDuration() {
  let strTimeDuration = 'PT';
  if (configForm.value.timeUnit === TimeUnitType.MINUTE) {
    strTimeDuration += `${configForm.value.timeDuration}M`;
  }
  if (configForm.value.timeUnit === TimeUnitType.HOUR) {
    strTimeDuration += `${configForm.value.timeDuration}H`;
  }
  if (configForm.value.timeUnit === TimeUnitType.DAY) {
    strTimeDuration += `${configForm.value.timeDuration}D`;
  }
  return strTimeDuration;
}

// 保存配置
async function saveConfig() {
  if (!formRef.value) return false;
  const valid = await formRef.value.validate();
  if (!valid) return false;
  const showText = getShowText();
  if (!showText) return false;
  currentNode.value.name = nodeName.value!;
  currentNode.value.showText = showText;
  if (configForm.value.delayType === DelayTypeEnum.FIXED_TIME_DURATION) {
    currentNode.value.delaySetting = {
      delayType: configForm.value.delayType,
      delayTime: getIsoTimeDuration(),
    };
  }
  if (configForm.value.delayType === DelayTypeEnum.FIXED_DATE_TIME) {
    currentNode.value.delaySetting = {
      delayType: configForm.value.delayType,
      delayTime: configForm.value.dateTime,
    };
  }
  drawerApi.close();
  return true;
}

const [Drawer, drawerApi] = useVbenDrawer({
  title: nodeName.value,
  onConfirm: saveConfig,
});

// 显示延迟器节点配置，由父组件调用
function openDrawer(node: SimpleFlowNode) {
  nodeName.value = node.name;
  if (node.delaySetting) {
    configForm.value.delayType = node.delaySetting.delayType;
    // 固定时长
    if (configForm.value.delayType === DelayTypeEnum.FIXED_TIME_DURATION) {
      const strTimeDuration = node.delaySetting.delayTime;
      const parseTime = strTimeDuration.slice(2, -1);
      const parseTimeUnit = strTimeDuration.slice(-1);
      configForm.value.timeDuration = Number.parseInt(parseTime);
      configForm.value.timeUnit = convertTimeUnit(parseTimeUnit);
    }
    // 固定日期时间
    if (configForm.value.delayType === DelayTypeEnum.FIXED_DATE_TIME) {
      configForm.value.dateTime = node.delaySetting.delayTime;
    }
  }
  drawerApi.open();
}

defineExpose({ openDrawer }); // 暴露方法给父组件
</script>
<template>
  <Drawer class="w-1/3">
    <template #title>
      <div class="flex items-center">
        <ElInput
          v-if="showInput"
          ref="inputRef"
          type="text"
          class="mr-2 w-48"
          @blur="changeNodeName()"
          @keyup.enter="changeNodeName()"
          v-model="nodeName"
          :placeholder="nodeName"
        />
        <div
          v-else
          class="flex cursor-pointer items-center"
          @click="clickIcon()"
        >
          {{ nodeName }}
          <IconifyIcon class="ml-1" icon="lucide:edit-3" :size="16" />
        </div>
      </div>
    </template>

    <div>
      <ElForm
        ref="formRef"
        :model="configForm"
        :rules="formRules"
        label-position="top"
      >
        <ElFormItem label="延迟时间" prop="delayType">
          <ElRadioGroup v-model="configForm.delayType">
            <ElRadio
              v-for="item in DELAY_TYPE"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem
          v-if="configForm.delayType === DelayTypeEnum.FIXED_TIME_DURATION"
        >
          <div class="flex items-center gap-2">
            <ElInputNumber
              v-model="configForm.timeDuration"
              :min="1"
              controls-position="right"
              class="!w-48"
            />
            <ElSelect v-model="configForm.timeUnit" class="w-24">
              <ElOption
                v-for="item in TIME_UNIT_TYPES"
                :key="item.value"
                :value="item.value"
                :label="item.label"
              >
                {{ item.label }}
              </ElOption>
            </ElSelect>
            <span class="whitespace-nowrap">后进入下一节点</span>
          </div>
        </ElFormItem>
        <ElFormItem
          v-if="configForm.delayType === DelayTypeEnum.FIXED_DATE_TIME"
          prop="dateTime"
        >
          <div class="flex items-center gap-2">
            <ElDatePicker
              v-model="configForm.dateTime"
              type="datetime"
              placeholder="请选择日期和时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              class="flex-1"
            />
            <span class="whitespace-nowrap">后进入下一节点</span>
          </div>
        </ElFormItem>
      </ElForm>
    </div>
  </Drawer>
</template>
