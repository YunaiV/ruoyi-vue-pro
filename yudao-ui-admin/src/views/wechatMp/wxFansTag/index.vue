<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="标签名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入标签名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['wechatMp:fans-tag:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   :loading="exportLoading"
                   v-hasPermi="['wechatMp:fans-tag:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <div style="display: flex;width: auto">
      <div class="left_column" style="border: 1px solid #EBEEF5FF; width: 15%;height: auto">
        <div style="padding: 10px 20px; border-bottom: 1px solid #ebeef5; box-sizing: border-box;"><span
          style="font-size: 16px">公众号名称</span></div>

        <div style="margin-top: 10px;margin-right: 5px;margin-left: 5px">
          <input type="text" placeholder="输入关键字进行过滤"
                 class="el-input__inner"/>
        </div>

        <div style="margin-top: 10px;margin-right: 5px;margin-left: 5px">
          <div style="margin-right: 5px;margin-left: 5px" v-for="(account,index) in accountList" @click="getAccountTag(account.appId)">{{ account.name }}</div>
        </div>
      </div>
      <div class="right_column" style="width: 85%">
        <!-- 列表 -->
        <el-table v-loading="loading" :data="list">
          <el-table-column label="编号" align="center" prop="id"/>
          <el-table-column label="标签名称" align="center" prop="name"/>
          <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                         v-hasPermi="['wechatMp:fans-tag:update']">修改
              </el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                         v-hasPermi="['wechatMp:fans-tag:delete']">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <!-- 分页组件 -->
        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                    @pagination="getList"/>

      </div>
    </div>


    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称"/>
        </el-form-item>
        <el-form-item label="粉丝数量" prop="count">
          <el-input v-model="form.count" placeholder="请输入粉丝数量"/>
        </el-form-item>
        <el-form-item label="微信账号ID" prop="wxAccountId">
          <el-input v-model="form.wxAccountId" placeholder="请输入微信账号ID"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style>
.left_column {
  height: 100%;
  position: relative;
  overflow: auto;
}
</style>

<script>
import {
  createWxFansTag,
  deleteWxFansTag,
  exportWxFansTagExcel,
  getWxFansTag,
  getWxFansTagList,
  updateWxFansTag
} from '@/api/wechatMp/wxFansTag'
import {getAccountPage} from '@/api/wechatMp/wxAccount'

export default {
  name: 'WxFansTag',
  components: {},
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
      // 粉丝标签列表
      list: [],
      // 账号列表
      accountList: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        appId: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {}
    }
  },
  created() {
    this.getAccountList()
  },
  methods: {
    /** 查询列表 */
    getList(appId) {
      this.loading = false
      this.queryParams.appId = appId
      // 处理查询参数
      let params = {...this.queryParams}
      // 执行查询
      getWxFansTagList(params).then(response => {
        this.list = response.data
        this.loading = false
      })
    },
    /** 查询列表 */
    getAccountList() {
      // 执行查询
      getAccountPage().then(response => {
        this.accountList = response.data.list
      })
    },

    /**
     * 获取帐户标签
     * @param appId 公众号appId
     */
    getAccountTag(appId) {
      this.getList(appId)
    },

    /** 取消按钮 */
    cancel() {
      this.open = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {
        appId: undefined,
      }
      this.resetForm('form')
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeCreateTime = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加粉丝标签'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id
      getWxFansTag(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改粉丝标签'
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        // 修改的提交
        if (this.form.id != null) {
          updateWxFansTag(this.form).then(response => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        // 添加的提交
        createWxFansTag(this.form).then(response => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id
      this.$modal.confirm('是否确认删除粉丝标签编号为"' + id + '"的数据项?').then(function () {
        return deleteWxFansTag(id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams}
      params.pageNo = undefined
      params.pageSize = undefined
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime')
      // 执行导出
      this.$modal.confirm('是否确认导出所有粉丝标签数据项?').then(() => {
        this.exportLoading = true
        return exportWxFansTagExcel(params)
      }).then(response => {
        this.$download.excel(response, '粉丝标签.xls')
        this.exportLoading = false
      }).catch(() => {
      })
    }
  }
}
</script>
