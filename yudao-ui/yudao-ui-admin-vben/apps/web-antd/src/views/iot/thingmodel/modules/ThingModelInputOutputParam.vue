<!-- 产品的物模型表单（event、service 项里的参数） -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import { ref, unref } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Button, Divider, Form, Input, Modal } from 'ant-design-vue';

import { IoTDataSpecsDataTypeEnum } from '#/views/iot/utils/constants';

import ThingModelProperty from './ThingModelProperty.vue';

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
  formData.value = {
    identifier: val.identifier,
    name: val.name,
    description: val.description,
    property: {
      dataType: val.dataType,
      dataSpecs: val.dataSpecs,
      dataSpecsList: val.dataSpecsList,
    },
  };
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
      <Button link type="primary" @click="openParamForm(item)"> 编辑 </Button>
      <Divider direction="vertical" />
      <Button link danger @click="deleteParamItem(index)"> 删除 </Button>
    </div>
  </div>
  <Button link type="primary" @click="openParamForm(null)"> +新增参数 </Button>

  <!-- param 表单 -->
  <Modal v-model="dialogVisible" title="新增参数" append-to-body>
    <Form
      ref="paramFormRef"
      v-loading="formLoading"
      :model="formData"
      label-width="100px"
    >
      <Form.Item label="参数名称" prop="name">
        <Input v-model="formData.name" placeholder="请输入功能名称" />
      </Form.Item>
      <Form.Item label="标识符" prop="identifier">
        <Input v-model="formData.identifier" placeholder="请输入标识符" />
      </Form.Item>
      <!-- 属性配置 -->
      <ThingModelProperty v-model="formData.property" is-params />
    </Form>
    <template #footer>
      <Button :disabled="formLoading" type="primary" @click="submitForm">
        确 定
      </Button>
      <Button @click="dialogVisible = false">取 消</Button>
    </template>
  </Modal>
</template>
