<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="queryParams.accountId" placeholder="请选择公众号">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <!-- 等待处理 -->
      <el-form-item label="消息类型" prop="msgType">
        <el-select v-model="queryParams.msgType" placeholder="请选择消息类型" clearable size="small">
          <el-option label="请选择字典生成" value=""/>
        </el-select>
      </el-form-item>
      <el-form-item label="用户标识" prop="openid">
        <el-input v-model="queryParams.openid" placeholder="请输入用户标识" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
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
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="消息类型" align="center" prop="type" width="80"/>
      <el-table-column label="用户标识" align="center" prop="openid" width="300" />
      <!-- TODO 芋艿：发送/接收 -->
      <el-table-column label="内容" prop="content">
        <template slot-scope="scope">
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
          <div v-else-if="scope.row.type === 'event' && scope.row.event === 'scancode_waitmsg'"> <!-- TODO 芋艿：需要测试下 -->
            <el-tag size="mini">扫码结果</el-tag>【{{ scope.row.eventKey }}】
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
          <div v-else>
            <el-tag type="danger" size="mini">未知消息类型</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
<!--          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"-->
<!--                     v-hasPermi="['mp:message:update']">修改-->
<!--          </el-button>-->
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <el-dialog title="用户消息" :visible.sync="open" width="40%">
      <wx-msg wxUserId="1" v-if="true" />
    </el-dialog>

  </div>
</template>

<script>
import { getMessagePage } from "@/api/mp/message";
import WxVideoPlayer from '@/views/mp/components/wx-video-play/main.vue';
import WxVoicePlayer from '@/views/mp/components/wx-voice-play/main.vue';
import WxMsg from '@/views/mp/components/wx-msg/main.vue';

export default {
  name: "WxFansMsg",
  components: {
    WxVideoPlayer,
    WxVoicePlayer,
    WxMsg
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
      // 粉丝消息表 列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: true,
      // 查询参数
      dateRangeCreateTime: [],
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        openid: null,
        nickname: null,
        headimgUrl: null,
        wxAccountId: null,
        msgType: null,
        content: null,
        resContent: null,
        isRes: null,
        mediaId: null,
        picUrl: null,
        picPath: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {},

      // 公众号账号列表
      accounts: []
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getMessagePage(params).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        openid: undefined,
        nickname: undefined,
        headimgUrl: undefined,
        wxAccountId: undefined,
        msgType: undefined,
        content: undefined,
        resContent: undefined,
        isRes: undefined,
        mediaId: undefined,
        picUrl: undefined,
        picPath: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
  }
};
</script>
