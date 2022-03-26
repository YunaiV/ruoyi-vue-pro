<template>
  <div class="app-container full">
    <v-form-designer :designer-config="designerConfig">
      <!-- 自定义按钮插槽演示 -->
      <template #customToolButtons>
        <el-button type="text" @click="saveFormJson"><i class="Finished" />保存</el-button>
      </template>
    </v-form-designer>
  </div>
</template>

<script setup>
const designerConfig = reactive({
  languageMenu: true,  // 是否显示语言切换菜单
  externalLink: true,  // 是否显示GitHub、文档等外部链接
  formTemplates: true,  // 是否显示表单模板
  eventCollapse: true,  // 是否显示组件事件属性折叠面板
  clearDesignerButton: true,  // 是否显示清空设计器按钮
  previewFormButton: true,  // 是否显示预览表单按钮
  importJsonButton: false,  // 是否显示导入JSON按钮
  exportJsonButton: true,  // 是否显示导出JSON器按钮
  exportCodeButton: true,  // 是否显示导出代码按钮
  generateSFCButton: true,  // 是否显示生成SFC按钮
  toolbarMaxWidth: 420,  //设计器工具按钮栏最大宽度（单位像素）
  toolbarMinWidth: 300,  //设计器工具按钮栏最小宽度（单位像素）
});
// TODO 芋艿：样式有问题
</script>

<style lang="scss">
@import "@/assets/styles/variables.module.scss";

.app-container {
  margin-left: -$base-sidebar-width;
  padding: 0;
  height: 100%;
}
</style>
