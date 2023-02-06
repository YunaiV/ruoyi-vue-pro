<script setup lang="ts">
import dayjs from 'dayjs'
import * as NotifyMessageApi from '@/api/system/notify/message'

const { push } = useRouter()
const activeName = ref('notice')
const unreadCount = ref(0) // 未读消息数量
const list = ref<any[]>([]) // 消息列表

// 获得消息列表
const getList = async () => {
  list.value = await NotifyMessageApi.getUnreadNotifyMessageListApi()
  // 强制设置 unreadCount 为 0，避免小红点因为轮询太慢，不消除
  unreadCount.value = 0
}

// 获得未读消息数
const getUnreadCount = async () => {
  NotifyMessageApi.getUnreadNotifyMessageCountApi().then((data) => {
    unreadCount.value = data
  })
}

// 跳转我的站内信
const goMyList = () => {
  push({
    name: 'MyNotifyMessage'
  })
}

// ========== 初始化 =========
onMounted(() => {
  // 首次加载小红点
  getUnreadCount()
  // 轮询刷新小红点
  setInterval(() => {
    getUnreadCount()
  }, 1000 * 60 * 2)
})
</script>
<template>
  <div class="message">
    <ElPopover placement="bottom" :width="400" trigger="click">
      <template #reference>
        <ElBadge :is-dot="unreadCount > 0" class="item">
          <Icon icon="ep:bell" :size="18" class="cursor-pointer" @click="getList" />
        </ElBadge>
      </template>
      <ElTabs v-model="activeName">
        <ElTabPane label="我的站内信" name="notice">
          <div class="message-list">
            <template v-for="item in list" :key="item.id">
              <div class="message-item">
                <img src="@/assets/imgs/avatar.gif" alt="" class="message-icon" />
                <div class="message-content">
                  <span class="message-title">
                    {{ item.templateNickname }}：{{ item.templateContent }}
                  </span>
                  <span class="message-date">
                    {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
                  </span>
                </div>
              </div>
            </template>
          </div>
        </ElTabPane>
      </ElTabs>
      <!-- 更多 -->
      <div style="text-align: right; margin-top: 10px">
        <XButton type="primary" preIcon="ep:view" title="查看全部" @click="goMyList" />
      </div>
    </ElPopover>
  </div>
</template>
<style scoped lang="scss">
.message-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 260px;
  line-height: 45px;
}
.message-list {
  display: flex;
  flex-direction: column;
  .message-item {
    display: flex;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px solid var(--el-border-color-light);
    &:last-child {
      border: none;
    }
    .message-icon {
      width: 40px;
      height: 40px;
      margin: 0 20px 0 5px;
    }
    .message-content {
      display: flex;
      flex-direction: column;
      .message-title {
        margin-bottom: 5px;
      }
      .message-date {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}
</style>
