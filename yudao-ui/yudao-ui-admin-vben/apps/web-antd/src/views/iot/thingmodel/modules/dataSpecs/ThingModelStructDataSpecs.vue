<!-- dataType：struct 数组类型 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import { nextTick, onMounted, ref, unref } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Button, Divider, Form, Input, Modal } from 'ant-design-vue';

import { IoTDataSpecsDataTypeEnum } from '#/views/iot/utils/constants';

import ThingModelProperty from '../ThingModelProperty.vue';

/** Struct 型的 dataSpecs 配置组件 */
defineOptions({ name: 'ThingModelStructDataSpecs' });

const props = defineProps<{ modelValue: any }>();
const emits = defineEmits(['update:modelValue']);
const dataSpecsList = useVModel(props, 'modelValue', emits) as Ref<any[]>;
const dialogVisible = ref(false); // 弹窗的是否展示
const dialogTitle = ref('新增参数'); // 弹窗的标题
const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const structFormRef = ref(); // 表单 ref
const formData = ref<any>({
  property: {
    dataType: IoTDataSpecsDataTypeEnum.INT,
    dataSpecs: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
    },
  },
});

/** 打开 struct 表单 */
function openStructForm(val: any) {
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
      dataType: val.childDataType,
      dataSpecs: val.dataSpecs,
      dataSpecsList: val.dataSpecsList,
    },
  };
}

/** 删除 struct 项 */
function deleteStructItem(index: number) {
  dataSpecsList.value.splice(index, 1);
}

/** 添加参数 */
async function submitForm() {
  await structFormRef.value.validate();

  try {
    const data = unref(formData);
    // 构建数据对象
    const item = {
      identifier: data.identifier,
      name: data.name,
      description: data.description,
      dataType: IoTDataSpecsDataTypeEnum.STRUCT,
      childDataType: data.property.dataType,
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
    const existingIndex = dataSpecsList.value.findIndex(
      (spec) => spec.identifier === data.identifier,
    );
    if (existingIndex === -1) {
      dataSpecsList.value.push(item);
    } else {
      dataSpecsList.value[existingIndex] = item;
    }
  } finally {
    dialogVisible.value = false;
  }
}

/** 重置表单 */
function resetForm() {
  formData.value = {
    property: {
      dataType: IoTDataSpecsDataTypeEnum.INT,
      dataSpecs: {
        dataType: IoTDataSpecsDataTypeEnum.INT,
      },
    },
  };
  structFormRef.value?.resetFields();
}

/** 校验 struct 不能为空 */
function validateList(_: any, __: any, callback: any) {
  if (isEmpty(dataSpecsList.value)) {
    callback(new Error('struct 不能为空'));
    return;
  }
  callback();
}

/** 组件初始化 */
onMounted(async () => {
  await nextTick();
  // 预防 dataSpecsList 空指针
  isEmpty(dataSpecsList.value) && (dataSpecsList.value = []);
});
</script>

<template>
  <!-- struct 数据展示 -->
  <Form.Item
    :rules="[{ required: true, validator: validateList, trigger: 'change' }]"
    label="JSON 对象"
  >
    <div
      v-for="(item, index) in dataSpecsList"
      :key="index"
      class="px-10px mb-10px flex w-full justify-between bg-gray-100"
    >
      <span>参数名称：{{ item.name }}</span>
      <div class="btn">
        <Button link type="primary" @click="openStructForm(item)">
          编辑
        </Button>
        <Divider direction="vertical" />
        <Button link danger @click="deleteStructItem(index)"> 删除 </Button>
      </div>
    </div>
    <Button link type="primary" @click="openStructForm(null)">
      +新增参数
    </Button>
  </Form.Item>

  <!-- struct 表单 -->
  <Modal v-model="dialogVisible" :title="dialogTitle" append-to-body>
    <Form
      ref="structFormRef"
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
      <ThingModelProperty v-model="formData.property" is-struct-data-specs />
    </Form>
    <template #footer>
      <Button :disabled="formLoading" type="primary" @click="submitForm">
        确 定
      </Button>
      <Button @click="dialogVisible = false">取 消</Button>
    </template>
  </Modal>
</template>
