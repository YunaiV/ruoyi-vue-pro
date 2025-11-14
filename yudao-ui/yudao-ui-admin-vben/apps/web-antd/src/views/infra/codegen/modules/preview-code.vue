<script lang="ts" setup>
import type { InfraCodegenApi } from '#/api/infra/codegen';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { CodeEditor } from '@vben/plugins/code-editor';

import { useClipboard } from '@vueuse/core';
import { Button, DirectoryTree, message, Tabs } from 'ant-design-vue';

import { previewCodegen } from '#/api/infra/codegen';

/** 文件树类型 */
interface FileNode {
  key: string;
  title: string;
  parentKey: string;
  isLeaf?: boolean;
  children?: FileNode[];
}

/** 组件状态 */
const loading = ref(false);
const fileTree = ref<FileNode[]>([]);
const previewFiles = ref<InfraCodegenApi.CodegenPreview[]>([]);
const activeKey = ref<string>('');

/** 代码地图 */
const codeMap = ref<Map<string, string>>(new Map<string, string>());
function setCodeMap(key: string, code: string) {
  // 处理可能的缩进问题，特别是对Java文件
  const trimmedCode = code.trimStart();
  // 如果已有缓存则不重新构建
  if (codeMap.value.has(key)) {
    return;
  }
  codeMap.value.set(key, trimmedCode);
}

/** 删除代码地图 */
function removeCodeMapKey(targetKey: any) {
  // 只有一个代码视图时不允许删除
  if (codeMap.value.size === 1) {
    return;
  }
  if (codeMap.value.has(targetKey)) {
    codeMap.value.delete(targetKey);
  }
}

/** 复制代码 */
async function copyCode() {
  const { copy } = useClipboard();
  const file = previewFiles.value.find(
    (item) => item.filePath === activeKey.value,
  );
  if (file) {
    await copy(file.code);
    message.success('复制成功');
  }
}

/** 文件节点点击事件 */
function handleNodeClick(_: any[], e: any) {
  if (!e.node.isLeaf) {
    return;
  }

  activeKey.value = e.node.key;
  const file = previewFiles.value.find((item) => {
    const list = activeKey.value.split('.');
    // 特殊处理 - 包合并
    if (list.length > 2) {
      const lang = list.pop();
      return item.filePath === `${list.join('/')}.${lang}`;
    }
    return item.filePath === activeKey.value;
  });
  if (!file) {
    return;
  }

  setCodeMap(activeKey.value, file.code);
}

/** 处理文件树 */
function handleFiles(data: InfraCodegenApi.CodegenPreview[]): FileNode[] {
  const exists: Record<string, boolean> = {};
  const files: FileNode[] = [];

  // 处理文件路径
  for (const item of data) {
    const paths = item.filePath.split('/');
    let cursor = 0;
    let fullPath = '';

    while (cursor < paths.length) {
      const path = paths[cursor] || '';
      const oldFullPath = fullPath;

      // 处理 Java 包路径特殊情况
      if (path === 'java' && cursor + 1 < paths.length) {
        fullPath = fullPath ? `${fullPath}/${path}` : path;
        cursor++;

        // 合并包路径
        let packagePath = '';
        while (cursor < paths.length) {
          const nextPath = paths[cursor] || '';
          if (
            [
              'controller',
              'convert',
              'dal',
              'dataobject',
              'enums',
              'mysql',
              'service',
              'vo',
            ].includes(nextPath)
          ) {
            break;
          }
          packagePath = packagePath ? `${packagePath}.${nextPath}` : nextPath;
          cursor++;
        }

        if (packagePath) {
          const newFullPath = `${fullPath}/${packagePath}`;
          if (!exists[newFullPath]) {
            exists[newFullPath] = true;
            files.push({
              key: newFullPath,
              title: packagePath,
              parentKey: oldFullPath || '/',
              isLeaf: cursor === paths.length,
            });
          }
          fullPath = newFullPath;
        }
        continue;
      }

      // 处理普通路径
      fullPath = fullPath ? `${fullPath}/${path}` : path;
      if (!exists[fullPath]) {
        exists[fullPath] = true;
        files.push({
          key: fullPath,
          title: path,
          parentKey: oldFullPath || '/',
          isLeaf: cursor === paths.length - 1,
        });
      }
      cursor++;
    }
  }

  /** 构建树形结构 */
  function buildTree(parentKey: string): FileNode[] {
    return files
      .filter((file) => file.parentKey === parentKey)
      .map((file) => ({
        ...file,
        children: buildTree(file.key),
      }));
  }

  return buildTree('/');
}

/** 模态框实例 */
const [Modal, modalApi] = useVbenModal({
  footer: false,
  fullscreen: true,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      // 关闭时清除代码视图缓存
      codeMap.value.clear();
      return;
    }

    const row = modalApi.getData<InfraCodegenApi.CodegenTable>();
    if (!row) {
      return;
    }

    // 加载预览数据
    loading.value = true;
    try {
      const data = await previewCodegen(row.id);
      previewFiles.value = data;

      // 构建代码树，并默认选中第一个文件
      fileTree.value = handleFiles(data);
      if (data.length > 0) {
        activeKey.value = data[0]?.filePath || '';
        const code = data[0]?.code || '';
        setCodeMap(activeKey.value, code);
      }
    } finally {
      loading.value = false;
    }
  },
});
</script>

<template>
  <Modal title="代码预览">
    <div class="flex h-full" v-loading="loading">
      <!-- 文件树 -->
      <div
        class="h-full w-1/3 overflow-auto border-r border-gray-200 pr-4 dark:border-gray-700"
      >
        <DirectoryTree
          v-if="fileTree.length > 0"
          default-expand-all
          v-model:active-key="activeKey"
          @select="handleNodeClick"
          :tree-data="fileTree"
        />
      </div>
      <!-- 代码预览 -->
      <div class="h-full w-2/3 overflow-auto pl-4">
        <Tabs
          v-model:active-key="activeKey"
          hide-add
          type="editable-card"
          @edit="removeCodeMapKey"
        >
          <Tabs.TabPane
            v-for="key in codeMap.keys()"
            :key="key"
            :tab="key.split('/').pop()"
          >
            <div
              class="h-full rounded-md bg-gray-50 !p-0 text-gray-800 dark:bg-gray-800 dark:text-gray-200"
            >
              <CodeEditor
                class="max-h-200"
                :value="codeMap.get(activeKey)"
                mode="application/json"
                :readonly="true"
                :bordered="true"
                :auto-format="false"
              />
            </div>
          </Tabs.TabPane>
          <template #rightExtra>
            <Button type="primary" ghost @click="copyCode">
              <IconifyIcon icon="lucide:copy" />
              复制代码
            </Button>
          </template>
        </Tabs>
      </div>
    </div>
  </Modal>
</template>
