<script lang="ts" setup>
import { provide, reactive, ref } from 'vue';

import { useAccess } from '@vben/access';
import { confirm, DocAlert, Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Card, Form, message, Pagination, Tabs } from 'ant-design-vue';

import { deletePermanentMaterial, getMaterialPage } from '#/api/mp/material';
import { WxAccountSelect } from '#/views/mp/components';

import ImageTable from './components/image-table.vue';
import { UploadType } from './components/upload';
import UploadFile from './components/UploadFile.vue';
import UploadVideo from './components/UploadVideo.vue';
import VideoTable from './components/video-table.vue';
import VoiceTable from './components/voice-table.vue';

defineOptions({ name: 'MpMaterial' });

const { hasAccessByCodes } = useAccess();

const type = ref<UploadType>(UploadType.Image); // 素材类型
const loading = ref(false); // 遮罩层
const list = ref<any[]>([]); // 数据列表
const total = ref(0); // 总条数

const accountId = ref(-1);
provide('accountId', accountId);

const queryParams = reactive({
  accountId,
  pageNo: 1,
  pageSize: 10,
  permanent: true,
}); // 查询参数
const showCreateVideo = ref(false); // 是否新建视频的弹窗

/** 侦听公众号变化 */
function onAccountChanged(id: number) {
  accountId.value = id;
  queryParams.accountId = id;
  queryParams.pageNo = 1;
  getList();
}

/** 查询列表 */
async function getList() {
  loading.value = true;
  try {
    const data = await getMaterialPage({
      ...queryParams,
      type: type.value,
    });
    list.value = data.list;
    total.value = data.total;
  } finally {
    loading.value = false;
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNo = 1;
  getList();
}

/** 处理 tab 切换 */
function onTabChange() {
  // 提前清空数据，避免 tab 切换后显示垃圾数据
  list.value = [];
  total.value = 0;
  // 从第一页开始查询
  handleQuery();
}

/** 处理删除操作 */
async function handleDelete(id: number) {
  await confirm('此操作将永久删除该文件, 是否继续?');
  const hideLoading = message.loading({
    content: '正在删除...',
    duration: 0,
  });
  try {
    await deletePermanentMaterial(id);
    message.success('删除成功');
    await getList();
  } finally {
    hideLoading();
  }
}
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="公众号素材" url="https://doc.iocoder.cn/mp/material/" />
    </template>
    <div class="h-full">
      <!-- 搜索工作栏 -->
      <Card class="h-[10%]" :bordered="false">
        <Form :model="queryParams" layout="inline">
          <Form.Item label="公众号">
            <WxAccountSelect @change="onAccountChanged" />
          </Form.Item>
        </Form>
      </Card>

      <Card :bordered="false" class="mt-4 h-auto">
        <Tabs v-model:active-key="type" @change="onTabChange">
          <!-- tab 1：图片  -->
          <Tabs.TabPane :key="UploadType.Image">
            <template #tab>
              <span class="flex items-center">
                <IconifyIcon icon="lucide:image" class="mr-1" />
                图片
              </span>
            </template>
            <UploadFile
              v-if="hasAccessByCodes(['mp:material:upload-permanent'])"
              :type="UploadType.Image"
              @uploaded="getList"
            >
              支持 bmp/png/jpeg/jpg/gif 格式，大小不超过 2M
            </UploadFile>
            <!-- 列表 -->
            <ImageTable
              :list="list"
              :loading="loading"
              @delete="handleDelete"
            />
            <!-- 分页组件 -->
            <div class="mt-4 flex justify-end">
              <Pagination
                v-model:current="queryParams.pageNo"
                v-model:page-size="queryParams.pageSize"
                :total="total"
                show-size-changer
                @change="getList"
                @show-size-change="getList"
              />
            </div>
          </Tabs.TabPane>

          <!-- tab 2：语音  -->
          <Tabs.TabPane :key="UploadType.Voice">
            <template #tab>
              <span class="flex items-center">
                <IconifyIcon icon="lucide:mic" class="mr-1" />
                语音
              </span>
            </template>
            <UploadFile
              v-if="hasAccessByCodes(['mp:material:upload-permanent'])"
              :type="UploadType.Voice"
              @uploaded="getList"
            >
              格式支持 mp3/wma/wav/amr，文件大小不超过 2M，播放长度不超过 60s
            </UploadFile>
            <!-- 列表 -->
            <VoiceTable
              :list="list"
              :loading="loading"
              @delete="handleDelete"
            />
            <!-- 分页组件 -->
            <div class="mt-4 flex justify-end">
              <Pagination
                v-model:current="queryParams.pageNo"
                v-model:page-size="queryParams.pageSize"
                :total="total"
                show-size-changer
                @change="getList"
                @show-size-change="getList"
              />
            </div>
          </Tabs.TabPane>

          <!-- tab 3：视频 -->
          <Tabs.TabPane :key="UploadType.Video">
            <template #tab>
              <span class="flex items-center">
                <IconifyIcon icon="lucide:video" class="mr-1" />
                视频
              </span>
            </template>
            <Button
              v-if="hasAccessByCodes(['mp:material:upload-permanent'])"
              type="primary"
              @click="showCreateVideo = true"
            >
              新建视频
            </Button>
            <!-- 新建视频的弹窗 -->
            <UploadVideo v-model:open="showCreateVideo" @uploaded="getList" />
            <!-- 列表 -->
            <VideoTable
              :list="list"
              :loading="loading"
              @delete="handleDelete"
            />
            <!-- 分页组件 -->
            <div class="mt-4 flex justify-end">
              <Pagination
                v-model:current="queryParams.pageNo"
                v-model:page-size="queryParams.pageSize"
                :total="total"
                show-size-changer
                @change="getList"
                @show-size-change="getList"
              />
            </div>
          </Tabs.TabPane>
        </Tabs>
      </Card>
    </div>
  </Page>
</template>
