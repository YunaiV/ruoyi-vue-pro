<template>
  <view class="chat-box">
    <!--  消息渲染  -->
    <view class="message-item ss-flex-col scroll-item">
      <view class="ss-flex ss-row-center ss-col-center">
        <!-- 系统消息 -->
        <view
          v-if="message.contentType === KeFuMessageContentTypeEnum.SYSTEM"
          class="system-message"
        >
          {{ message.content }}
        </view>
        <!-- 日期 - 移到消息内容上方显示 -->
        <view
          v-if="
            message.contentType !== KeFuMessageContentTypeEnum.SYSTEM &&
            showTime(message, messageIndex)
          "
          class="date-message"
        >
          {{ formatDate(message.createTime) }}
        </view>
      </view>

      <!-- 消息体渲染管理员消息和用户消息并左右展示  -->
      <view
        v-if="message.contentType !== KeFuMessageContentTypeEnum.SYSTEM"
        class="ss-flex ss-col-top"
        :class="[
          message.senderType === UserTypeEnum.ADMIN
            ? `ss-row-left`
            : message.senderType === UserTypeEnum.MEMBER
            ? `ss-row-right`
            : '',
        ]"
      >
        <!-- 客服头像 -->
        <image
          v-show="message.senderType === UserTypeEnum.ADMIN"
          class="chat-avatar ss-m-r-24"
          :src="sheep.$url.cdn(message.senderAvatar) || sheep.$url.static('')"
          mode="aspectFill"
          lazy-load
        />
        <!-- 内容 -->
        <template v-if="message.contentType === KeFuMessageContentTypeEnum.TEXT">
          <view class="message-box" :class="{ admin: message.senderType === UserTypeEnum.ADMIN }">
            <mp-html :content="processedContent" :domain="sheep.$url.cdn('')" lazy-load />
          </view>
        </template>
        <template v-if="message.contentType === KeFuMessageContentTypeEnum.IMAGE">
          <view
            class="message-box"
            :class="{ admin: message.senderType === UserTypeEnum.ADMIN }"
            :style="{ width: '200rpx' }"
          >
            <su-image
              class="message-img"
              isPreview
              :previewList="[sheep.$url.cdn(getMessageContent(message).picUrl || message.content)]"
              :current="0"
              :src="sheep.$url.cdn(getMessageContent(message).picUrl || message.content)"
              :height="200"
              :width="200"
              mode="aspectFill"
            />
          </view>
        </template>
        <template v-if="message.contentType === KeFuMessageContentTypeEnum.PRODUCT">
          <div class="ss-m-b-10">
            <GoodsItem
              :goodsData="getMessageContent(message)"
              @tap="
                sheep.$router.go('/pages/goods/index', { id: getMessageContent(message).spuId })
              "
            />
          </div>
        </template>
        <template v-if="message.contentType === KeFuMessageContentTypeEnum.ORDER">
          <OrderItem
            :orderData="getMessageContent(message)"
            @tap="sheep.$router.go('/pages/order/detail', { id: getMessageContent(message).id })"
          />
        </template>
        <!-- user头像 -->
        <image
          v-if="message.senderType === UserTypeEnum.MEMBER"
          class="chat-avatar ss-m-l-24"
          :src="
            sheep.$url.cdn(userInfo.avatar) ||
            sheep.$url.static('/static/img/shop/chat/default.png')
          "
          mode="aspectFill"
        >
        </image>
      </view>
    </view>
  </view>
</template>

<script setup>
  import { computed, unref } from 'vue';
  import dayjs from 'dayjs';
  import { KeFuMessageContentTypeEnum, UserTypeEnum } from '@/pages/chat/util/constants';
  import { emojiList } from '@/pages/chat/util/emoji';
  import sheep from '@/sheep';
  import { formatDate, jsonParse } from '@/sheep/helper/utils';
  import GoodsItem from '@/pages/chat/components/goods.vue';
  import OrderItem from '@/pages/chat/components/order.vue';

  const props = defineProps({
    // 消息
    message: {
      type: Object,
      default: () => ({}),
    },
    // 消息索引
    messageIndex: {
      type: Number,
      default: 0,
    },
    // 消息列表
    messageList: {
      type: Array,
      default: () => [],
    },
  });

  const getMessageContent = computed(() => (item) => jsonParse(item.content)); // 解析消息内容
  const userInfo = computed(() => sheep.$store('user').userInfo);

  //======================= 工具 =======================

  const showTime = computed(() => (item, index) => {
    if (unref(props.messageList)[index + 1]) {
      let dateString = dayjs(unref(props.messageList)[index + 1].createTime).fromNow();
      return dateString !== dayjs(unref(item).createTime).fromNow();
    }
    return false;
  });

  // 缓存表情映射
  const emojiMap = computed(() => {
    const map = new Map();
    emojiList.forEach((emoji) => {
      map.set(emoji.name, emoji.file);
    });
    return map;
  });

  // 处理表情 - 进行缓存优化
  function replaceEmoji(data) {
    let newData = data;
    if (typeof newData !== 'object') {
      let reg = /\[(.+?)]/g; // [] 中括号
      let zhEmojiName = newData.match(reg);
      if (zhEmojiName) {
        zhEmojiName.forEach((item) => {
          const emojiFile = emojiMap.value.get(item) || '';
          if (emojiFile) {
            newData = newData.replace(
              item,
              `<img class="chat-img" style="width: 24px;height: 24px;margin: 0 3px;vertical-align: middle;" src="${sheep.$url.cdn(
                '/static/img/chat/emoji/' + emojiFile,
              )}"/>`,
            );
          }
        });
      }
    }
    return newData;
  }

  // 预处理内容，避免重复计算
  const processedContent = computed(() => {
    if (props.message.contentType === KeFuMessageContentTypeEnum.TEXT) {
      return replaceEmoji(getMessageContent.value(props.message).text || props.message.content);
    }
    return props.message.content;
  });
</script>

<style scoped lang="scss">
  .message-item {
    margin-bottom: 10rpx;
  }

  .date-message,
  .system-message {
    width: fit-content;
    border-radius: 12rpx;
    padding: 8rpx 16rpx;
    margin-bottom: 16rpx;
    background-color: var(--ui-BG-3);
    color: #999;
    font-size: 24rpx;
  }

  .chat-avatar {
    width: 70rpx;
    height: 70rpx;
    border-radius: 50%;
  }

  .send-status {
    color: #333;
    height: 80rpx;
    margin-right: 8rpx;
    display: flex;
    align-items: center;

    .loading {
      width: 32rpx;
      height: 32rpx;
      -webkit-animation: rotating 2s linear infinite;
      animation: rotating 2s linear infinite;

      @-webkit-keyframes rotating {
        0% {
          transform: rotateZ(0);
        }

        100% {
          transform: rotateZ(360deg);
        }
      }

      @keyframes rotating {
        0% {
          transform: rotateZ(0);
        }

        100% {
          transform: rotateZ(360deg);
        }
      }
    }

    .warning {
      width: 32rpx;
      height: 32rpx;
      color: #ff3000;
    }
  }

  .message-box {
    max-width: 50%;
    font-size: 16px;
    white-space: normal;
    word-break: break-all;
    word-wrap: break-word;
    padding: 20rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    margin-top: 3px;
    margin-bottom: 9px;
    border-top-left-radius: 10px;
    border-bottom-right-radius: 10px;
    border-bottom-left-radius: 10px;
    &.admin {
      background: #fff;
      color: #333;
      margin-top: 3px;
      margin-bottom: 9px;
      border-radius: 0 10px 10px 10px;
    }

    :deep() {
      .imgred {
        width: 100%;
      }

      .imgred,
      img {
        width: 100%;
      }
    }
  }

  :deep() {
    .goods,
    .order {
      max-width: 500rpx;
    }
  }

  .message-img {
    width: 100px;
    height: 100px;
    border-radius: 6rpx;
  }

  .error-img {
    width: 400rpx;
    height: 400rpx;
  }
</style>
