<script lang="ts" setup>
import type { Ref } from 'vue';

import { computed, nextTick, onMounted, ref, toRaw, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Button, DatePicker, Input, Modal, Tooltip } from 'ant-design-vue';

import CycleConfig from './CycleConfig.vue';
import DurationConfig from './DurationConfig.vue';

const props = defineProps({
  businessObject: {
    type: Object,
    default: () => ({}),
  },
});

const bpmnInstances = () => (window as any).bpmnInstances;
const type: Ref<string> = ref('time');
const condition: Ref<string> = ref('');
const valid: Ref<boolean> = ref(true);
const showDatePicker: Ref<boolean> = ref(false);
const showDurationDialog: Ref<boolean> = ref(false);
const showCycleDialog: Ref<boolean> = ref(false);
const showHelp: Ref<boolean> = ref(false);
const dateValue: Ref<Date | null> = ref(null);
// const bpmnElement = ref(null);

const placeholder = computed<string>(() => {
  if (type.value === 'time') return '请输入时间';
  if (type.value === 'duration') return '请输入持续时长';
  if (type.value === 'cycle') return '请输入循环表达式';
  return '';
});
const helpText = computed<string>(() => {
  if (type.value === 'time') return '选择具体时间';
  if (type.value === 'duration') return 'ISO 8601格式，如PT1H';
  if (type.value === 'cycle') return 'CRON表达式或ISO 8601周期';
  return '';
});
const helpHtml = computed<string>(() => {
  if (type.value === 'duration') {
    return `指定定时器之前要等待多长时间。S表示秒，M表示分，D表示天；P表示时间段，T表示精确到时间的时间段。<br>
    时间格式依然为ISO 8601格式，一年两个月三天四小时五分六秒内，可以写成P1Y2M3DT4H5M6S。<br>
    P是开始标记，T是时间和日期分割标记，没有日期只有时间T是不能省去的，比如1小时执行一次应写成PT1H。`;
  }
  if (type.value === 'cycle') {
    return `支持CRON表达式（如0 0/30 * * * ?）或ISO 8601周期（如R3/PT10M）。`;
  }
  return '';
});

// 初始化和监听
function syncFromBusinessObject(): void {
  if (props.businessObject) {
    const timerDef = (props.businessObject.eventDefinitions || [])[0];
    if (timerDef) {
      if (timerDef.timeDate) {
        type.value = 'time';
        condition.value = timerDef.timeDate.body;
      } else if (timerDef.timeDuration) {
        type.value = 'duration';
        condition.value = timerDef.timeDuration.body;
      } else if (timerDef.timeCycle) {
        type.value = 'cycle';
        condition.value = timerDef.timeCycle.body;
      }
    }
  }
}
onMounted(syncFromBusinessObject);

// 切换类型
function setType(t: string) {
  type.value = t;
  condition.value = '';
  updateNode();
}

// 输入校验
watch([type, condition], () => {
  valid.value = validate();
  // updateNode() // 可以注释掉，避免频繁触发
});

function validate(): boolean {
  if (type.value === 'time') {
    return !!condition.value && !Number.isNaN(Date.parse(condition.value));
  }
  if (type.value === 'duration') {
    return /^P.*$/.test(condition.value);
  }
  if (type.value === 'cycle') {
    return /^(?:[0-9*/?, ]+|R\d*\/P.*)$/.test(condition.value);
  }
  return true;
}

// 选择时间
function onDateChange(val: any) {
  dateValue.value = val;
}
function onDateConfirm(): void {
  if (dateValue.value) {
    condition.value = new Date(dateValue.value).toISOString();
    showDatePicker.value = false;
    updateNode();
  }
}

// 持续时长
function onDurationChange(val: string) {
  condition.value = val;
}
function onDurationConfirm(): void {
  showDurationDialog.value = false;
  updateNode();
}

// 循环
function onCycleChange(val: string) {
  condition.value = val;
}
function onCycleConfirm(): void {
  showCycleDialog.value = false;
  updateNode();
}

// 输入框聚焦时弹窗（可选）
function handleInputFocus(): void {
  if (type.value === 'time') showDatePicker.value = true;
  if (type.value === 'duration') showDurationDialog.value = true;
  if (type.value === 'cycle') showCycleDialog.value = true;
}

// 同步到节点
function updateNode(): void {
  const moddle = (window.bpmnInstances as any)?.moddle;
  const modeling = (window.bpmnInstances as any)?.modeling;
  const elementRegistry = (window.bpmnInstances as any)?.elementRegistry;
  if (!moddle || !modeling || !elementRegistry) return;

  // 获取元素
  if (!props.businessObject || !props.businessObject.id) return;
  const element = elementRegistry.get(props.businessObject.id);
  if (!element) return;

  // 1. 复用原有 timerDef，或新建
  let timerDef =
    element.businessObject.eventDefinitions &&
    element.businessObject.eventDefinitions[0];
  if (!timerDef) {
    timerDef = bpmnInstances().bpmnFactory.create(
      'bpmn:TimerEventDefinition',
      {},
    );
    modeling.updateProperties(element, {
      eventDefinitions: [timerDef],
    });
  }

  // 2. 清空原有
  delete timerDef.timeDate;
  delete timerDef.timeDuration;
  delete timerDef.timeCycle;

  // 3. 设置新的
  if (type.value === 'time' && condition.value) {
    timerDef.timeDate = bpmnInstances().bpmnFactory.create(
      'bpmn:FormalExpression',
      {
        body: condition.value,
      },
    );
  } else if (type.value === 'duration' && condition.value) {
    timerDef.timeDuration = bpmnInstances().bpmnFactory.create(
      'bpmn:FormalExpression',
      {
        body: condition.value,
      },
    );
  } else if (type.value === 'cycle' && condition.value) {
    timerDef.timeCycle = bpmnInstances().bpmnFactory.create(
      'bpmn:FormalExpression',
      {
        body: condition.value,
      },
    );
  }

  bpmnInstances().modeling.updateProperties(toRaw(element), {
    eventDefinitions: [timerDef],
  });
}

watch(
  () => props.businessObject,
  (val) => {
    if (val) {
      nextTick(() => {
        syncFromBusinessObject();
      });
    }
  },
  { immediate: true },
);
</script>

<template>
  <div class="panel-tab__content">
    <div style="margin-top: 10px">
      <span>类型：</span>
      <Button.Group>
        <Button
          size="small"
          :type="type === 'time' ? 'primary' : 'default'"
          @click="setType('time')"
        >
          时间
        </Button>
        <Button
          size="small"
          :type="type === 'duration' ? 'primary' : 'default'"
          @click="setType('duration')"
        >
          持续
        </Button>
        <Button
          size="small"
          :type="type === 'cycle' ? 'primary' : 'default'"
          @click="setType('cycle')"
        >
          循环
        </Button>
      </Button.Group>
      <IconifyIcon
        icon="ant-design:check-circle-filled"
        v-if="valid"
        style=" margin-left: 8px;color: green"
      />
    </div>
    <div style="display: flex; align-items: center; margin-top: 10px">
      <span>条件：</span>
      <Input
        v-model:value="condition"
        :placeholder="placeholder"
        style="width: calc(100% - 100px)"
        :readonly="type !== 'duration' && type !== 'cycle'"
        @focus="handleInputFocus"
        @blur="updateNode"
      >
        <template #suffix>
          <Tooltip v-if="!valid" title="格式错误" placement="top">
            <IconifyIcon
              icon="ant-design:exclamation-circle-filled"
              class="text-orange-400"
            />
          </Tooltip>
          <Tooltip :title="helpText" placement="top">
            <IconifyIcon
              icon="ant-design:question-circle-filled"
              class="cursor-pointer text-[#409eff]"
              @click="showHelp = true"
            />
          </Tooltip>
          <Button
            v-if="type === 'time'"
            @click="showDatePicker = true"
            style="margin-left: 4px"
            shape="circle"
            size="small"
          >
            <IconifyIcon icon="ep:calendar" />
          </Button>
          <Button
            v-if="type === 'duration'"
            @click="showDurationDialog = true"
            style="margin-left: 4px"
            shape="circle"
            size="small"
          >
            <IconifyIcon icon="ep:timer" />
          </Button>
          <Button
            v-if="type === 'cycle'"
            @click="showCycleDialog = true"
            style="margin-left: 4px"
            shape="circle"
            size="small"
          >
            <IconifyIcon icon="ep:setting" />
          </Button>
        </template>
      </Input>
    </div>
    <!-- 时间选择器 -->
    <Modal
      v-model:open="showDatePicker"
      title="选择时间"
      width="400px"
      @cancel="showDatePicker = false"
    >
      <DatePicker
        v-model:value="dateValue"
        show-time
        placeholder="选择日期时间"
        style="width: 100%"
        @change="onDateChange"
      />
      <template #footer>
        <Button @click="showDatePicker = false">取消</Button>
        <Button type="primary" @click="onDateConfirm">确定</Button>
      </template>
    </Modal>
    <!-- 持续时长选择器 -->
    <Modal
      v-model:open="showDurationDialog"
      title="时间配置"
      width="600px"
      @cancel="showDurationDialog = false"
    >
      <DurationConfig :value="condition" @change="onDurationChange" />
      <template #footer>
        <Button @click="showDurationDialog = false">取消</Button>
        <Button type="primary" @click="onDurationConfirm">确定</Button>
      </template>
    </Modal>
    <!-- 循环配置器 -->
    <Modal
      v-model:open="showCycleDialog"
      title="时间配置"
      width="800px"
      @cancel="showCycleDialog = false"
    >
      <CycleConfig :value="condition" @change="onCycleChange" />
      <template #footer>
        <Button @click="showCycleDialog = false">取消</Button>
        <Button type="primary" @click="onCycleConfirm">确定</Button>
      </template>
    </Modal>
    <!-- 帮助说明 -->
    <Modal
      v-model:open="showHelp"
      title="格式说明"
      width="600px"
      @cancel="showHelp = false"
    >
      <div v-html="helpHtml"></div>
      <template #footer>
        <Button @click="showHelp = false">关闭</Button>
      </template>
    </Modal>
  </div>
</template>
