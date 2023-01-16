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
-->
<template>
  <div class="app-container">
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
                     v-hasPermi="['mp:auto-reply:create']" v-if="list.length <= 0">新增
          </el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>
      <!-- 列表 -->
      <el-tab-pane name="1">
        <span slot="label"><i class="el-icon-star-off"></i> 关注时回复</span>
        <el-table v-loading="loading" :data="list">
          <el-table-column label="回复消息类型" align="center" prop="responseMessageType"/>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                         v-hasPermi="['mp:auto-reply:update']">修改
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                         v-hasPermi="['mp:auto-reply:delete']">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane name="2">
        <span slot="label"><i class="el-icon-chat-line-round"></i> 消息回复</span>
        <el-table v-loading="loading" :data="list">
          <el-table-column label="请求消息类型" align="center" prop="requestMessageType"/>
          <el-table-column label="回复消息类型" align="center" prop="responseMessageType"/>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                         v-hasPermi="['mp:auto-reply:update']">修改
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                         v-hasPermi="['mp:auto-reply:delete']">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane name="3">
        <span slot="label"><i class="el-icon-news"></i> 关键词回复</span>
        <el-table v-loading="loading" :data="list">
          <el-table-column label="关键词" align="center" prop="requestKeyword"/>
          <el-table-column label="匹配类型" align="center" prop="requestMatch"/>
          <el-table-column label="回复消息类型" align="center" prop="responseMessageType"/>
          <el-table-column label="创建时间" align="center" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                         v-hasPermi="['mp:auto-reply:update']">修改
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                         v-hasPermi="['mp:auto-reply:delete']">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="handleType === 'add' ? '新增回复消息' : '修改回复消息'" :visible.sync="dialog1Visible" width="50%">
      <el-form label-width="100px">
        <el-form-item label="请求消息类型" v-if="type == '2'">
          <el-select v-model="objData.reqType" placeholder="请选择">
            <el-option
              v-for="item in dictData.get('wx_req_type')"
              :key="item.value"
              :label="item.label"
              :value="item.value"
              :disabled="item.disabled"
              v-if="item.value !== 'event'">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="匹配类型" v-if="type === '3'">
          <el-select v-model="objData.repMate" placeholder="请选择" style="width: 100px">
            <el-option
              v-for="item in dictData.get('wx_rep_mate')"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="关键词" v-if="type === '3'">
          <el-input placeholder="请输入内容" v-model="objData.reqKey" clearable> </el-input>
        </el-form-item>
        <el-form-item label="回复消息">
          <WxReplySelect :objData="objData" v-if="hackResetWxReplySelect"></WxReplySelect>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialog1Visible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
// import { getPage, getObj, addObj, putObj, delObj } from '@/api/wxmp/wxautoreply'
// import { tableOption1, tableOption2, tableOption3 } from '@/const/crud/wxmp/wxautoreply'
import WxReplySelect from '@/views/mp/components/wx-reply/main.vue'
import { getSimpleAccounts } from "@/api/mp/account";
import { getTagPage } from "@/api/mp/tag";

export default {
  name: 'mpAutoReply',
  components: {
    WxReplySelect
  },
  data() {
    return {
      // tab 类型（1、关注时回复；2、消息回复；3、关键词回复）
      type: '3',
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

      dialog1Visible:false,
      objData:{
        repType : 'text'
      },
      handleType: null,
      dictData: new Map(),
      hackResetWxReplySelect: false,

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

    // this.getPage(this.page)
    this.dictData.set('wx_rep_mate',[{
      value: '1',
      label: '全匹配'
    },{
      value: '2',
      label: '半匹配'
    }])
    this.dictData.set('wx_req_type',[{
      value: 'text',
      label: '文本'
    },{
      value: 'image',
      label: '图片'
    },{
      value: 'voice',
      label: '语音'
    },{
      value: 'video',
      label: '视频'
    },{
      value: 'shortvideo',
      label: '小视频'
    },{
      value: 'location',
      label: '地理位置'
    },{
      value: 'link',
      label: '链接消息'
    },{
      value: 'event',
      label: '事件推送'
    }])
  },
  methods: {
    /** 查询列表 */
    getList() {
      // 如果没有选中公众号账号，则进行提示。
      if (!this.queryParams.accountId) {
        this.$message.error('未选中公众号，无法查询标签')
        return false
      }

      this.loading = false
      // 处理查询参数
      let params = {...this.queryParams}
      // 执行查询
      getTagPage(params).then(response => {
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

    handleAdd(){
      this.hackResetWxReplySelect = false//销毁组件
      this.$nextTick(() => {
        this.hackResetWxReplySelect = true//重建组件
      })
      this.handleType = 'add'
      this.dialog1Visible = true
      this.objData = {
        repType : 'text'
      }
    },
    handleEdit(row){
      this.hackResetWxReplySelect = false//销毁组件
      this.$nextTick(() => {
        this.hackResetWxReplySelect = true//重建组件
      })
      this.handleType = 'edit'
      this.dialog1Visible = true
      this.objData = Object.assign({}, row)
    },
    handleClick(tab, event){
      this.tableData = []
      this.page.currentPage = 1
      this.type = tab.name
      this.getPage(this.page)
    },
    handleDel: function(row, index) {
      var _this = this
      this.$confirm('是否确认删除此数据', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
          return delObj(row.id)
        }).then(data => {
        _this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
        this.getPage(this.page)
      }).catch(function(err) { })
    },
    handleSubmit(row){
      if(this.handleType === 'add'){
        addObj(Object.assign({
          type:this.type
        }, this.objData)).then(data => {
          this.$message({
            showClose: true,
            message: '添加成功',
            type: 'success'
          })
          this.getPage(this.page)
          this.dialog1Visible = false
        })
      }
      if(this.handleType === 'edit'){
        putObj(this.objData).then(data => {
          this.$message({
            showClose: true,
            message: '修改成功',
            type: 'success'
          })
          this.getPage(this.page)
          this.dialog1Visible = false
        })
      }
    },
  }
}
</script>
