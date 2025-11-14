<script setup lang="ts">
import type { NavigationBarProperty } from './config';

import { useVModel } from '@vueuse/core';

import NavigationBarCellProperty from './components/cell-property.vue';

// 导航栏属性面板
defineOptions({ name: 'NavigationBarProperty' });
const props = defineProps<{ modelValue: NavigationBarProperty }>();

const emit = defineEmits(['update:modelValue']);

// 表单校验
const rules = {
  name: [{ required: true, message: '请输入页面名称', trigger: 'blur' }],
};

const formData = useVModel(props, 'modelValue', emit);
if (!formData.value._local) {
  formData.value._local = { previewMp: true, previewOther: false };
}
</script>

<template>
  <el-form label-width="80px" :model="formData" :rules="rules">
    <el-form-item label="样式" prop="styleType">
      <el-radio-group v-model="formData!.styleType">
        <el-radio value="normal">标准</el-radio>
        <el-tooltip
          content="沉侵式头部仅支持微信小程序、APP，建议页面第一个组件为图片展示类组件"
          placement="top"
        >
          <el-radio value="inner">沉浸式</el-radio>
        </el-tooltip>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      label="常驻显示"
      prop="alwaysShow"
      v-if="formData.styleType === 'inner'"
    >
      <el-radio-group v-model="formData!.alwaysShow">
        <el-radio :value="false">关闭</el-radio>
        <el-tooltip
          content="常驻显示关闭后,头部小组件将在页面滑动时淡入"
          placement="top"
        >
          <el-radio :value="true">开启</el-radio>
        </el-tooltip>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="背景类型" prop="bgType">
      <el-radio-group v-model="formData.bgType">
        <el-radio value="color">纯色</el-radio>
        <el-radio value="img">图片</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      label="背景颜色"
      prop="bgColor"
      v-if="formData.bgType === 'color'"
    >
      <ColorInput v-model="formData.bgColor" />
    </el-form-item>
    <el-form-item label="背景图片" prop="bgImg" v-else>
      <div class="flex items-center">
        <UploadImg
          v-model="formData.bgImg"
          :limit="1"
          width="56px"
          height="56px"
          :show-description="false"
        />
        <span class="mb-2 ml-2 text-xs text-gray-400">建议宽度：750</span>
      </div>
    </el-form-item>
    <el-card class="property-group" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span>内容（小程序）</span>
          <el-form-item prop="_local.previewMp" class="mb-0">
            <el-checkbox
              v-model="formData._local.previewMp"
              @change="
                formData._local.previewOther = !formData._local.previewMp
              "
            >
              预览
            </el-checkbox>
          </el-form-item>
        </div>
      </template>
      <NavigationBarCellProperty v-model="formData.mpCells" is-mp />
    </el-card>
    <el-card class="property-group" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span>内容（非小程序）</span>
          <el-form-item prop="_local.previewOther" class="mb-0">
            <el-checkbox
              v-model="formData._local.previewOther"
              @change="
                formData._local.previewMp = !formData._local.previewOther
              "
            >
              预览
            </el-checkbox>
          </el-form-item>
        </div>
      </template>
      <NavigationBarCellProperty v-model="formData.otherCells" :is-mp="false" />
    </el-card>
  </el-form>
</template>

<style scoped lang="scss"></style>
