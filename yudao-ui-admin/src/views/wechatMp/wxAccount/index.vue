<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入公众号名称" clearable
                  @keyup.enter.native="handleQuery"/>
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
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['wechatMp:account:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   :loading="exportLoading"
                   v-hasPermi="['wechatMp:account:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id"/>
      <el-table-column label="名称" align="center" prop="name"/>
      <el-table-column label="微信原始ID" align="center" prop="account"/>
      <el-table-column label="appId" align="center" prop="appId"/>
      <el-table-column label="url" align="center" prop="url"/>
      <el-table-column label="Token" align="center" prop="token"/>
      <el-table-column label="加密密钥" align="center" prop="aesKey"/>
      <el-table-column label="二维码" align="center" prop="qrCodeUrl"/>
      <el-table-column label="备注" align="center" prop="remark"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['wechatMp:account:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['wechatMp:account:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="公众号名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入公众号名称"/>
        </el-form-item>

        <el-tooltip class="item" effect="dark"
                    content="在微信公众平台（mp.weixin.qq.com）的菜单【设置】-【公众号设置】-【帐号详情】中能找到原始ID"
                    placement="right">
          <el-form-item label="微信原始ID" prop="account">
            <el-input v-model="form.account" placeholder="请输入微信原始ID" :disabled='disabled'/>
          </el-form-item>
        </el-tooltip>

        <el-tooltip class="item" effect="dark"
                    content="在微信公众平台（mp.weixin.qq.com）的菜单【开发】-【基本配置】中能找到AppID "
                    placement="right">
          <el-form-item label="AppID" prop="appId">
            <el-input v-model="form.appId" placeholder="请输入公众号appId" :disabled='disabled'/>
          </el-form-item>
        </el-tooltip>

        <el-tooltip class="item" effect="dark"
                    content="在微信公众平台（mp.weixin.qq.com）的菜单【开发】-【基本配置】中能找到AppSecret"
                    placement="right">
          <el-form-item label="AppSecret" prop="appSecret">
            <el-input v-model="form.appSecret" placeholder="请输入公众号密钥" :disabled='disabled'/>
          </el-form-item>
        </el-tooltip>

        <el-form-item label="token" prop="token">
          <el-input v-model="form.token" placeholder="请输入公众号token"/>
        </el-form-item>
        <el-form-item label="加密密钥" prop="aesKey">
          <el-input v-model="form.aesKey" placeholder="请输入加密密钥"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createAccount,
  deleteAccount,
  exportAccountExcel,
  getAccount,
  getAccountPage,
  updateAccount
} from '@/api/wechatMp/wxAccount'

export default {
  name: 'wxAccount',
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
      // 公众号账户列表
      list: [],
      // 弹出层标题
      title: '',
      // 是否显示弹出层
      open: false,
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        account: null,
        appId: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{required: true, message: '公众号名称不能为空', trigger: 'blur'}],
        account: [{required: true, message: '公众号账户不能为空', trigger: 'blur'}],
        appId: [{required: true, message: '公众号appid不能为空', trigger: 'blur'}],
        appSecret: [{required: true, message: '公众号密钥不能为空', trigger: 'blur'}],
      },
      // 禁用属性
      disabled: false,
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      // 处理查询参数
      let params = {...this.queryParams}
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime')
      // 执行查询
      getAccountPage(params).then(response => {
        this.list = response.data.list
        this.total = response.data.total
        this.loading = false
      })
    },
    /** 取消按钮 */
    cancel() {
      this.open = false
      this.disabled = false
      this.reset()
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        account: undefined,
        appId: undefined,
        appSecret: undefined,
        token: undefined,
        aesKey: undefined,
        remark: undefined,
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
      this.title = '添加公众号账户'
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id
      getAccount(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改公众号账户'
        this.disabled = true
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
          updateAccount(this.form).then(response => {
            this.$modal.msgSuccess('修改成功')
            this.open = false
            this.getList()
          })
          return
        }
        // 添加的提交
        createAccount(this.form).then(response => {
          this.$modal.msgSuccess('新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id
      this.$modal.confirm('是否确认删除公众号账户编号为"' + id + '"的数据项?').then(function () {
        return deleteAccount(id)
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
      this.$modal.confirm('是否确认导出所有公众号账户数据项?').then(() => {
        this.exportLoading = true
        return exportAccountExcel(params)
      }).then(response => {
        this.$download.excel(response, '公众号账户.xls')
        this.exportLoading = false
      }).catch(() => {
      })
    }
  }
}
</script>
