<template>
  <XModal title="预览" v-model="preview.open">
    <div class="flex">
      <el-card class="w-1/4" :gutter="12" shadow="hover">
        <el-scrollbar height="calc(100vh - 88px - 40px - 50px)">
          <el-tree
            ref="treeRef"
            node-key="id"
            :data="preview.fileTree"
            :expand-on-click-node="false"
            default-expanded-keys="[0]"
            highlight-current
            @node-click="handleNodeClick"
          />
        </el-scrollbar>
      </el-card>
      <el-card class="w-3/4" style="margin-left: 10px" :gutter="12" shadow="hover">
        <el-tabs v-model="preview.activeName">
          <el-tab-pane
            v-for="item in previewCodegen"
            :label="item.filePath.substring(item.filePath.lastIndexOf('/') + 1)"
            :name="item.filePath"
            :key="item.filePath"
          >
            <XTextButton style="float: right" :title="t('common.copy')" @click="copy(item.code)" />
            <pre>{{ item.code }}</pre>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </XModal>
</template>
<script setup lang="ts">
import { reactive, ref, unref } from 'vue'
import { useClipboard } from '@vueuse/core'
import { ElCard, ElTree, ElTabs, ElTabPane } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { handleTree2 } from '@/utils/tree'
import { previewCodegenApi } from '@/api/infra/codegen'
import { CodegenTableVO, CodegenPreviewVO } from '@/api/infra/codegen/types'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// ======== 显示页面 ========
const preview = reactive({
  open: false,
  titel: '代码预览',
  fileTree: [],
  activeName: ''
})
const previewCodegen = ref<CodegenPreviewVO[]>()
const show = async (row: CodegenTableVO) => {
  const res = await previewCodegenApi(row.id)
  let file = handleFiles(res)
  previewCodegen.value = res
  preview.fileTree = handleTree2(file, 'id', 'parentId', 'children', '/')
  preview.activeName = res[0].filePath
  preview.open = true
}
const handleNodeClick = async (data, node) => {
  if (node && !node.isLeaf) {
    return false
  }
  preview.activeName = data.id
}
/** 生成 files 目录 **/
interface filesType {
  id: string
  label: string
  parentId: string
}
const handleFiles = (datas: CodegenPreviewVO[]) => {
  let exists = {} // key：file 的 id；value：true
  let files: filesType[] = []
  // 遍历每个元素
  for (const data of datas) {
    let paths = data.filePath.split('/')
    let fullPath = '' // 从头开始的路径，用于生成 id
    // 特殊处理 java 文件
    if (paths[paths.length - 1].indexOf('.java') >= 0) {
      let newPaths: string[] = []
      for (let i = 0; i < paths.length; i++) {
        let path = paths[i]
        if (path !== 'java') {
          newPaths.push(path)
          continue
        }
        newPaths.push(path)
        // 特殊处理中间的 package，进行合并
        let tmp = ''
        while (i < paths.length) {
          path = paths[i + 1]
          if (
            path === 'controller' ||
            path === 'convert' ||
            path === 'dal' ||
            path === 'enums' ||
            path === 'service' ||
            path === 'vo' || // 下面三个，主要是兜底。可能考虑到有人改了包结构
            path === 'mysql' ||
            path === 'dataobject'
          ) {
            break
          }
          tmp = tmp ? tmp + '.' + path : path
          i++
        }
        if (tmp) {
          newPaths.push(tmp)
        }
      }
      paths = newPaths
    }
    // 遍历每个 path， 拼接成树
    for (let i = 0; i < paths.length; i++) {
      // 已经添加到 files 中，则跳过
      let oldFullPath = fullPath
      // 下面的 replaceAll 的原因，是因为上面包处理了，导致和 tabs 不匹配，所以 replaceAll 下
      fullPath = fullPath.length === 0 ? paths[i] : fullPath.replaceAll('.', '/') + '/' + paths[i]
      if (exists[fullPath]) {
        continue
      }
      // 添加到 files 中
      exists[fullPath] = true
      files.push({
        id: fullPath,
        label: paths[i],
        parentId: oldFullPath || '/' // "/" 为根节点
      })
    }
  }
  return files
}
/** 复制 **/
const copy = async (text: string) => {
  const { copy, copied, isSupported } = useClipboard({ source: text })
  if (!isSupported) {
    message.error(t('common.copyError'))
  } else {
    await copy()
    if (unref(copied)) {
      message.success(t('common.copySuccess'))
    }
  }
}
defineExpose({
  show
})
</script>
