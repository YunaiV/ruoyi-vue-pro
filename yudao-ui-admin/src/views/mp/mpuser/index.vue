<template>
  <div class="app-container">
    <doc-alert title="公众号粉丝" url="https://doc.iocoder.cn/mp/user/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="queryParams.accountId" placeholder="请选择公众号">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户标识" prop="openid">
        <el-input v-model="queryParams.openid" placeholder="请输入用户标识" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-refresh" size="mini" @click="handleSync"
                   v-hasPermi="['mp:user:sync']">同步
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="用户标识" align="center" prop="openid" width="260" />
      <el-table-column label="昵称" align="center" prop="nickname" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="标签" align="center" prop="tagIds" width="200">
        <template v-slot="scope">
          <span v-for="(tagId, index) in scope.row.tagIds" :key="index">
            <el-tag>{{ tags.find(tag => tag.tagId === tagId)?.name }} </el-tag>&nbsp;
          </span>
        </template>
      </el-table-column>
      <el-table-column label="订阅状态" align="center" prop="subscribeStatus">
        <template v-slot="scope">
          <el-tag v-if="scope.row.subscribeStatus === 0" type="success">已订阅</el-tag>
          <el-tag v-else type="danger">未订阅</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订阅时间" align="center" prop="subscribeTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.subscribeTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['mp:user:update']">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="标签" prop="tagIds">
          <el-select v-model="form.tagIds" multiple clearable placeholder="请选择标签">
            <el-option v-for="item in tags" :key="parseInt(item.tagId)" :label="item.name" :value="parseInt(item.tagId)" />
          </el-select>
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
import { updateUser, getUser, getUserPage, syncUser } from "@/api/mp/mpuser";
import { getSimpleAccounts } from "@/api/mp/account";
import { getSimpleTags } from "@/api/mp/tag";

export default {
  name: "WxAccountFans",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 微信公众号粉丝列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        accountId: null,
        openid: null,
        nickname: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {},

      // 公众号账号列表
      accounts: [],
      // 公众号标签列表
      tags: [],
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

    // 加载标签
    getSimpleTags().then(response => {
      this.tags = response.data;
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      // 如果没有选中公众号账号，则进行提示。
      if (!this.queryParams.accountId) {
        this.$message.error('未选中公众号，无法查询用户')
        return false
      }

      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      // 执行查询
      getUserPage(params).then(response => {
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
        nickname: undefined,
        remark: undefined,
        tagIds: [],
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
      this.resetForm("queryForm");
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.queryParams.accountId = this.accounts[0].id;
      }
      this.handleQuery();
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getUser(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改公众号粉丝";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateUser(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
        }
      });
    },
    /** 同步标签 */
    handleSync() {
      const accountId = this.queryParams.accountId
      this.$modal.confirm('是否确认同步粉丝？').then(function () {
        return syncUser(accountId)
      }).then(() => {
        this.$modal.msgSuccess('开始从微信公众号同步粉丝信息，同步需要一段时间，建议稍后再查询')
      }).catch(() => {
      })
    },
  }
};
</script>
