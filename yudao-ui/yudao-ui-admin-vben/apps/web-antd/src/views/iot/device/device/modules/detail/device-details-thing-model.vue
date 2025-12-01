<!-- 设备物模型：设备属性、事件管理、服务调用 -->
<script setup lang="ts">
import type { ThingModelData } from '#/api/iot/thingmodel';

import { ref } from 'vue';

import { ContentWrap } from '@vben/common-ui';

import { Tabs } from 'ant-design-vue';

import DeviceDetailsThingModelEvent from './device-details-thing-model-event.vue';
import DeviceDetailsThingModelProperty from './device-details-thing-model-property.vue';
import DeviceDetailsThingModelService from './device-details-thing-model-service.vue';

const props = defineProps<{
  deviceId: number;
  thingModelList: ThingModelData[];
}>();

const activeTab = ref('property'); // 默认选中设备属性
</script>
<template>
  <ContentWrap>
    <Tabs v-model:active-key="activeTab" class="!h-auto !p-0">
      <Tabs.TabPane key="property" tab="设备属性（运行状态）">
        <DeviceDetailsThingModelProperty
          v-if="activeTab === 'property'"
          :device-id="deviceId"
        />
      </Tabs.TabPane>
      <Tabs.TabPane key="event" tab="设备事件上报">
        <DeviceDetailsThingModelEvent
          v-if="activeTab === 'event'"
          :device-id="props.deviceId"
          :thing-model-list="props.thingModelList"
        />
      </Tabs.TabPane>
      <Tabs.TabPane key="service" tab="设备服务调用">
        <DeviceDetailsThingModelService
          v-if="activeTab === 'service'"
          :device-id="deviceId"
          :thing-model-list="props.thingModelList"
        />
      </Tabs.TabPane>
    </Tabs>
  </ContentWrap>
</template>
