<script lang="ts" setup>
import { Tag } from 'ant-design-vue';

defineOptions({ name: 'MsgEvent' });

defineProps<{
  item: any;
}>();
</script>

<template>
  <div>
    <div v-if="item.event === 'subscribe'">
      <Tag color="success">关注</Tag>
    </div>
    <div v-else-if="item.event === 'unsubscribe'">
      <Tag color="error">取消关注</Tag>
    </div>
    <div v-else-if="item.event === 'CLICK'">
      <Tag>点击菜单</Tag>
      【{{ item.eventKey }}】
    </div>
    <div v-else-if="item.event === 'VIEW'">
      <Tag>点击菜单链接</Tag>
      【{{ item.eventKey }}】
    </div>
    <div v-else-if="item.event === 'scancode_waitmsg'">
      <Tag>扫码结果</Tag>
      【{{ item.eventKey }}】
    </div>
    <div v-else-if="item.event === 'scancode_push'">
      <Tag>扫码结果</Tag>
      【{{ item.eventKey }}】
    </div>
    <div v-else-if="item.event === 'pic_sysphoto'">
      <Tag>系统拍照发图</Tag>
    </div>
    <div v-else-if="item.event === 'pic_photo_or_album'">
      <Tag>拍照或者相册</Tag>
    </div>
    <div v-else-if="item.event === 'pic_weixin'">
      <Tag>微信相册</Tag>
    </div>
    <div v-else-if="item.event === 'location_select'">
      <Tag>选择地理位置</Tag>
    </div>
    <div v-else>
      <Tag color="error">未知事件类型</Tag>
    </div>
  </div>
</template>
