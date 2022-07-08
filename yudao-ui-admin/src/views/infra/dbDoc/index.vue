<template>
  <div class="app-container">
    <doc-alert title="数据库文档" url="https://doc.iocoder.cn/db-doc/" />
    <!-- 操作工作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleExportHtml">导出 HTML</el-button>
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleExportWord">导出 Word</el-button>
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleExportMarkdown">导出 Markdown</el-button>
      </el-col>
    </el-row>

    <!-- 展示文档 -->
    <div v-loading="loading" :style="'height:'+ height">
      <i-frame :src="src" />
    </div>
  </div>
</template>
<script>
import { exportHtml, exportWord, exportMarkdown} from "@/api/infra/dbDoc";
import iFrame from "@/components/iFrame/index";

export default {
  name: "DBDoc",
  components: { iFrame },
  data() {
    return {
      height: document.documentElement.clientHeight - 94.5 + "px;",
      loading: true,
      src: "undefined",
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
        this.$download.html(response, '数据库文档.html');
      })
    },
    /** 处理导出 Word */
    handleExportWord() {
      exportWord().then(response => {
        this.$download.word(response, '数据库文档.doc');
      })
    },
    /** 处理导出 Markdown */
    handleExportMarkdown() {
      exportMarkdown().then(response => {
        this.$download.markdown(response, '数据库文档.md');
      })
    }
  }
};
</script>
