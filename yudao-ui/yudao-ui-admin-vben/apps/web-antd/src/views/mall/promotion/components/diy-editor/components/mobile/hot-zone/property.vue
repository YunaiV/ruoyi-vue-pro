<script setup lang="ts">
import type { HotZoneProperty } from './config';

import { ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { Button, Form, FormItem } from 'ant-design-vue';

import UploadImg from '#/components/upload/image-upload.vue';

import ComponentContainerProperty from '../../component-container-property.vue';
import HotZoneEditDialog from './components/hot-zone-edit-dialog/index.vue';

/** 热区属性面板 */
defineOptions({ name: 'HotZoneProperty' });

const props = defineProps<{ modelValue: HotZoneProperty }>();

const emit = defineEmits(['update:modelValue']);

const formData = useVModel(props, 'modelValue', emit);

const editDialogRef = ref(); // 热区编辑对话框

/** 打开热区编辑对话框 */
function handleOpenEditDialog() {
  editDialogRef.value.open();
}
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <!-- 表单 -->
    <Form
      :label-col="{ style: { width: '80px' } }"
      :model="formData"
      class="mt-2"
    >
      <FormItem label="上传图片" name="imgUrl">
        <UploadImg
          v-model="formData.imgUrl"
          height="50px"
          width="auto"
          class="min-w-[80px]"
          :show-description="false"
        >
          <!-- TODO @芋艿：这里不提示；是不是组件得封装下；-->
          <template #tip> 推荐宽度 750 </template>
        </UploadImg>
      </FormItem>
    </Form>

    <Button type="primary" ghost class="w-full" @click="handleOpenEditDialog">
      设置热区
    </Button>
  </ComponentContainerProperty>

  <!-- 热区编辑对话框 -->
  <HotZoneEditDialog
    ref="editDialogRef"
    v-model="formData.list"
    :img-url="formData.imgUrl"
  />
</template>
