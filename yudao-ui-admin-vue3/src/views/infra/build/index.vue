<template>
  <div class="app-container">
    <v-form-designer :designer-config="designerConfig" />
  </div>
</template>

<script setup>
const designerConfig = reactive({
  languageMenu: true,  // 是否显示语言切换菜单
  externalLink: true,  // 是否显示 GitHub、文档等外部链接
  formTemplates: true,  // 是否显示表单模板
  eventCollapse: true,  // 是否显示组件事件属性折叠面板
  clearDesignerButton: true,  // 是否显示清空设计器按钮
  previewFormButton: true,  // 是否显示预览表单按钮
  importJsonButton: true,  // 是否显示导入 JSON 按钮
  exportJsonButton: true,  // 是否显示导出 JSON 器按钮
  exportCodeButton: true,  // 是否显示导出代码按钮
  generateSFCButton: true,  // 是否显示生成 SFC 按钮
  toolbarMaxWidth: 420,  // 设计器工具按钮栏最大宽度（单位像素）
  toolbarMinWidth: 300,  // 设计器工具按钮栏最小宽度（单位像素）
});
</script>

<style lang="scss">
@import "@/assets/styles/variables.module.scss";

.app-container {
  margin-left: -$base-sidebar-width;
  padding: 0;
  height: calc(100vh - 84px) !important;

  /** 解决 index.scss 对 aside 的修改 */
  aside {
    background: #ffffff;
    padding: 0;
  }

  /** 让清空、预览等按钮，支持右对齐 */
  .right-toolbar-con {
    text-align: right !important;
  }

  /** 隐藏【技术 WX 群】【订阅 Pro】按钮，避免和项目存在误导 */
  .external-link {
    a:nth-child(n + 4) {
      display: none !important;
    }
  }
}
</style>
