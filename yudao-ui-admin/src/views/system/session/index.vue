<template>
  <div class="app-container">
    <doc-alert title="用户体系" url="https://doc.iocoder.cn/user-center/" />
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="68px">
      <el-form-item label="登录地址" prop="userIp">
        <el-input v-model="queryParams.userIp" placeholder="请输入登录地址" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="用户名称" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>

    </el-form>
    <el-table v-loading="loading" :data="list" style="width: 100%;">
      <el-table-column label="会话编号" align="center" prop="id" width="300" />
      <el-table-column label="登录名称" align="center" prop="username" width="100" />
      <el-table-column label="部门名称" align="center" prop="deptName" width="100" />
      <el-table-column label="登录地址" align="center" prop="userIp" width="100" />
      <el-table-column label="userAgent" align="center" prop="userAgent" :show-overflow-tooltip="true" />
      <el-table-column label="登录时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleForceLogout(scope.row)"
            v-hasPermi="['system:user-session:delete']">强退</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script>
import { list, forceLogout } from "@/api/system/session";

export default {
  name: "Online",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        userIp: undefined,
        username: undefined
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志列表 */
    getList() {
      this.loading = true;
      list(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 强退按钮操作 */
    handleForceLogout(row) {
      this.$modal.confirm('是否确认强退名称为"' + row.username + '"的数据项?').then(function() {
          return forceLogout(row.id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("强退成功");
      }).catch(() => {});
    }
  }
};
</script>

