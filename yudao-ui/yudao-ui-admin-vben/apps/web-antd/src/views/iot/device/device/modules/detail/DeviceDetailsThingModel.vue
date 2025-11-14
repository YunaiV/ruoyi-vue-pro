<!-- 设备物模型：设备属性、事件管理、服务调用 -->
<script setup lang="ts">
import type { ThingModelData } from '#/api/iot/thingmodel';

import { ref } from 'vue';

import { ContentWrap } from '@vben/common-ui';

import { Tabs } from 'ant-design-vue';

import DeviceDetailsThingModelEvent from './DeviceDetailsThingModelEvent.vue';
import DeviceDetailsThingModelProperty from './DeviceDetailsThingModelProperty.vue';
import DeviceDetailsThingModelService from './DeviceDetailsThingModelService.vue';

const props = defineProps<{
  deviceId: number;
  thingModelList: ThingModelData[];
}>();

const activeTab = ref('property'); // 默认选中设备属性
</script>
<template>
  <ContentWrap>
    <Tabs v-model:active-key="activeTab" class="!h-auto !p-0">
      <Tabs.Pane key="property" tab="设备属性（运行状态）">
        <DeviceDetailsThingModelProperty :device-id="deviceId" />
      </Tabs.Pane>
      <Tabs.Pane key="event" tab="设备事件上报">
        <DeviceDetailsThingModelEvent
          :device-id="props.deviceId"
          :thing-model-list="props.thingModelList"
        />
      </Tabs.Pane>
      <Tabs.Pane key="service" tab="设备服务调用">
        <DeviceDetailsThingModelService
          :device-id="deviceId"
          :thing-model-list="props.thingModelList"
        />
      </Tabs.Pane>
    </Tabs>
  </ContentWrap>
</template>
