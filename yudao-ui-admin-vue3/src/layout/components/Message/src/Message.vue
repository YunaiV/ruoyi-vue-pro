<script setup lang="ts">
import { ref } from 'vue'
import { ElTabs, ElTabPane, ElPopover, ElBadge } from 'element-plus'

const activeName = ref('notice')

const noticeList = ref([
  { id: 1, title: '版本升级1', date: '2022-12-12 10:00:00' },
  { id: 2, title: '版本升级2', date: '2022-12-12 10:00:00' },
  { id: 3, title: '版本升级3', date: '2022-12-12 10:00:00' },
  { id: 4, title: '版本升级4', date: '2022-12-12 10:00:00' },
  { id: 5, title: '版本升级5', date: '2022-12-12 10:00:00' }
])
const messageList = ref([
  { id: 1, title: '加班1', date: '2022-12-12 10:00:00' },
  { id: 2, title: '加班2', date: '2022-12-12 10:00:00' },
  { id: 3, title: '加班3', date: '2022-12-12 10:00:00' },
  { id: 4, title: '加班4', date: '2022-12-12 10:00:00' },
  { id: 5, title: '加班5', date: '2022-12-12 10:00:00' }
])
const needList = ref([
  { id: 1, title: '审批1', date: '2022-12-12 10:00:00' },
  { id: 2, title: '审批2', date: '2022-12-12 10:00:00' },
  { id: 3, title: '审批3', date: '2022-12-12 10:00:00' },
  { id: 4, title: '审批4', date: '2022-12-12 10:00:00' },
  { id: 5, title: '审批5', date: '2022-12-12 10:00:00' }
])
</script>
<template>
  <div class="message">
    <ElPopover placement="bottom" :width="310" trigger="click">
      <template #reference>
        <ElBadge :value="noticeList.length" class="item">
          <Icon icon="ep:bell" :size="18" class="cursor-pointer" />
        </ElBadge>
      </template>
      <ElTabs v-model="activeName">
        <ElTabPane label="通知(5)" name="notice">
          <div class="message-list">
            <template v-for="item in noticeList" :key="item.id">
              <div class="message-item">
                <img src="@/assets/imgs/avatar.gif" alt="" class="message-icon" />
                <div class="message-content">
                  <span class="message-title">{{ item.title }}</span>
                  <span class="message-date">{{ item.date }}</span>
                </div>
              </div>
            </template>
          </div>
        </ElTabPane>
        <ElTabPane label="消息(0)" name="message">
          <div class="message-list">
            <template v-for="item in messageList" :key="item.id">
              <div class="message-item">
                <img src="@/assets/imgs/avatar.gif" alt="" class="message-icon" />
                <div class="message-content">
                  <span class="message-title">{{ item.title }}</span>
                  <span class="message-date">{{ item.date }}</span>
                </div>
              </div>
            </template>
          </div>
        </ElTabPane>
        <ElTabPane label="代办(0)" name="need">
          <div class="message-list">
            <template v-for="item in needList" :key="item.id">
              <div class="message-item">
                <img src="@/assets/imgs/avatar.gif" alt="" class="message-icon" />
                <div class="message-content">
                  <span class="message-title">{{ item.title }}</span>
                  <span class="message-date">{{ item.date }}</span>
                </div>
              </div>
            </template>
          </div>
        </ElTabPane>
      </ElTabs>
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
