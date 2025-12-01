<script setup lang="ts">
import type { IoTOtaFirmware } from '#/api/iot/ota/firmware';

import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { formatDate } from '@vben/utils';

import { Card, Col, Descriptions, Row } from 'ant-design-vue';

import { getOtaFirmware } from '#/api/iot/ota/firmware';
import { getOtaTaskRecordStatusStatistics } from '#/api/iot/ota/task/record';
import { IoTOtaTaskRecordStatusEnum } from '#/views/iot/utils/constants';

import OtaTaskList from '../task/ota-task-list.vue';

/** IoT OTA 固件详情 */
defineOptions({ name: 'IoTOtaFirmwareDetail' });

const route = useRoute();

const firmwareId = ref(Number(route.params.id));
const firmwareLoading = ref(false);
const firmware = ref<IoTOtaFirmware>({} as IoTOtaFirmware);

const firmwareStatisticsLoading = ref(false);
const firmwareStatistics = ref<Record<string, number>>({});

/** 获取固件信息 */
async function getFirmwareInfo() {
  firmwareLoading.value = true;
  try {
    firmware.value = await getOtaFirmware(firmwareId.value);
  } finally {
    firmwareLoading.value = false;
  }
}

/** 获取升级统计 */
async function getStatistics() {
  firmwareStatisticsLoading.value = true;
  try {
    firmwareStatistics.value = await getOtaTaskRecordStatusStatistics(
      firmwareId.value,
    );
  } finally {
    firmwareStatisticsLoading.value = false;
  }
}

/** 初始化 */
onMounted(() => {
  getFirmwareInfo();
  getStatistics();
});
</script>

<template>
  <div class="p-4">
    <!-- 固件信息 -->
    <Card title="固件信息" class="mb-3" :loading="firmwareLoading">
      <Descriptions :column="3" bordered size="small">
        <Descriptions.Item label="固件名称">
          {{ firmware?.name }}
        </Descriptions.Item>
        <Descriptions.Item label="所属产品">
          {{ firmware?.productName }}
        </Descriptions.Item>
        <Descriptions.Item label="固件版本">
          {{ firmware?.version }}
        </Descriptions.Item>
        <Descriptions.Item label="创建时间">
          {{
            firmware?.createTime
              ? formatDate(firmware.createTime, 'YYYY-MM-DD HH:mm:ss')
              : '-'
          }}
        </Descriptions.Item>
        <Descriptions.Item label="固件描述" :span="2">
          {{ firmware?.description }}
        </Descriptions.Item>
      </Descriptions>
    </Card>

    <!-- 升级设备统计 -->
    <Card
      title="升级设备统计"
      class="mb-3"
      :loading="firmwareStatisticsLoading"
    >
      <Row :gutter="20" class="py-3">
        <Col :span="6">
          <div
            class="rounded border border-solid border-gray-200 bg-gray-50 p-3 text-center"
          >
            <div class="mb-1 text-3xl font-bold text-blue-500">
              {{
                Object.values(firmwareStatistics).reduce(
                  (sum: number, count) => sum + (count || 0),
                  0,
                ) || 0
              }}
            </div>
            <div class="text-sm text-gray-600">升级设备总数</div>
          </div>
        </Col>
        <Col :span="3">
          <div
            class="rounded border border-solid border-gray-200 bg-gray-50 p-3 text-center"
          >
            <div class="mb-1 text-3xl font-bold text-gray-400">
              {{
                firmwareStatistics[IoTOtaTaskRecordStatusEnum.PENDING.value] ||
                0
              }}
            </div>
            <div class="text-sm text-gray-600">待推送</div>
          </div>
        </Col>
        <Col :span="3">
          <div
            class="rounded border border-solid border-gray-200 bg-gray-50 p-3 text-center"
          >
            <div class="mb-1 text-3xl font-bold text-blue-400">
              {{
                firmwareStatistics[IoTOtaTaskRecordStatusEnum.PUSHED.value] || 0
              }}
            </div>
            <div class="text-sm text-gray-600">已推送</div>
          </div>
        </Col>
        <Col :span="3">
          <div
            class="rounded border border-solid border-gray-200 bg-gray-50 p-3 text-center"
          >
            <div class="mb-1 text-3xl font-bold text-yellow-500">
              {{
                firmwareStatistics[
                  IoTOtaTaskRecordStatusEnum.UPGRADING.value
                ] || 0
              }}
            </div>
            <div class="text-sm text-gray-600">正在升级</div>
          </div>
        </Col>
        <Col :span="3">
          <div
            class="rounded border border-solid border-gray-200 bg-gray-50 p-3 text-center"
          >
            <div class="mb-1 text-3xl font-bold text-green-500">
              {{
                firmwareStatistics[IoTOtaTaskRecordStatusEnum.SUCCESS.value] ||
                0
              }}
            </div>
            <div class="text-sm text-gray-600">升级成功</div>
          </div>
        </Col>
        <Col :span="3">
          <div
            class="rounded border border-solid border-gray-200 bg-gray-50 p-3 text-center"
          >
            <div class="mb-1 text-3xl font-bold text-red-500">
              {{
                firmwareStatistics[IoTOtaTaskRecordStatusEnum.FAILURE.value] ||
                0
              }}
            </div>
            <div class="text-sm text-gray-600">升级失败</div>
          </div>
        </Col>
        <Col :span="3">
          <div
            class="rounded border border-solid border-gray-200 bg-gray-50 p-3 text-center"
          >
            <div class="mb-1 text-3xl font-bold text-gray-400">
              {{
                firmwareStatistics[IoTOtaTaskRecordStatusEnum.CANCELED.value] ||
                0
              }}
            </div>
            <div class="text-sm text-gray-600">升级取消</div>
          </div>
        </Col>
      </Row>
    </Card>

    <!-- 任务管理 -->
    <OtaTaskList
      v-if="firmware?.productId"
      :firmware-id="firmwareId"
      :product-id="firmware.productId"
      @success="getStatistics"
    />
  </div>
</template>
