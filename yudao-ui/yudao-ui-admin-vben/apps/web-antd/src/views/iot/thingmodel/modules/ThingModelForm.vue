<!-- 产品的物模型表单 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import type { IotProductApi } from '#/api/iot/product/product';
import type { ThingModelData } from '#/api/iot/thingmodel';

import { inject, ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { $t } from '@vben/locales';
import { cloneDeep } from '@vben/utils';

import { Button, Form, Input, message, Radio } from 'ant-design-vue';

import {
  createThingModel,
  getThingModel,
  updateThingModel,
} from '#/api/iot/thingmodel';
import {
  IOT_PROVIDE_KEY,
  IoTDataSpecsDataTypeEnum,
  IoTThingModelTypeEnum,
} from '#/views/iot/utils/constants';

import ThingModelEvent from './ThingModelEvent.vue';
import ThingModelProperty from './ThingModelProperty.vue';
import ThingModelService from './ThingModelService.vue';

/** IoT 物模型数据表单 */
defineOptions({ name: 'IoTThingModelForm' });

/** 提交表单 */
const emit = defineEmits(['success']);
const product = inject<Ref<IotProductApi.Product>>(IOT_PROVIDE_KEY.PRODUCT); // 注入产品信息

const dialogVisible = ref(false); // 弹窗的是否展示
const dialogTitle = ref(''); // 弹窗的标题
const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref(''); // 表单的类型：create - 新增；update - 修改
const formData = ref<ThingModelData>({
  type: IoTThingModelTypeEnum.PROPERTY.toString(),
  dataType: IoTDataSpecsDataTypeEnum.INT,
  property: {
    dataType: IoTDataSpecsDataTypeEnum.INT,
    dataSpecs: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
    },
  },
  service: {},
  event: {},
});

const formRef = ref(); // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true;
  dialogTitle.value = $t(`action.${type}`);
  formType.value = type;
  resetForm();
  if (id) {
    formLoading.value = true;
    try {
      formData.value = await getThingModel(id);
      // 情况一：属性初始化
      if (
        !formData.value.property ||
        Object.keys(formData.value.property).length === 0
      ) {
        formData.value.dataType = IoTDataSpecsDataTypeEnum.INT;
        formData.value.property = {
          dataType: IoTDataSpecsDataTypeEnum.INT,
          dataSpecs: {
            dataType: IoTDataSpecsDataTypeEnum.INT,
          },
        };
      }
      // 情况二：服务初始化
      if (
        !formData.value.service ||
        Object.keys(formData.value.service).length === 0
      ) {
        formData.value.service = {};
      }
      // 情况三：事件初始化
      if (
        !formData.value.event ||
        Object.keys(formData.value.event).length === 0
      ) {
        formData.value.event = {};
      }
    } finally {
      formLoading.value = false;
    }
  }
};
defineExpose({ open, close: () => (dialogVisible.value = false) });

async function submitForm() {
  await formRef.value.validate();
  formLoading.value = true;
  try {
    const data = cloneDeep(formData.value) as ThingModelData;
    // 信息补全
    data.productId = product!.value.id;
    data.productKey = product!.value.productKey;
    fillExtraAttributes(data);
    await (formType.value === 'create'
      ? createThingModel(data)
      : updateThingModel(data));
    message.success($t('ui.actionMessage.operationSuccess'));
    // 关闭弹窗
    dialogVisible.value = false;
    emit('success');
  } finally {
    formLoading.value = false;
  }
}

/** 填写额外的属性（处理不同类型的情况） */
function fillExtraAttributes(data: any) {
  // 属性
  if (data.type === IoTThingModelTypeEnum.PROPERTY) {
    removeDataSpecs(data.property);
    data.dataType = data.property.dataType;
    data.property.identifier = data.identifier;
    data.property.name = data.name;
    delete data.service;
    delete data.event;
  }
  // 服务
  if (data.type === IoTThingModelTypeEnum.SERVICE) {
    removeDataSpecs(data.service);
    data.dataType = data.service.dataType;
    data.service.identifier = data.identifier;
    data.service.name = data.name;
    delete data.property;
    delete data.event;
  }
  // 事件
  if (data.type === IoTThingModelTypeEnum.EVENT) {
    removeDataSpecs(data.event);
    data.dataType = data.event.dataType;
    data.event.identifier = data.identifier;
    data.event.name = data.name;
    delete data.property;
    delete data.service;
  }
}

/** 处理 dataSpecs 为空的情况 */
function removeDataSpecs(val: any) {
  if (!val.dataSpecs || Object.keys(val.dataSpecs).length === 0) {
    delete val.dataSpecs;
  }
  if (!val.dataSpecsList || val.dataSpecsList.length === 0) {
    delete val.dataSpecsList;
  }
}

/** 重置表单 */
function resetForm() {
  formData.value = {
    type: IoTThingModelTypeEnum.PROPERTY.toString(),
    dataType: IoTDataSpecsDataTypeEnum.INT,
    property: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
      dataSpecs: {
        dataType: IoTDataSpecsDataTypeEnum.INT,
      },
    },
    service: {},
    event: {},
  };
  formRef.value?.resetFields();
}
</script>

<template>
  <Modal v-model="dialogVisible" :title="dialogTitle">
    <Form
      ref="formRef"
      :loading="formLoading"
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <Form.Item label="功能类型" name="type">
        <Radio.Group v-model:value="formData.type">
          <Radio.Button
            v-for="(dict, index) in getDictOptions(
              DICT_TYPE.IOT_THING_MODEL_TYPE,
              'number',
            )"
            :key="index"
            :value="dict.value"
          >
            {{ dict.label }}
          </Radio.Button>
        </Radio.Group>
      </Form.Item>
      <Form.Item label="功能名称" name="name">
        <Input v-model:value="formData.name" placeholder="请输入功能名称" />
      </Form.Item>
      <Form.Item label="标识符" name="identifier">
        <Input v-model:value="formData.identifier" placeholder="请输入标识符" />
      </Form.Item>
      <!-- 属性配置 -->
      <ThingModelProperty
        v-if="formData.type === IoTThingModelTypeEnum.PROPERTY.toString()"
        v-model="formData.property"
      />
      <!-- 服务配置 -->
      <ThingModelService
        v-if="formData.type === IoTThingModelTypeEnum.SERVICE.toString()"
        v-model="formData.service"
      />
      <!-- 事件配置 -->
      <ThingModelEvent
        v-if="formData.type === IoTThingModelTypeEnum.EVENT.toString()"
        v-model="formData.event"
      />
      <Form.Item label="描述" name="desc">
        <Input.TextArea
          v-model:value="formData.desc"
          :maxlength="200"
          :rows="3"
          placeholder="请输入属性描述"
        />
      </Form.Item>
    </Form>

    <template #footer>
      <Button :disabled="formLoading" type="primary" @click="submitForm">
        确 定
      </Button>
      <Button @click="dialogVisible = false">取 消</Button>
    </template>
  </Modal>
</template>
