<script setup lang="ts">
import { IFrame } from '@/components/IFrame'
import * as DbDocApi from '@/api/infra/dbDoc'
import { onMounted, ref } from 'vue'
import download from '@/utils/download'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n() // 国际化
const loding = ref(true)
const src = ref('')
/** 页面加载 */
const init = async () => {
  const res = await DbDocApi.exportHtmlApi()
  let blob = new Blob([res], { type: 'text/html' })
  let blobUrl = window.URL.createObjectURL(blob)
  src.value = blobUrl
  loding.value = false
}
/** 处理导出 HTML */
const handleExportHtml = async () => {
  const res = await DbDocApi.exportHtmlApi()
  download.html(res, '数据库文档.html')
}
/** 处理导出 Word */
const handleExportWord = async () => {
  const res = await DbDocApi.exportHtmlApi()
  download.word(res, '数据库文档.doc')
}
/** 处理导出 Markdown */
const handleExportMarkdown = async () => {
  const res = await DbDocApi.exportHtmlApi()
  download.markdown(res, '数据库文档.md')
}
onMounted(async () => {
  await init()
})
</script>

<template>
  <ContentWrap title="数据库文档" message="https://doc.iocoder.cn/db-doc/">
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button type="primary" @click="handleExportHtml">
        <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') + ' HTML' }}
      </el-button>
      <el-button type="primary" @click="handleExportWord">
        <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') + ' Word' }}
      </el-button>
      <el-button type="primary" @click="handleExportMarkdown">
        <Icon icon="ep:download" class="mr-5px" /> {{ t('action.export') + ' Markdown' }}
      </el-button>
    </div>
    <IFrame v-if="!loding" v-loading="loding" :src="src" />
  </ContentWrap>
</template>
