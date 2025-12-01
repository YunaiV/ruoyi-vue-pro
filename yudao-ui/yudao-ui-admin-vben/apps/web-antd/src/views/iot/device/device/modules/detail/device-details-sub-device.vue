<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { Card, Empty } from 'ant-design-vue';

interface Props {
  deviceId: number;
}

defineProps<Props>();

const loading = ref(false);
const subDevices = ref<any[]>([]);

/** 获取子设备列表 */
async function getSubDeviceList() {
  loading.value = true;
  try {
    // TODO: 实现获取子设备列表的API调用
    // const data = await getSubDevicesByGatewayId(deviceId);
    // subDevices.value = data || [];
    subDevices.value = [];
  } catch (error) {
    console.error('获取子设备列表失败:', error);
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  getSubDeviceList();
});
</script>

<template>
  <Card :loading="loading" title="子设备管理">
    <Empty
      description="暂无子设备数据，此功能待实现"
      :image="Empty.PRESENTED_IMAGE_SIMPLE"
    />
    <!-- TODO: 实现子设备列表展示和管理功能 -->
  </Card>
</template>
