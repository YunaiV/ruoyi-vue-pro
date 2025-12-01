<!-- 产品的物模型表单（event、service 项里的参数） -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import { ref, unref } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Button, Divider, Form, Input, Modal } from 'ant-design-vue';

import { IoTDataSpecsDataTypeEnum } from '#/views/iot/utils/constants';

import ThingModelProperty from './thing-model-property.vue';

/** 输入输出参数配置组件 */
defineOptions({ name: 'ThingModelInputOutputParam' });

const props = defineProps<{ direction: string; modelValue: any }>();
const emits = defineEmits(['update:modelValue']);
const thingModelParams = useVModel(props, 'modelValue', emits) as Ref<any[]>;
const dialogVisible = ref(false); // 弹窗的是否展示
const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const paramFormRef = ref(); // 表单 ref
const formData = ref<any>({
  dataType: IoTDataSpecsDataTypeEnum.INT,
  property: {
    dataType: IoTDataSpecsDataTypeEnum.INT,
    dataSpecs: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
    },
    dataSpecsList: [],
  },
});

/** 打开 param 表单 */
function openParamForm(val: any) {
  dialogVisible.value = true;
  resetForm();
  if (isEmpty(val)) {
    return;
  }
  // 编辑时回显数据
  const valData = val as any;
  formData.value = {
    identifier: valData?.identifier || '',
    name: valData?.name || '',
    description: valData?.description || '',
    property: {
      dataType: valData?.dataType || IoTDataSpecsDataTypeEnum.INT,
      dataSpecs: valData?.dataSpecs ?? {},
      dataSpecsList: valData?.dataSpecsList ?? [],
    },
  };

  // 确保 property.dataType 有值
  if (!formData.value.property.dataType) {
    formData.value.property.dataType = IoTDataSpecsDataTypeEnum.INT;
  }
}

/** 删除 param 项 */
function deleteParamItem(index: number) {
  thingModelParams.value.splice(index, 1);
}

/** 添加参数 */
async function submitForm() {
  // 初始化参数列表
  if (isEmpty(thingModelParams.value)) {
    thingModelParams.value = [];
  }
  // 校验参数
  await paramFormRef.value.validate();
  try {
    // 构建数据对象
    const data = unref(formData);
    const item = {
      identifier: data.identifier,
      name: data.name,
      description: data.description,
      dataType: data.property.dataType,
      paraOrder: 0, // TODO @puhui999: 先写死默认看看后续
      direction: props.direction,
      dataSpecs:
        !!data.property.dataSpecs &&
        Object.keys(data.property.dataSpecs).length > 1
          ? data.property.dataSpecs
          : undefined,
      dataSpecsList: isEmpty(data.property.dataSpecsList)
        ? undefined
        : data.property.dataSpecsList,
    };

    // 新增或修改同 identifier 的参数
    const existingIndex = thingModelParams.value.findIndex(
      (spec) => spec.identifier === data.identifier,
    );
    if (existingIndex === -1) {
      thingModelParams.value.push(item);
    } else {
      thingModelParams.value[existingIndex] = item;
    }
  } finally {
    dialogVisible.value = false;
  }
}

/** 重置表单 */
function resetForm() {
  formData.value = {
    dataType: IoTDataSpecsDataTypeEnum.INT,
    property: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
      dataSpecs: {
        dataType: IoTDataSpecsDataTypeEnum.INT,
      },
      dataSpecsList: [],
    },
  };
  paramFormRef.value?.resetFields();
}
</script>

<template>
  <div
    v-for="(item, index) in thingModelParams"
    :key="index"
    class="w-1/1 px-10px mb-10px flex justify-between bg-gray-100"
  >
    <span>参数名称：{{ item.name }}</span>
    <div class="btn">
      <Button type="link" @click="openParamForm(item)">编辑</Button>
      <Divider type="vertical" />
      <Button type="link" danger @click="deleteParamItem(index)">删除</Button>
    </div>
  </div>
  <Button type="link" @click="openParamForm(null)">+新增参数</Button>

  <!-- param 表单 -->
  <Modal
    v-model:open="dialogVisible"
    title="新增参数"
    :confirm-loading="formLoading"
    @ok="submitForm"
  >
    <Form
      ref="paramFormRef"
      :model="formData"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <Form.Item label="参数名称" name="name">
        <Input v-model:value="formData.name" placeholder="请输入功能名称" />
      </Form.Item>
      <Form.Item label="标识符" name="identifier">
        <Input v-model:value="formData.identifier" placeholder="请输入标识符" />
      </Form.Item>
      <!-- 属性配置 -->
      <ThingModelProperty v-model="formData.property" is-params />
    </Form>
  </Modal>
</template>
