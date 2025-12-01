<!-- 产品的物模型表单 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

// TODO @haohao：使用 form.vue；
import type { IotProductApi } from '#/api/iot/product/product';
import type { ThingModelData } from '#/api/iot/thingmodel';

import { inject, ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { $t } from '@vben/locales';
import { cloneDeep } from '@vben/utils';

import { Form, Input, message, Modal, Radio } from 'ant-design-vue';

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

import ThingModelEvent from './thing-model-event.vue';
import ThingModelProperty from './thing-model-property.vue';
import ThingModelService from './thing-model-service.vue';

/** IoT 物模型数据表单 */
defineOptions({ name: 'IoTThingModelForm' });

/** 提交表单 */
const emit = defineEmits(['success']);
const product = inject<Ref<IotProductApi.Product>>(IOT_PROVIDE_KEY.PRODUCT); // 注入产品信息

const dialogVisible = ref(false); // 弹窗的是否展示
const dialogTitle = ref(''); // 弹窗的标题
const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref(''); // 表单的类型：create - 新增；update - 修改
const formData = ref<any>({
  type: IoTThingModelTypeEnum.PROPERTY,
  dataType: IoTDataSpecsDataTypeEnum.INT,
  property: {
    dataType: IoTDataSpecsDataTypeEnum.INT,
    dataSpecs: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
    },
  },
  service: {
    inputParams: [],
    outputParams: [],
  },
  event: {
    outputParams: [],
  },
});

const formRef = ref(); // 表单 Ref

/** 打开弹窗 */
// TODO @haohao：Modal 的写法。
async function open(type: string, id?: number) {
  dialogVisible.value = true;
  // 设置标题：create -> 新增，update -> 编辑
  dialogTitle.value =
    type === 'create' ? $t('page.action.add') : $t('page.action.edit');
  formType.value = type;
  resetForm();
  if (id) {
    formLoading.value = true;
    try {
      const result = await getThingModel(id);
      // 转换类型为数字
      formData.value = {
        ...result,
        type: Number(result.type),
      };
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
      } else {
        // 确保 dataSpecs 和 dataSpecsList 存在
        if (!formData.value.property.dataSpecs) {
          formData.value.property.dataSpecs = {};
        }
        if (!formData.value.property.dataSpecsList) {
          formData.value.property.dataSpecsList = [];
        }
        // 如果 property.dataType 不存在，设置为默认值
        if (!formData.value.property.dataType) {
          formData.value.property.dataType = IoTDataSpecsDataTypeEnum.INT;
        }
      }
      // 情况二：服务初始化
      if (
        !formData.value.service ||
        Object.keys(formData.value.service).length === 0
      ) {
        formData.value.service = {
          inputParams: [],
          outputParams: [],
        };
      } else {
        // 确保参数数组存在
        if (!formData.value.service.inputParams) {
          formData.value.service.inputParams = [];
        }
        if (!formData.value.service.outputParams) {
          formData.value.service.outputParams = [];
        }
      }
      // 情况三：事件初始化
      if (
        !formData.value.event ||
        Object.keys(formData.value.event).length === 0
      ) {
        formData.value.event = {
          outputParams: [],
        };
      } else {
        // 确保参数数组存在
        if (!formData.value.event.outputParams) {
          formData.value.event.outputParams = [];
        }
      }
    } finally {
      formLoading.value = false;
    }
  }
}
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
    // 保留输入输出参数，但如果为空数组则删除
    if (!data.service.inputParams || data.service.inputParams.length === 0) {
      delete data.service.inputParams;
    }
    if (!data.service.outputParams || data.service.outputParams.length === 0) {
      delete data.service.outputParams;
    }
    delete data.property;
    delete data.event;
  }
  // 事件
  if (data.type === IoTThingModelTypeEnum.EVENT) {
    removeDataSpecs(data.event);
    data.dataType = data.event.dataType;
    data.event.identifier = data.identifier;
    data.event.name = data.name;
    // 保留输出参数，但如果为空数组则删除
    if (!data.event.outputParams || data.event.outputParams.length === 0) {
      delete data.event.outputParams;
    }
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
    type: IoTThingModelTypeEnum.PROPERTY,
    dataType: IoTDataSpecsDataTypeEnum.INT,
    property: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
      dataSpecs: {
        dataType: IoTDataSpecsDataTypeEnum.INT,
      },
    },
    service: {
      inputParams: [],
      outputParams: [],
    },
    event: {
      outputParams: [],
    },
  };
  formRef.value?.resetFields();
}
</script>

<template>
  <Modal
    v-model:open="dialogVisible"
    :title="dialogTitle"
    :confirm-loading="formLoading"
    @ok="submitForm"
  >
    <!-- TODO @haohao：这个可以改造成 data.ts schema 形式么？可能是有一定成本，后续迁移 ele 版本，会容易很多。 -->
    <Form
      ref="formRef"
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <Form.Item label="功能类型" name="type">
        <Radio.Group v-model:value="formData.type">
          <Radio.Button
            v-for="dict in getDictOptions(DICT_TYPE.IOT_THING_MODEL_TYPE)"
            :key="String(dict.value)"
            :value="Number(dict.value)"
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
        v-if="formData.type === IoTThingModelTypeEnum.PROPERTY"
        v-model="formData.property"
      />
      <!-- 服务配置 -->
      <ThingModelService
        v-if="formData.type === IoTThingModelTypeEnum.SERVICE"
        v-model="formData.service"
      />
      <!-- 事件配置 -->
      <ThingModelEvent
        v-if="formData.type === IoTThingModelTypeEnum.EVENT"
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
  </Modal>
</template>
