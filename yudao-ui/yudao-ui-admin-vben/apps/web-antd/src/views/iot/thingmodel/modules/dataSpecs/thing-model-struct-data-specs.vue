<!-- dataType：struct 数组类型 -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import { nextTick, onMounted, ref, unref } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Button, Divider, Form, Input, Modal } from 'ant-design-vue';

import { IoTDataSpecsDataTypeEnum } from '#/views/iot/utils/constants';

import ThingModelProperty from '../thing-model-property.vue';

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
    dataSpecsList: [],
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
  const valData = val as any;
  formData.value = {
    identifier: valData?.identifier || '',
    name: valData?.name || '',
    description: valData?.description || '',
    property: {
      dataType: valData?.childDataType || IoTDataSpecsDataTypeEnum.INT,
      dataSpecs: valData?.dataSpecs ?? {},
      dataSpecsList: valData?.dataSpecsList ?? [],
    },
  };

  // 确保 property.dataType 有值
  if (!formData.value.property.dataType) {
    formData.value.property.dataType = IoTDataSpecsDataTypeEnum.INT;
  }
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
      dataSpecsList: [],
    },
  };
  structFormRef.value?.resetFields();
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
  <Form.Item label="属性对象">
    <div
      v-for="(item, index) in dataSpecsList"
      :key="index"
      class="px-10px mb-10px flex w-full justify-between bg-gray-100"
    >
      <span>参数：{{ item.name }}</span>
      <div class="btn">
        <Button type="link" @click="openStructForm(item)"> 编辑 </Button>
        <Divider type="vertical" />
        <Button type="link" danger @click="deleteStructItem(index)">
          删除
        </Button>
      </div>
    </div>
    <Button type="link" @click="openStructForm(null)"> +新增参数 </Button>
  </Form.Item>

  <!-- struct 表单 -->
  <Modal
    v-model:open="dialogVisible"
    :title="dialogTitle"
    :confirm-loading="formLoading"
    @ok="submitForm"
  >
    <Form
      ref="structFormRef"
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
      <ThingModelProperty v-model="formData.property" is-struct-data-specs />
    </Form>
  </Modal>
</template>
