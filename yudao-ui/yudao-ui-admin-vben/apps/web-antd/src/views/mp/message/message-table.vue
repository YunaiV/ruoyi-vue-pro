<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpMessageApi } from '#/api/mp/message';

import { onMounted, watch } from 'vue';

import { MpMsgType as MsgType } from '@vben/constants';
import { formatDate2 } from '@vben/utils';

import { Button, Image, Tag } from 'ant-design-vue';

// TODO @dylan：看看 ele 要迁移一个么？
import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  WxLocation,
  WxMusic,
  WxNews,
  WxVideoPlayer,
  WxVoicePlayer,
} from '#/views/mp/components';

const props = withDefaults(
  defineProps<{
    list?: MpMessageApi.Message[];
    loading?: boolean;
  }>(),
  {
    list() {
      return [];
    },
    loading: false,
  },
);

const emit = defineEmits<{
  (e: 'send', userId: number): void;
}>();

const columns: VxeTableGridOptions<MpMessageApi.Message>['columns'] = [
  {
    field: 'createTime',
    title: '发送时间',
    width: 180,
    align: 'center',
    slots: { default: 'createTime' },
  },
  {
    field: 'type',
    title: '消息类型',
    width: 80,
    align: 'center',
  },
  {
    field: 'sendFrom',
    title: '发送方',
    width: 80,
    align: 'center',
    slots: { default: 'sendFrom' },
  },
  {
    field: 'openid',
    title: '用户标识',
    width: 300,
    align: 'center',
  },
  {
    field: 'content',
    title: '内容',
    align: 'left',
    minWidth: 320,
    slots: { default: 'content' },
  },
  {
    field: 'actions',
    title: '操作',
    width: 120,
    align: 'center',
    fixed: 'right',
    slots: { default: 'actions' },
  },
];

const [Grid, gridApi] = useVbenVxeGrid<MpMessageApi.Message>({
  gridOptions: {
    border: true,
    columns,
    keepSource: true,
    pagerConfig: {
      enabled: false,
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    showOverflow: 'tooltip',
  },
});

function normalizeList(list?: MpMessageApi.Message[]) {
  return Array.isArray(list) ? list : [];
}

function updateGridData(data: MpMessageApi.Message[]) {
  if (gridApi.grid?.loadData) {
    gridApi.grid.loadData(data);
  } else {
    gridApi.setGridOptions({ data });
  }
}

watch(
  () => props.list,
  (list) => {
    updateGridData(normalizeList(list));
  },
  { flush: 'post' },
);

watch(
  () => props.loading,
  (loading) => {
    gridApi.setLoading(loading);
  },
);

/** 初始化 */
onMounted(() => {
  updateGridData(normalizeList(props.list));
  gridApi.setLoading(props.loading);
});
</script>

<template>
  <Grid>
    <template #createTime="{ row }">
      {{ row.createTime ? formatDate2(row.createTime) : '' }}
    </template>

    <template #sendFrom="{ row }">
      <Tag v-if="row.sendFrom === 1" color="success">粉丝</Tag>
      <Tag v-else color="default">公众号</Tag>
    </template>

    <template #content="{ row }">
      <div
        v-if="
          (row.type as string) === (MsgType.Event as string) &&
          (row.event as string) === 'subscribe'
        "
      >
        <Tag color="success">关注</Tag>
      </div>
      <div
        v-else-if="
          (row.type as string) === (MsgType.Event as string) &&
          (row.event as string) === 'unsubscribe'
        "
      >
        <Tag color="error">取消关注</Tag>
      </div>
      <div
        v-else-if="
          (row.type as string) === (MsgType.Event as string) &&
          (row.event as string) === 'CLICK'
        "
      >
        <Tag>点击菜单</Tag>
        【{{ row.eventKey }}】
      </div>
      <div v-else-if="row.type === MsgType.Event && row.event === 'VIEW'">
        <Tag>点击菜单链接</Tag>
        【{{ row.eventKey }}】
      </div>
      <div
        v-else-if="
          row.type === MsgType.Event && row.event === 'scancode_waitmsg'
        "
      >
        <Tag>扫码结果</Tag>
        【{{ row.eventKey }}】
      </div>
      <div
        v-else-if="row.type === MsgType.Event && row.event === 'scancode_push'"
      >
        <Tag>扫码结果</Tag>
        【{{ row.eventKey }}】
      </div>
      <div
        v-else-if="row.type === MsgType.Event && row.event === 'pic_sysphoto'"
      >
        <Tag>系统拍照发图</Tag>
      </div>
      <div
        v-else-if="
          row.type === MsgType.Event && row.event === 'pic_photo_or_album'
        "
      >
        <Tag>拍照或者相册</Tag>
      </div>
      <div v-else-if="row.type === MsgType.Event && row.event === 'pic_weixin'">
        <Tag>微信相册</Tag>
      </div>
      <div
        v-else-if="
          row.type === MsgType.Event && row.event === 'location_select'
        "
      >
        <Tag>选择地理位置</Tag>
      </div>
      <div v-else-if="row.type === MsgType.Event">
        <Tag color="error">未知事件类型</Tag>
      </div>

      <div v-else-if="row.type === MsgType.Text">{{ row.content }}</div>
      <div v-else-if="row.type === MsgType.Voice">
        <WxVoicePlayer
          :url="row.mediaUrl || ''"
          :content="row.recognition || ''"
        />
      </div>
      <div v-else-if="row.type === MsgType.Image">
        <a :href="row.mediaUrl" target="_blank">
          <Image :src="row.mediaUrl" :width="100" :preview="false" />
        </a>
      </div>
      <div v-else-if="row.type === MsgType.Video || row.type === 'shortvideo'">
        <WxVideoPlayer :url="row.mediaUrl || ''" class="mt-2" />
      </div>
      <div v-else-if="row.type === MsgType.Link">
        <Tag>链接</Tag>
        ：
        <a :href="row.url" target="_blank">{{ row.title }}</a>
      </div>
      <div v-else-if="row.type === MsgType.Location">
        <WxLocation
          :label="row.label || ''"
          :location-y="row.locationY || 0"
          :location-x="row.locationX || 0"
        />
      </div>
      <div v-else-if="row.type === MsgType.Music">
        <WxMusic
          :title="row.title"
          :description="row.description"
          :thumb-media-url="row.thumbMediaUrl || ''"
          :music-url="row.musicUrl"
          :hq-music-url="row.hqMusicUrl"
        />
      </div>
      <div v-else-if="row.type === MsgType.News">
        <WxNews :articles="row.articles" />
      </div>
      <div v-else>
        <Tag color="error">未知消息类型</Tag>
      </div>
    </template>

    <template #actions="{ row }">
      <Button type="link" @click="emit('send', row.userId || 0)"> 消息 </Button>
    </template>
  </Grid>
</template>
