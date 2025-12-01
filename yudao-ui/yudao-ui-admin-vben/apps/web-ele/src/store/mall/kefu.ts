import type { MallKefuConversationApi } from '#/api/mall/promotion/kefu/conversation';
import type { MallKefuMessageApi } from '#/api/mall/promotion/kefu/message';

import { isEmpty } from '@vben/utils';

import { acceptHMRUpdate, defineStore } from 'pinia';

import * as KeFuConversationApi from '#/api/mall/promotion/kefu/conversation';

interface MallKefuInfoVO {
  conversationList: MallKefuConversationApi.Conversation[]; // 会话列表
  conversationMessageList: Map<number, MallKefuMessageApi.Message[]>; // 会话消息
}

export const useMallKefuStore = defineStore('mall-kefu', {
  state: (): MallKefuInfoVO => ({
    conversationList: [],
    conversationMessageList: new Map<number, MallKefuMessageApi.Message[]>(), // key 会话，value 会话消息列表
  }),
  getters: {
    getConversationList(): MallKefuConversationApi.Conversation[] {
      return this.conversationList;
    },
    getConversationMessageList(): (
      conversationId: number,
    ) => MallKefuMessageApi.Message[] | undefined {
      return (conversationId: number) =>
        this.conversationMessageList.get(conversationId);
    },
  },
  actions: {
    // ======================= 会话消息相关 =======================
    /** 缓存历史消息 */
    saveMessageList(
      conversationId: number,
      messageList: MallKefuMessageApi.Message[],
    ) {
      this.conversationMessageList.set(conversationId, messageList);
    },

    // ======================= 会话相关 =======================
    /** 加载会话缓存列表 */
    async setConversationList() {
      // TODO @jave：idea linter 告警，修复下；
      // TODO @jave：不使用 KeFuConversationApi.，直接用 getConversationList
      this.conversationList = await KeFuConversationApi.getConversationList();
      this.conversationSort();
    },
    /** 更新会话缓存已读 */
    async updateConversationStatus(conversationId: number) {
      if (isEmpty(this.conversationList)) {
        return;
      }
      const conversation = this.conversationList.find(
        (item) => item.id === conversationId,
      );
      conversation && (conversation.adminUnreadMessageCount = 0);
    },
    /** 更新会话缓存 */
    async updateConversation(conversationId: number) {
      if (isEmpty(this.conversationList)) {
        return;
      }

      const conversation =
        await KeFuConversationApi.getConversation(conversationId);
      this.deleteConversation(conversationId);
      conversation && this.conversationList.push(conversation);
      this.conversationSort();
    },
    /** 删除会话缓存 */
    deleteConversation(conversationId: number) {
      const index = this.conversationList.findIndex(
        (item) => item.id === conversationId,
      );
      // 存在则删除
      if (index !== -1) {
        this.conversationList.splice(index, 1);
      }
    },
    conversationSort() {
      // 按置顶属性和最后消息时间排序
      this.conversationList.toSorted((a, b) => {
        // 按照置顶排序，置顶的会在前面
        if (a.adminPinned !== b.adminPinned) {
          return a.adminPinned ? -1 : 1;
        }
        // 按照最后消息时间排序，最近的会在前面
        return (
          (b.lastMessageTime as unknown as number) -
          (a.lastMessageTime as unknown as number)
        );
      });
    },
  },
});

// 解决热更新问题
const hot = import.meta.hot;
if (hot) {
  hot.accept(acceptHMRUpdate(useMallKefuStore, hot));
}
