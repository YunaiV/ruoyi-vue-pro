<template>
  <ContentWrap title="数据库文档">
    <!-- 操作工具栏 -->
    <div class="mb-10px">
       <XButton
          type="primary"
          preIcon="ep:download"
          :title="t('action.export') + ' HTML'"
          @click="handleExport('HTML')"
        />
         <XButton
          type="primary"
          preIcon="ep:download"
          :title="t('action.export') + ' Word'"
          @click="handleExport('Word')"
        />
         <XButton
          type="primary"
          preIcon="ep:download"
          :title="t('action.export') + ' Markdown'"
          @click="handleExport('Markdown')"
        />
    </div>
    <IFrame v-if="!loding" v-loading="loding" :src="src" />
  </ContentWrap>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import download from '@/utils/download'
import { useI18n } from '@/hooks/web/useI18n'
import { IFrame } from '@/components/IFrame'
import * as DbDocApi from '@/api/infra/dbDoc'

const { t } = useI18n() // 国际化
const src = ref('')
const loding = ref(true)
/** 页面加载 */
const init = async () => {
  const res = await DbDocApi.exportHtmlApi()
  let blob = new Blob([res], { type: 'text/html' })
  let blobUrl = window.URL.createObjectURL(blob)
  src.value = blobUrl
  loding.value = false
}
/** 处理导出  */
const handleExport = async (type: string) => {
  const res = await DbDocApi.exportHtmlApi()
  if (type === 'HTML') { download.html(res, '数据库文档.html') }
  if (type === 'Word') { download.word(res, '数据库文档.doc') }
  if(type === 'Markdown') {download.markdown(res, '数据库文档.md')}
  
}
onMounted(async () => {
  await init()
})
</script>

