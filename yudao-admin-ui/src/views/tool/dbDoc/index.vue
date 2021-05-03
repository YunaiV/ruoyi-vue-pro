<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleExportHtml">导出 HTML</el-button>
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleExportWord">导出 Word</el-button>
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleExportMarkdown">导出 Markdown</el-button>
      </el-col>
    </el-row>

    <!-- 展示文档 -->
    <div v-loading="loading" :style="'height:'+ height">
      <iframe :src="src" frameborder="no" style="width: 100%;height: 100%" scrolling="auto" />
    </div>
  </div>
</template>
<script>
import { exportHtml, exportWord, exportMarkdown} from "@/api/infra/dbDoc";

export default {
  name: "DBDoc",
  data() {
    return {
      height: document.documentElement.clientHeight - 94.5 + "px;",
      loading: true,
      src: undefined,
    };
  },
  mounted: function() {
    setTimeout(() => {
      this.loading = false;
    }, 230);
    const that = this;
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 94.5 + "px;";
    };
  },
  created() {
    // 加载 Html，进行预览
    exportHtml().then(response => {
      let blob = new Blob([response], {type : 'text/html'});
      this.src = window.URL.createObjectURL(blob);
    })
  },
  methods: {
    /** 处理导出 HTML */
    handleExportHtml() {
      exportHtml().then(response => {
        this.downloadHtml(response, '数据库文档.html');
      })
    },
    /** 处理导出 Word */
    handleExportWord() {
      exportWord().then(response => {
        this.downloadWord(response, '数据库文档.doc');
      })
    },
    /** 处理导出 Markdown */
    handleExportMarkdown() {
      exportMarkdown().then(response => {
        this.downloadMarkdown(response, '数据库文档.md');
      })
    }
  }
};
</script>
