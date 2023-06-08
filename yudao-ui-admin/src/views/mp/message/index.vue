<template>
  <div class="app-container">
    <doc-alert title="公众号消息" url="https://doc.iocoder.cn/mp/message/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="queryParams.accountId" placeholder="请选择公众号">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <!-- TODO 等待处理 -->
      <el-form-item label="消息类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择消息类型" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MP_MESSAGE_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="用户标识" prop="openid">
        <el-input v-model="queryParams.openid" placeholder="请输入用户标识" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="发送时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="消息类型" align="center" prop="type" width="80"/>
      <el-table-column label="发送方" align="center" prop="sendFrom" width="80">
        <template v-slot="scope">
          <el-tag v-if="scope.row.sendFrom === 1" type="success">粉丝</el-tag>
          <el-tag v-else type="info">公众号</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用户标识" align="center" prop="openid" width="300" />
      <el-table-column label="内容" prop="content">
        <template v-slot="scope">
          <!-- 【事件】区域 -->
          <div v-if="scope.row.type === 'event' && scope.row.event === 'subscribe'">
            <el-tag type="success" size="mini">关注</el-tag>
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'unsubscribe'">
            <el-tag type="danger" size="mini">取消关注</el-tag>
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'CLICK'">
            <el-tag size="mini">点击菜单</el-tag>【{{ scope.row.eventKey }}】
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'VIEW'">
            <el-tag size="mini">点击菜单链接</el-tag>【{{ scope.row.eventKey }}】
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'scancode_waitmsg'">
            <el-tag size="mini">扫码结果</el-tag>【{{ scope.row.eventKey }}】
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'scancode_push'">
            <el-tag size="mini">扫码结果</el-tag>【{{ scope.row.eventKey }}】
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'pic_sysphoto'">
            <el-tag size="mini">系统拍照发图</el-tag>
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'pic_photo_or_album'">
            <el-tag size="mini">拍照或者相册</el-tag>
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'pic_weixin'">
            <el-tag size="mini">微信相册</el-tag>
          </div>
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'location_select'">
            <el-tag size="mini">选择地理位置</el-tag>
          </div>
          <div v-else-if="scope.row.type === 'event'">
            <el-tag type="danger" size="mini">未知事件类型</el-tag>
          </div>
          <!-- 【消息】区域 -->
          <div v-else-if="scope.row.type === 'text'">{{ scope.row.content }}</div>
          <div v-else-if="scope.row.type === 'voice'">
            <wx-voice-player :url="scope.row.mediaUrl" :content="scope.row.recognition" />
          </div>
          <div v-else-if="scope.row.type === 'image'">
            <a target="_blank" :href="scope.row.mediaUrl">
              <img :src="scope.row.mediaUrl" style="width: 100px">
            </a>
          </div>
          <div v-else-if="scope.row.type === 'video' || scope.row.type === 'shortvideo'">
            <wx-video-player :url="scope.row.mediaUrl" style="margin-top: 10px" />
          </div>
          <div v-else-if="scope.row.type === 'link'">
            <el-tag size="mini">链接</el-tag>：
            <a :href="scope.row.url" target="_blank">{{scope.row.title}}</a>
          </div>
          <div v-else-if="scope.row.type === 'location'">
            <wx-location :label="scope.row.label" :location-y="scope.row.locationY" :location-x="scope.row.locationX" />
          </div>
          <div v-else-if="scope.row.type === 'music'">
            <wx-music :title="scope.row.title" :description="scope.row.description" :thumb-media-url="scope.row.thumbMediaUrl"
                      :music-url="scope.row.musicUrl" :hq-music-url="scope.row.hqMusicUrl" />
          </div>
          <div v-else-if="scope.row.type === 'news'">
            <wx-news :articles="scope.row.articles" />
          </div>
          <div v-else>
            <el-tag type="danger" size="mini">未知消息类型</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleSend(scope.row)"
                     v-hasPermi="['mp:message:send']">消息
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 发送消息的弹窗 -->
    <el-dialog title="粉丝消息列表" :visible.sync="open" width="50%">
      <wx-msg :user-id="userId" v-if="open" />
    </el-dialog>

  </div>
</template>

<script>
import WxVideoPlayer from '@/views/mp/components/wx-video-play/main.vue';
import WxVoicePlayer from '@/views/mp/components/wx-voice-play/main.vue';
import WxMsg from '@/views/mp/components/wx-msg/main.vue';
import WxLocation from '@/views/mp/components/wx-location/main.vue';
import WxMusic from '@/views/mp/components/wx-music/main.vue';
import WxNews from '@/views/mp/components/wx-news/main.vue';
import { getMessagePage } from "@/api/mp/message";
import { getSimpleAccounts } from "@/api/mp/account";

export default {
  name: "MpMessage",
  components: {
    WxVideoPlayer,
    WxVoicePlayer,
    WxMsg,
    WxLocation,
    WxMusic,
    WxNews
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 粉丝消息列表
      list: [],
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        openid: null,
        accountId: null,
        type: null,
        createTime: []
      },
      // 操作的用户编号
      userId: 0,

      // 公众号账号列表
      accounts: []
    };
  },
  created() {
    getSimpleAccounts().then(response => {
      this.accounts = response.data;
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.queryParams.accountId = this.accounts[0].id;
      }
      // 加载数据
      this.getList();
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      // 如果没有选中公众号账号，则进行提示。
      if (!this.queryParams.accountId) {
        this.$message.error('未选中公众号，无法查询消息')
        return false
      }

      this.loading = true;
      // 执行查询
      getMessagePage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.queryParams.accountId = this.accounts[0].id;
      }
      this.handleQuery();
    },
    handleSend(row) {
      this.userId = row.userId;
      this.open = true;
    },
  }
};
</script>
