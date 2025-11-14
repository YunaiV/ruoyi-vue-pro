<script setup lang="ts">
import type { HotZoneProperty } from './config';

import { ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { ElButton, ElForm, ElFormItem, ElText } from 'element-plus';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';
import UploadImg from '#/components/upload/image-upload.vue';

import HotZoneEditDialog from './components/hot-zone-edit-dialog/index.vue';

/** 热区属性面板 */
defineOptions({ name: 'HotZoneProperty' });

const props = defineProps<{ modelValue: HotZoneProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);

// 热区编辑对话框
const editDialogRef = ref();
// 打开热区编辑对话框
const handleOpenEditDialog = () => {
  editDialogRef.value.open();
};
</script>

<template>
  <ComponentContainerProperty v-model="formData.style">
    <!-- 表单 -->
    <ElForm label-width="80px" :model="formData" class="mt-2">
      <ElFormItem label="上传图片" prop="imgUrl">
        <UploadImg
          v-model="formData.imgUrl"
          height="50px"
          width="auto"
          class="min-w-[80px]"
          :show-description="false"
        >
          <template #tip>
            <ElText type="info" size="small"> 推荐宽度 750</ElText>
          </template>
        </UploadImg>
      </ElFormItem>
    </ElForm>

    <ElButton type="primary" plain class="w-full" @click="handleOpenEditDialog">
      设置热区
    </ElButton>
  </ComponentContainerProperty>
  <!-- 热区编辑对话框 -->
  <HotZoneEditDialog
    ref="editDialogRef"
    v-model="formData.list"
    :img-url="formData.imgUrl"
  />
</template>

<style scoped lang="scss">
.hot-zone {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #fff;
  cursor: move;
  background: #409effbf;
  border: 1px solid var(--el-color-primary);

  /* 控制点 */
  .ctrl-dot {
    position: absolute;
    width: 4px;
    height: 4px;
    background-color: #fff;
    border-radius: 50%;
  }
}
</style>
