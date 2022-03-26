<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="handleExportHtml">导出 HTML</el-button>
        <el-button type="primary" icon="Plus" @click="handleExportWord">导出 Word</el-button>
        <el-button type="primary" icon="Plus" @click="handleExportMarkdown">导出 Markdown</el-button>
      </el-col>
    </el-row>

    <!-- 展示文档 -->
    <i-frame :src="src" />
  </div>
</template>
<script setup>
import { exportHtml, exportWord, exportMarkdown} from "@/api/infra/dbDoc";
import iFrame from '@/components/iFrame'

const { proxy } = getCurrentInstance();

const src = ref(undefined);

/** 处理导出 HTML */
function handleExportHtml() {
  exportHtml().then(response => {
    proxy.$download.html(response, '数据库文档.html');
  })
}

/** 处理导出 Word */
function handleExportWord() {
  exportWord().then(response => {
    proxy.$download.word(response, '数据库文档.doc');
  })
}
/** 处理导出 Markdown */
function handleExportMarkdown() {
  exportMarkdown().then(response => {
    proxy.$download.markdown(response, '数据库文档.md');
  })
}

// 加载 Html，进行预览
exportHtml().then(response => {
  const blob = new Blob([response], {type : 'text/html'});
  src.value = window.URL.createObjectURL(blob);
})
</script>
