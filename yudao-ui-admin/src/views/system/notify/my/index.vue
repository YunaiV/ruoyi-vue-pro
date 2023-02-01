<template>
  <div class="app-container">
    <doc-alert title="站内信配置" url="https://doc.iocoder.cn/notify/" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="是否已读" prop="readStatus">
        <el-select v-model="queryParams.readStatus" placeholder="请选择状态" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.INFRA_BOOLEAN_STRING)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="发送时间" prop="createTime">
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
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleUpdateList">标记已读</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleUpdateAll">全部已读</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" ref="tables" :data="list">
      <el-table-column type="selection" width="55" />
      <el-table-column label="发送人" align="center" prop="templateNickname" width="120" />
      <el-table-column label="发送时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="类型" align="center" prop="templateType" width="80">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE" :value="scope.row.templateType" />
        </template>
      </el-table-column>
      <el-table-column label="内容" align="center" prop="templateContent" />
      <el-table-column label="是否已读" align="center" prop="readStatus" width="80">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.readStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template v-slot="scope">
          <el-button v-show="!scope.row.readStatus" size="mini" type="text" icon="el-icon-check" @click="handleUpdateSingle(scope.row)">已读</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

  </div>
</template>

<script>
import {getMyNotifyMessagePage, updateAllNotifyMessageRead, updateNotifyMessageRead} from "@/api/system/notify/message";

export default {
  name: "myNotify",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 我的站内信列表
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        readStatus: null,
        createTime: []
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getMyNotifyMessagePage(this.queryParams).then(response => {
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
      this.handleQuery();
    },
    handleUpdateList() {
      let list = this.$refs["tables"].selection;
      if (list.length === 0) {
        return;
      }
      this.handleUpdate(list.map(v => v.id))
    },
    handleUpdateSingle(row) {
      this.handleUpdate([row.id])
    },
    handleUpdate(ids) {
      updateNotifyMessageRead(ids).then(response => {
        this.$modal.msgSuccess("标记已读成功！");
        this.getList();
      });
    },
    handleUpdateAll(){
      updateAllNotifyMessageRead().then(response => {
        this.$modal.msgSuccess("全部已读成功！");
        this.getList();
      });
    }
  }
}
</script>
