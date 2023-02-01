<!--
MIT License

Copyright (c) 2020 www.joolun.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
  芋道源码：
  ① 移除 avue 框架，使用 element-ui 重写
  ② 重写代码，保持和现有项目保持一致
-->
<template>
  <div class="app-container">
    <doc-alert title="自动回复" url="https://doc.iocoder.cn/mp/auto-reply/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="queryParams.accountId" placeholder="请选择公众号">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- tab 切换 -->
    <el-tabs v-model="type" @tab-click="handleClick">
      <!-- 操作工具栏 -->
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                     v-hasPermi="['mp:auto-reply:create']" v-if="type !== '1' || list.length <= 0">新增
          </el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
      </el-row>
      <!-- tab 项 -->
      <el-tab-pane name="1">
        <span slot="label"><i class="el-icon-star-off"></i> 关注时回复</span>
      </el-tab-pane>
      <el-tab-pane name="2">
        <span slot="label"><i class="el-icon-chat-line-round"></i> 消息回复</span>
      </el-tab-pane>
      <el-tab-pane name="3">
        <span slot="label"><i class="el-icon-news"></i> 关键词回复</span>
      </el-tab-pane>
    </el-tabs>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="请求消息类型" align="center" prop="requestMessageType" v-if="type === '2'" />
      <el-table-column label="关键词" align="center" prop="requestKeyword" v-if="type === '3'" />
      <el-table-column label="匹配类型" align="center" prop="requestMatch" v-if="type === '3'">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.MP_AUTO_REPLY_REQUEST_MATCH" :value="scope.row.requestMatch"/>
        </template>
      </el-table-column>
      <el-table-column label="回复消息类型" align="center">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.MP_MESSAGE_TYPE" :value="scope.row.responseMessageType"/>
        </template>
      </el-table-column>
      <el-table-column label="回复内容" align="center">
        <template v-slot="scope">
          <div v-if="scope.row.responseMessageType === 'text'">{{ scope.row.responseContent }}</div>
          <div v-else-if="scope.row.responseMessageType === 'voice'">
            <wx-voice-player :url="scope.row.responseMediaUrl" />
          </div>
          <div v-else-if="scope.row.responseMessageType === 'image'">
            <a target="_blank" :href="scope.row.responseMediaUrl">
              <img :src="scope.row.responseMediaUrl" style="width: 100px">
            </a>
          </div>
          <div v-else-if="scope.row.responseMessageType === 'video' || scope.row.responseMessageType === 'shortvideo'">
            <wx-video-player :url="scope.row.responseMediaUrl" style="margin-top: 10px" />
          </div>
          <div v-else-if="scope.row.responseMessageType === 'news'">
            <wx-news :articles="scope.row.responseArticles" />
          </div>
          <div v-else-if="scope.row.responseMessageType === 'music'">
            <wx-music :title="scope.row.responseTitle" :description="scope.row.responseDescription"
                      :thumb-media-url="scope.row.responseThumbMediaUrl"
                      :music-url="scope.row.responseMusicUrl" :hq-music-url="scope.row.responseHqMusicUrl" />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['mp:auto-reply:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['mp:auto-reply:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加或修改自动回复的对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="消息类型" prop="requestMessageType" v-if="type === '2'">
          <el-select v-model="form.requestMessageType" placeholder="请选择">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MP_MESSAGE_TYPE)"
                       :key="dict.value" :label="dict.label" :value="dict.value"
                       v-if="requestMessageTypes.includes(dict.value)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="匹配类型" prop="requestMatch" v-if="type === '3'">
          <el-select v-model="form.requestMatch" placeholder="请选择匹配类型" clearable size="small">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.MP_AUTO_REPLY_REQUEST_MATCH)"
                       :key="dict.value" :label="dict.label" :value="parseInt(dict.value)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键词" prop="requestKeyword" v-if="type === '3'">
          <el-input v-model="form.requestKeyword" placeholder="请输入内容" clearable />
        </el-form-item>
        <el-form-item label="回复消息">
          <wx-reply-select :objData="objData" v-if="hackResetWxReplySelect" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </span>
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
import WxReplySelect from '@/views/mp/components/wx-reply/main.vue'
import { getSimpleAccounts } from "@/api/mp/account";
import { createAutoReply, deleteAutoReply, getAutoReply, getAutoReplyPage, updateAutoReply } from "@/api/mp/autoReply";

export default {
  name: 'mpAutoReply',
  components: {
    WxVideoPlayer,
    WxVoicePlayer,
    WxMsg,
    WxLocation,
    WxMusic,
    WxNews,
    WxReplySelect
  },
  data() {
    return {
      // tab 类型（1、关注时回复；2、消息回复；3、关键词回复）
      type: '3',
      // 允许选择的请求消息类型
      requestMessageTypes: ['text', 'image', 'voice', 'video', 'shortvideo', 'location', 'link'],
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 自动回复列表
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        accountId: undefined,
      },

      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 回复消息
      objData: {
        type : 'text'
      },
      // 表单校验
      rules: {
        requestKeyword: [{ required: true, message: "请求的关键字不能为空", trigger: "blur" }],
        requestMatch: [{ required: true, message: "请求的关键字的匹配不能为空", trigger: "blur" }],
      },
      hackResetWxReplySelect: false, // 重置 WxReplySelect 组件，解决无法清除的问题

      // 公众号账号列表
      accounts: []
    }
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
        this.$message.error('未选中公众号，无法查询自动回复')
        return false
      }

      this.loading = false
      // 处理查询参数
      let params = {
        ...this.queryParams,
        type: this.type
      }
      // 执行查询
      getAutoReplyPage(params).then(response => {
        this.list = response.data.list
        this.total = response.data.total
        this.loading = false
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryForm')
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.queryParams.accountId = this.accounts[0].id;
      }
      this.handleQuery()
    },
    handleClick(tab, event) {
      this.type = tab.name
      this.handleQuery()
    },

    /** 新增按钮操作 */
    handleAdd(){
      this.reset();
      this.resetEditor();
      // 打开表单，并设置初始化
      this.open = true
      this.title = '新增自动回复';
      this.objData = {
        type : 'text',
        accountId: this.queryParams.accountId,
      }
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      this.resetEditor();
      const id = row.id;
      getAutoReply(id).then(response => {
        // 设置属性
        this.form = {...response.data}
        this.$delete(this.form, 'responseMessageType');
        this.$delete(this.form, 'responseContent');
        this.$delete(this.form, 'responseMediaId');
        this.$delete(this.form, 'responseMediaUrl');
        this.$delete(this.form, 'responseDescription');
        this.$delete(this.form, 'responseArticles');
        this.objData = {
          type: response.data.responseMessageType,
          accountId: this.queryParams.accountId,
          content: response.data.responseContent,
          mediaId: response.data.responseMediaId,
          url: response.data.responseMediaUrl,
          title: response.data.responseTitle,
          description: response.data.responseDescription,
          thumbMediaId: response.data.responseThumbMediaId,
          thumbMediaUrl: response.data.responseThumbMediaUrl,
          articles: response.data.responseArticles,
          musicUrl: response.data.responseMusicUrl,
          hqMusicUrl: response.data.responseHqMusicUrl,
        }

        // 打开表单
        this.open = true
        this.title = '修改自动回复';
      })
    },
    handleSubmit() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 处理回复消息
        const form = {...this.form};
        form.responseMessageType = this.objData.type;
        form.responseContent = this.objData.content;
        form.responseMediaId = this.objData.mediaId;
        form.responseMediaUrl = this.objData.url;
        form.responseTitle = this.objData.title;
        form.responseDescription = this.objData.description;
        form.responseThumbMediaId = this.objData.thumbMediaId;
        form.responseThumbMediaUrl = this.objData.thumbMediaUrl;
        form.responseArticles = this.objData.articles;
        form.responseMusicUrl = this.objData.musicUrl;
        form.responseHqMusicUrl = this.objData.hqMusicUrl;

        if (this.form.id !== undefined) {
          updateAutoReply(form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        } else {
          createAutoReply(form).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        accountId: this.queryParams.accountId,
        type: this.type,
        requestKeyword: undefined,
        requestMatch: this.type === '3' ? 1 : undefined,
        requestMessageType: undefined,
      };
      this.resetForm("form");
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单 Editor 重置
    resetEditor() {
      this.hackResetWxReplySelect = false // 销毁组件
      this.$nextTick(() => {
        this.hackResetWxReplySelect = true // 重建组件
      })
    },
    handleDelete: function(row) {
      const ids = row.id;
      this.$modal.confirm('是否确认删除此数据?').then(function() {
        return deleteAutoReply(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
  }
}
</script>
