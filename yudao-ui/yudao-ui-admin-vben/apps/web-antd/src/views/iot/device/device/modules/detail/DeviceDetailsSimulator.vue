<!-- 模拟设备 -->
<script lang="ts" setup>
import type { TableColumnType } from 'ant-design-vue';

import type { IotDeviceApi } from '#/api/iot/device/device';
import type { IotProductApi } from '#/api/iot/product/product';
import type { ThingModelData } from '#/api/iot/thingmodel';

import { computed, ref } from 'vue';

import {
  Button,
  Card,
  Col,
  Input,
  message,
  Row,
  Table,
  Tabs,
  Textarea,
} from 'ant-design-vue';

import { DeviceStateEnum, sendDeviceMessage } from '#/api/iot/device/device';
import {
  IotDeviceMessageMethodEnum,
  IoTThingModelTypeEnum,
} from '#/views/iot/utils/constants';

import DeviceDetailsMessage from './DeviceDetailsMessage.vue';

const props = defineProps<{
  device: IotDeviceApi.Device;
  product: IotProductApi.Product;
  thingModelList: ThingModelData[];
}>();

// 消息弹窗
const activeTab = ref('upstream'); // 上行upstream、下行downstream
const upstreamTab = ref(IotDeviceMessageMethodEnum.PROPERTY_POST.method); // 上行子标签
const downstreamTab = ref(IotDeviceMessageMethodEnum.PROPERTY_SET.method); // 下行子标签
const deviceMessageRef = ref(); // 设备消息组件引用
const deviceMessageRefreshDelay = 2000; // 延迟 N 秒，保证模拟上行的消息被处理

// 表单数据：存储用户输入的模拟值
const formData = ref<Record<string, string>>({});

// 根据类型过滤物模型数据
const getFilteredThingModelList = (type: number) => {
  return props.thingModelList.filter(
    (item) => String(item.type) === String(type),
  );
};

// 计算属性：属性列表
const propertyList = computed(() =>
  getFilteredThingModelList(IoTThingModelTypeEnum.PROPERTY),
);

// 计算属性：事件列表
const eventList = computed(() =>
  getFilteredThingModelList(IoTThingModelTypeEnum.EVENT),
);

// 计算属性：服务列表
const serviceList = computed(() =>
  getFilteredThingModelList(IoTThingModelTypeEnum.SERVICE),
);

// 属性表格列定义
const propertyColumns: TableColumnType[] = [
  {
    title: '功能名称',
    dataIndex: 'name',
    key: 'name',
    width: 120,
    fixed: 'left' as any,
  },
  {
    title: '标识符',
    dataIndex: 'identifier',
    key: 'identifier',
    width: 120,
    fixed: 'left' as any,
  },
  {
    title: '数据类型',
    key: 'dataType',
    width: 100,
  },
  {
    title: '数据定义',
    key: 'dataDefinition',
    minWidth: 200,
  },
  {
    title: '值',
    key: 'value',
    width: 150,
    fixed: 'right' as any,
  },
];

// 事件表格列定义
const eventColumns = [
  {
    title: '功能名称',
    dataIndex: 'name',
    key: 'name',
    width: 120,
    fixed: 'left' as any,
  },
  {
    title: '标识符',
    dataIndex: 'identifier',
    key: 'identifier',
    width: 120,
    fixed: 'left' as any,
  },
  {
    title: '数据类型',
    key: 'dataType',
    width: 100,
  },
  {
    title: '数据定义',
    key: 'dataDefinition',
    minWidth: 200,
  },
  {
    title: '值',
    key: 'value',
    width: 200,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right' as any,
  },
];

// 服务表格列定义
const serviceColumns = [
  {
    title: '服务名称',
    dataIndex: 'name',
    key: 'name',
    width: 120,
    fixed: 'left' as any,
  },
  {
    title: '标识符',
    dataIndex: 'identifier',
    key: 'identifier',
    width: 120,
    fixed: 'left' as any,
  },
  {
    title: '输入参数',
    key: 'dataDefinition',
    minWidth: 200,
  },
  {
    title: '参数值',
    key: 'value',
    width: 200,
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    fixed: 'right' as any,
  },
];

// 获取表单值
function getFormValue(identifier: string) {
  return formData.value[identifier] || '';
}

// 设置表单值
function setFormValue(identifier: string, value: string) {
  formData.value[identifier] = value;
}

// 属性上报
async function handlePropertyPost() {
  try {
    const params: Record<string, any> = {};
    propertyList.value.forEach((item) => {
      const value = formData.value[item.identifier!];
      if (value) {
        params[item.identifier!] = value;
      }
    });

    if (Object.keys(params).length === 0) {
      message.warning({ content: '请至少输入一个属性值' });
      return;
    }

    await sendDeviceMessage({
      deviceId: props.device.id!,
      method: IotDeviceMessageMethodEnum.PROPERTY_POST.method,
      params,
    });

    message.success({ content: '属性上报成功' });
    // 延迟刷新设备消息列表
    deviceMessageRef.value?.refresh(deviceMessageRefreshDelay);
  } catch (error) {
    message.error({ content: '属性上报失败' });
    console.error(error);
  }
}

// 事件上报
async function handleEventPost(row: ThingModelData) {
  try {
    const valueStr = formData.value[row.identifier!];
    let params: any = {};

    if (valueStr) {
      try {
        params = JSON.parse(valueStr);
      } catch {
        message.error({ content: '事件参数格式错误，请输入有效的JSON格式' });
        return;
      }
    }

    await sendDeviceMessage({
      deviceId: props.device.id!,
      method: IotDeviceMessageMethodEnum.EVENT_POST.method,
      params: {
        identifier: row.identifier,
        params,
      },
    });

    message.success({ content: '事件上报成功' });
    // 延迟刷新设备消息列表
    deviceMessageRef.value?.refresh(deviceMessageRefreshDelay);
  } catch (error) {
    message.error({ content: '事件上报失败' });
    console.error(error);
  }
}

// 状态变更
async function handleDeviceState(state: number) {
  try {
    await sendDeviceMessage({
      deviceId: props.device.id!,
      method: IotDeviceMessageMethodEnum.STATE_UPDATE.method,
      params: { state },
    });

    message.success({ content: '状态变更成功' });
    // 延迟刷新设备消息列表
    deviceMessageRef.value?.refresh(deviceMessageRefreshDelay);
  } catch (error) {
    message.error({ content: '状态变更失败' });
    console.error(error);
  }
}

// 属性设置
async function handlePropertySet() {
  try {
    const params: Record<string, any> = {};
    propertyList.value.forEach((item) => {
      const value = formData.value[item.identifier!];
      if (value) {
        params[item.identifier!] = value;
      }
    });

    if (Object.keys(params).length === 0) {
      message.warning({ content: '请至少输入一个属性值' });
      return;
    }

    await sendDeviceMessage({
      deviceId: props.device.id!,
      method: IotDeviceMessageMethodEnum.PROPERTY_SET.method,
      params,
    });

    message.success({ content: '属性设置成功' });
    // 延迟刷新设备消息列表
    deviceMessageRef.value?.refresh(deviceMessageRefreshDelay);
  } catch (error) {
    message.error({ content: '属性设置失败' });
    console.error(error);
  }
}

// 服务调用
async function handleServiceInvoke(row: ThingModelData) {
  try {
    const valueStr = formData.value[row.identifier!];
    let params: any = {};

    if (valueStr) {
      try {
        params = JSON.parse(valueStr);
      } catch {
        message.error({ content: '服务参数格式错误，请输入有效的JSON格式' });
        return;
      }
    }

    await sendDeviceMessage({
      deviceId: props.device.id!,
      method: IotDeviceMessageMethodEnum.SERVICE_INVOKE.method,
      params: {
        identifier: row.identifier,
        params,
      },
    });

    message.success({ content: '服务调用成功' });
    // 延迟刷新设备消息列表
    deviceMessageRef.value?.refresh(deviceMessageRefreshDelay);
  } catch (error) {
    message.error({ content: '服务调用失败' });
    console.error(error);
  }
}
</script>

<template>
  <ContentWrap>
    <Row :gutter="20">
      <!-- 左侧指令调试区域 -->
      <Col :span="12">
        <Card>
          <Tabs v-model:active-key="activeTab">
            <!-- 上行指令调试 -->
            <Tabs.Pane key="upstream" tab="上行指令调试">
              <Tabs
                v-if="activeTab === 'upstream'"
                v-model:active-key="upstreamTab"
              >
                <!-- 属性上报 -->
                <Tabs.Pane
                  :key="IotDeviceMessageMethodEnum.PROPERTY_POST.method"
                  tab="属性上报"
                >
                  <ContentWrap>
                    <Table
                      :data-source="propertyList"
                      align="center"
                      :columns="propertyColumns"
                      :pagination="false"
                    >
                      <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'dataType'">
                          {{ record.property?.dataType ?? '-' }}
                        </template>
                        <template v-else-if="column.key === 'dataDefinition'">
                          <DataDefinition :data="record" />
                        </template>
                        <template v-else-if="column.key === 'value'">
                          <Input
                            :value="getFormValue(record.identifier)"
                            @update:value="
                              setFormValue(record.identifier, $event)
                            "
                            placeholder="输入值"
                            size="small"
                          />
                        </template>
                      </template>
                    </Table>
                    <div class="mt-4 flex items-center justify-between">
                      <span class="text-sm text-gray-600">
                        设置属性值后，点击「发送属性上报」按钮
                      </span>
                      <Button type="primary" @click="handlePropertyPost">
                        发送属性上报
                      </Button>
                    </div>
                  </ContentWrap>
                </Tabs.Pane>

                <!-- 事件上报 -->
                <Tabs.Pane
                  :key="IotDeviceMessageMethodEnum.EVENT_POST.method"
                  tab="事件上报"
                >
                  <ContentWrap>
                    <Table
                      :data-source="eventList"
                      align="center"
                      :columns="eventColumns"
                      :pagination="false"
                    >
                      <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'dataType'">
                          {{ record.event?.dataType ?? '-' }}
                        </template>
                        <template v-else-if="column.key === 'dataDefinition'">
                          <DataDefinition :data="record" />
                        </template>
                        <template v-else-if="column.key === 'value'">
                          <Textarea
                            :value="getFormValue(record.identifier)"
                            @update:value="
                              setFormValue(record.identifier, $event)
                            "
                            :rows="3"
                            placeholder="输入事件参数（JSON格式）"
                            size="small"
                          />
                        </template>
                        <template v-else-if="column.key === 'action'">
                          <Button
                            type="primary"
                            size="small"
                            @click="handleEventPost(record)"
                          >
                            上报事件
                          </Button>
                        </template>
                      </template>
                    </Table>
                  </ContentWrap>
                </Tabs.Pane>

                <!-- 状态变更 -->
                <Tabs.Pane
                  :key="IotDeviceMessageMethodEnum.STATE_UPDATE.method"
                  tab="状态变更"
                >
                  <ContentWrap>
                    <div class="flex gap-4">
                      <Button
                        type="primary"
                        @click="handleDeviceState(DeviceStateEnum.ONLINE)"
                      >
                        设备上线
                      </Button>
                      <Button
                        danger
                        @click="handleDeviceState(DeviceStateEnum.OFFLINE)"
                      >
                        设备下线
                      </Button>
                    </div>
                  </ContentWrap>
                </Tabs.Pane>
              </Tabs>
            </Tabs.Pane>

            <!-- 下行指令调试 -->
            <Tabs.Pane key="downstream" tab="下行指令调试">
              <Tabs
                v-if="activeTab === 'downstream'"
                v-model:active-key="downstreamTab"
              >
                <!-- 属性调试 -->
                <Tabs.Pane
                  :key="IotDeviceMessageMethodEnum.PROPERTY_SET.method"
                  tab="属性设置"
                >
                  <ContentWrap>
                    <Table
                      :data-source="propertyList"
                      align="center"
                      :columns="propertyColumns"
                      :pagination="false"
                    >
                      <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'dataType'">
                          {{ record.property?.dataType ?? '-' }}
                        </template>
                        <template v-else-if="column.key === 'dataDefinition'">
                          <DataDefinition :data="record" />
                        </template>
                        <template v-else-if="column.key === 'value'">
                          <Input
                            :value="getFormValue(record.identifier)"
                            @update:value="
                              setFormValue(record.identifier, $event)
                            "
                            placeholder="输入值"
                            size="small"
                          />
                        </template>
                      </template>
                    </Table>
                    <div class="mt-4 flex items-center justify-between">
                      <span class="text-sm text-gray-600">
                        设置属性值后，点击「发送属性设置」按钮
                      </span>
                      <Button type="primary" @click="handlePropertySet">
                        发送属性设置
                      </Button>
                    </div>
                  </ContentWrap>
                </Tabs.Pane>

                <!-- 服务调用 -->
                <Tabs.Pane
                  :key="IotDeviceMessageMethodEnum.SERVICE_INVOKE.method"
                  tab="设备服务调用"
                >
                  <ContentWrap>
                    <Table
                      :data-source="serviceList"
                      align="center"
                      :columns="serviceColumns"
                      :pagination="false"
                    >
                      <template #bodyCell="{ column, record }">
                        <template v-if="column.key === 'dataDefinition'">
                          <DataDefinition :data="record" />
                        </template>
                        <template v-else-if="column.key === 'value'">
                          <Textarea
                            :value="getFormValue(record.identifier)"
                            @update:value="
                              setFormValue(record.identifier, $event)
                            "
                            :rows="3"
                            placeholder="输入服务参数（JSON格式）"
                            size="small"
                          />
                        </template>
                        <template v-else-if="column.key === 'action'">
                          <Button
                            type="primary"
                            size="small"
                            @click="handleServiceInvoke(record)"
                          >
                            服务调用
                          </Button>
                        </template>
                      </template>
                    </Table>
                  </ContentWrap>
                </Tabs.Pane>
              </Tabs>
            </Tabs.Pane>
          </Tabs>
        </Card>
      </Col>

      <!-- 右侧设备日志区域 -->
      <Col :span="12">
        <ContentWrap title="设备消息">
          <DeviceDetailsMessage
            v-if="device.id"
            ref="deviceMessageRef"
            :device-id="device.id"
          />
        </ContentWrap>
      </Col>
    </Row>
  </ContentWrap>
</template>
