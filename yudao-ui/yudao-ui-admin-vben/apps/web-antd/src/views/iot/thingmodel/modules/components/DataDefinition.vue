<script lang="ts" setup>
import type { ThingModelData } from '#/api/iot/thingmodel';

import {
  getEventTypeLabel,
  getThingModelServiceCallTypeLabel,
  IoTDataSpecsDataTypeEnum,
  IoTThingModelTypeEnum,
} from '#/views/iot/utils/constants';

/** 数据定义展示组件 */
defineOptions({ name: 'DataDefinition' });

defineProps<{ data: ThingModelData }>();
</script>

<template>
  <!-- 属性 -->
  <template v-if="data.type === IoTThingModelTypeEnum.PROPERTY.toString()">
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
        `${data.property?.dataSpecs.min}~${data.property?.dataSpecs.max}`
      }}
    </div>
    <!-- 非列表型：文本 -->
    <div v-if="IoTDataSpecsDataTypeEnum.TEXT === data.property?.dataType">
      数据长度：{{ data.property?.dataSpecs.length }}
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
      <div>
        {{
          IoTDataSpecsDataTypeEnum.BOOL === data.property?.dataType
            ? '布尔值'
            : '枚举值'
        }}：
      </div>
      <div v-for="item in data.property?.dataSpecsList" :key="item.value">
        {{ `${item.name}-${item.value}` }}
      </div>
    </div>
  </template>
  <!-- 服务 -->
  <div v-if="data.type === IoTThingModelTypeEnum.SERVICE.toString()">
    调用方式：{{
      getThingModelServiceCallTypeLabel(data.service?.callType as any)
    }}
  </div>
  <!-- 事件 -->
  <div v-if="data.type === IoTThingModelTypeEnum.EVENT.toString()">
    事件类型：{{ getEventTypeLabel(data.event?.type as any) }}
  </div>
</template>

<style lang="scss" scoped></style>
