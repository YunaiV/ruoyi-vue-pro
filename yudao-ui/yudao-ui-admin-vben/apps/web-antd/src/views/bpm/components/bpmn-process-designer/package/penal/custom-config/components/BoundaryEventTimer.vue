<script lang="ts" setup>
import { inject, nextTick, ref, toRaw, watch } from 'vue';

import {
  Divider,
  FormItem,
  InputNumber,
  RadioButton,
  RadioGroup,
  Select,
  SelectOption,
  Switch,
} from 'ant-design-vue';

import { convertTimeUnit } from '#/views/bpm/components/simple-process-design/components/nodes-config/utils';
import {
  TIME_UNIT_TYPES,
  TIMEOUT_HANDLER_TYPES,
  TimeUnitType,
} from '#/views/bpm/components/simple-process-design/consts';

defineOptions({ name: 'ElementCustomConfig4BoundaryEventTimer' });
const props = defineProps({
  id: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: '',
  },
});
const prefix = inject('prefix');

const bpmnElement = ref<any>();
const bpmnInstances = () => (window as Record<string, any>)?.bpmnInstances;

const timeoutHandlerEnable = ref(false);
const boundaryEventType = ref<any>();
const timeoutHandlerType = ref<{
  value: number | undefined;
}>({
  value: undefined,
});
const timeModdle = ref<any>();
const timeDuration = ref(6);
const timeUnit = ref(TimeUnitType.HOUR);
const maxRemindCount = ref(1);

const elExtensionElements = ref<any>();
const otherExtensions = ref<any[]>();
const configExtensions = ref<any[]>([]);
const eventDefinition = ref<any>();

const resetElement = () => {
  bpmnElement.value = bpmnInstances().bpmnElement;
  eventDefinition.value = bpmnElement.value.businessObject.eventDefinitions[0];

  // 获取元素扩展属性 或者 创建扩展属性
  elExtensionElements.value =
    bpmnElement.value.businessObject?.extensionElements ??
    bpmnInstances().moddle.create('bpmn:ExtensionElements', { values: [] });

  // 是否开启自定义用户任务超时处理
  boundaryEventType.value = elExtensionElements.value.values?.find(
    (ex: any) => ex.$type === `${prefix}:BoundaryEventType`,
  );
  if (boundaryEventType.value && boundaryEventType.value.value === 1) {
    timeoutHandlerEnable.value = true;
    configExtensions.value.push(boundaryEventType.value);
  }

  // 执行动作
  timeoutHandlerType.value = elExtensionElements.value.values?.find(
    (ex: any) => ex.$type === `${prefix}:TimeoutHandlerType`,
  )?.[0];
  if (timeoutHandlerType.value) {
    configExtensions.value.push(timeoutHandlerType.value);
    if (eventDefinition.value.timeCycle) {
      const timeStr = eventDefinition.value.timeCycle.body;
      const maxRemindCountStr = timeStr.split('/')[0];
      const timeDurationStr = timeStr.split('/')[1];
      maxRemindCount.value = Number.parseInt(maxRemindCountStr.slice(1));
      timeDuration.value = Number.parseInt(timeDurationStr.slice(2, -1));
      timeUnit.value = convertTimeUnit(timeDurationStr.slice(-1));
      timeModdle.value = eventDefinition.value.timeCycle;
    }
    if (eventDefinition.value.timeDuration) {
      const timeDurationStr = eventDefinition.value.timeDuration.body;
      timeDuration.value = Number.parseInt(timeDurationStr.slice(2, -1));
      timeUnit.value = convertTimeUnit(timeDurationStr.slice(-1));
      timeModdle.value = eventDefinition.value.timeDuration;
    }
  }

  // 保留剩余扩展元素，便于后面更新该元素对应属性
  otherExtensions.value =
    elExtensionElements.value.values?.filter(
      (ex: any) =>
        ex.$type !== `${prefix}:BoundaryEventType` &&
        ex.$type !== `${prefix}:TimeoutHandlerType`,
    ) ?? [];
};

const timeoutHandlerChange = (checked: any) => {
  timeoutHandlerEnable.value = checked;
  if (checked) {
    // 启用自定义用户任务超时处理
    // 边界事件类型 --- 超时
    boundaryEventType.value = bpmnInstances().moddle.create(
      `${prefix}:BoundaryEventType`,
      {
        value: 1,
      },
    );
    configExtensions.value.push(boundaryEventType.value);
    // 超时处理类型
    timeoutHandlerType.value = bpmnInstances().moddle.create(
      `${prefix}:TimeoutHandlerType`,
      {
        value: 1,
      },
    );
    configExtensions.value.push(timeoutHandlerType.value);
    // 超时时间表达式
    timeDuration.value = 6;
    timeUnit.value = 2;
    maxRemindCount.value = 1;
    timeModdle.value = bpmnInstances().moddle.create(`bpmn:Expression`, {
      body: 'PT6H',
    });
    eventDefinition.value.timeDuration = timeModdle.value;
  } else {
    // 关闭自定义用户任务超时处理
    configExtensions.value = [];
    delete eventDefinition.value.timeDuration;
    delete eventDefinition.value.timeCycle;
  }
  updateElementExtensions();
};

const onTimeoutHandlerTypeChanged = () => {
  maxRemindCount.value = 1;
  updateElementExtensions();
  updateTimeModdle();
};

const onTimeUnitChange = () => {
  // 分钟，默认是 60 分钟
  if (timeUnit.value === TimeUnitType.MINUTE) {
    timeDuration.value = 60;
  }
  // 小时，默认是 6 个小时
  if (timeUnit.value === TimeUnitType.HOUR) {
    timeDuration.value = 6;
  }
  // 天， 默认 1天
  if (timeUnit.value === TimeUnitType.DAY) {
    timeDuration.value = 1;
  }
  updateTimeModdle();
  updateElementExtensions();
};

const updateTimeModdle = () => {
  if (maxRemindCount.value > 1) {
    timeModdle.value.body = `R${maxRemindCount.value}/${isoTimeDuration()}`;
    if (!eventDefinition.value.timeCycle) {
      delete eventDefinition.value.timeDuration;
      eventDefinition.value.timeCycle = timeModdle.value;
    }
  } else {
    timeModdle.value.body = isoTimeDuration();
    if (!eventDefinition.value.timeDuration) {
      delete eventDefinition.value.timeCycle;
      eventDefinition.value.timeDuration = timeModdle.value;
    }
  }
};

const isoTimeDuration = () => {
  let strTimeDuration = 'PT';
  if (timeUnit.value === TimeUnitType.MINUTE) {
    strTimeDuration += `${timeDuration.value}M`;
  }
  if (timeUnit.value === TimeUnitType.HOUR) {
    strTimeDuration += `${timeDuration.value}H`;
  }
  if (timeUnit.value === TimeUnitType.DAY) {
    strTimeDuration += `${timeDuration.value}D`;
  }
  return strTimeDuration;
};

const updateElementExtensions = () => {
  const extensions = bpmnInstances().moddle.create('bpmn:ExtensionElements', {
    values: [...(otherExtensions.value || []), ...configExtensions.value],
  });
  bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
    extensionElements: extensions,
  });
};

watch(
  () => props.id,
  (val) => {
    val &&
      val.length > 0 &&
      nextTick(() => {
        resetElement();
      });
  },
  { immediate: true },
);
</script>

<template>
  <div>
    <Divider orientation="left">审批人超时未处理时</Divider>
    <FormItem label="启用开关" name="timeoutHandlerEnable">
      <Switch
        v-model:checked="timeoutHandlerEnable"
        checked-children="开启"
        un-checked-children="关闭"
        @change="timeoutHandlerChange"
      />
    </FormItem>
    <FormItem
      label="执行动作"
      name="timeoutHandlerType"
      v-if="timeoutHandlerEnable"
    >
      <RadioGroup
        v-model:value="timeoutHandlerType.value"
        @change="onTimeoutHandlerTypeChanged"
      >
        <RadioButton
          v-for="item in TIMEOUT_HANDLER_TYPES"
          :key="item.value"
          :value="item.value"
        >
          {{ item.label }}
        </RadioButton>
      </RadioGroup>
    </FormItem>
    <FormItem label="超时时间设置" v-if="timeoutHandlerEnable">
      <span class="mr-2">当超过</span>
      <FormItem name="timeDuration">
        <InputNumber
          class="mr-2"
          :style="{ width: '100px' }"
          v-model:value="timeDuration"
          :min="1"
          :controls="true"
          @change="
            () => {
              updateTimeModdle();
              updateElementExtensions();
            }
          "
        />
      </FormItem>
      <Select
        v-model:value="timeUnit"
        class="mr-2"
        :style="{ width: '100px' }"
        @change="onTimeUnitChange"
      >
        <SelectOption
          v-for="item in TIME_UNIT_TYPES"
          :key="item.value"
          :value="item.value"
        >
          {{ item.label }}
        </SelectOption>
      </Select>
      未处理
    </FormItem>
    <FormItem
      label="最大提醒次数"
      name="maxRemindCount"
      v-if="timeoutHandlerEnable && timeoutHandlerType.value === 1"
    >
      <InputNumber
        v-model:value="maxRemindCount"
        :min="1"
        :max="10"
        @change="
          () => {
            updateTimeModdle();
            updateElementExtensions();
          }
        "
      />
    </FormItem>
  </div>
</template>

<style lang="scss" scoped></style>
