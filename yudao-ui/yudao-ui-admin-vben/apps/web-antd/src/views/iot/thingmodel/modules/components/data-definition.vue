<!-- TODO @haohao：如果是模块内用的，就用 modules 里。（等后面点在看，优先级：低） -->
<script lang="ts" setup>
import type { ThingModelData } from '#/api/iot/thingmodel';

import { computed } from 'vue';

import { Tooltip } from 'ant-design-vue';

import {
  getEventTypeLabel,
  getThingModelServiceCallTypeLabel,
  IoTDataSpecsDataTypeEnum,
  IoTThingModelTypeEnum,
} from '#/views/iot/utils/constants';

/** 数据定义展示组件 */
defineOptions({ name: 'DataDefinition' });

const props = defineProps<{ data: ThingModelData }>();

const formattedDataSpecsList = computed(() => {
  if (
    !props.data.property?.dataSpecsList ||
    props.data.property.dataSpecsList.length === 0
  ) {
    return '';
  }
  return props.data.property.dataSpecsList
    .map((item) => `${item.value}-${item.name}`)
    .join('、');
}); // 格式化布尔值和枚举值列表为字符串

const shortText = computed(() => {
  if (
    !props.data.property?.dataSpecsList ||
    props.data.property.dataSpecsList.length === 0
  ) {
    return '-';
  }
  const first = props.data.property.dataSpecsList[0];
  const count = props.data.property.dataSpecsList.length;
  return count > 1
    ? `${first.value}-${first.name} 等${count}项`
    : `${first.value}-${first.name}`;
}); // 显示的简短文本（第一个值）
</script>

<template>
  <!-- 属性 -->
  <template v-if="Number(data.type) === IoTThingModelTypeEnum.PROPERTY">
    <!-- 非列表型：数值 -->
    <div
      v-if="
        [
          IoTDataSpecsDataTypeEnum.INT,
          IoTDataSpecsDataTypeEnum.DOUBLE,
          IoTDataSpecsDataTypeEnum.FLOAT,
        ].includes(data.property?.dataType as any)
      "
    >
      取值范围：{{
        `${data.property?.dataSpecs?.min}~${data.property?.dataSpecs?.max}`
      }}
    </div>
    <!-- 非列表型：文本 -->
    <div v-if="IoTDataSpecsDataTypeEnum.TEXT === data.property?.dataType">
      数据长度：{{ data.property?.dataSpecs?.length }}
    </div>
    <!-- 列表型: 数组、结构、时间（特殊） -->
    <div
      v-if="
        [
          IoTDataSpecsDataTypeEnum.ARRAY,
          IoTDataSpecsDataTypeEnum.STRUCT,
          IoTDataSpecsDataTypeEnum.DATE,
        ].includes(data.property?.dataType as any)
      "
    >
      -
    </div>
    <!-- 列表型: 布尔值、枚举 -->
    <div
      v-if="
        [IoTDataSpecsDataTypeEnum.BOOL, IoTDataSpecsDataTypeEnum.ENUM].includes(
          data.property?.dataType as any,
        )
      "
    >
      <Tooltip :title="formattedDataSpecsList" placement="topLeft">
        <span class="data-specs-text">
          {{
            IoTDataSpecsDataTypeEnum.BOOL === data.property?.dataType
              ? '布尔值'
              : '枚举值'
          }}：{{ shortText }}
        </span>
      </Tooltip>
    </div>
  </template>
  <!-- 服务 -->
  <div v-if="Number(data.type) === IoTThingModelTypeEnum.SERVICE">
    调用方式：
    {{ getThingModelServiceCallTypeLabel(data.service?.callType as any) }}
  </div>
  <!-- 事件 -->
  <div v-if="Number(data.type) === IoTThingModelTypeEnum.EVENT">
    事件类型：{{ getEventTypeLabel(data.event?.type as any) }}
  </div>
</template>

<style lang="scss" scoped>
/** TODO @haohao：tindwind */
.data-specs-text {
  cursor: help;
  border-bottom: 1px dashed #d9d9d9;

  &:hover {
    color: #1890ff;
    border-bottom-color: #1890ff;
  }
}
</style>
