<template>
  <el-dialog width="60%" title="用户选择" :visible.sync="dialogFormVisible" :before-close="close">
    <el-form>
      <el-row style="width: 93%;margin: 0 auto">
        <el-col :span="6">
          <el-form-item label="账户:" >
            <el-input v-model="searchData.account" style="width:80%" ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="昵称:">
            <el-input v-model="searchData.name" style="width:80%" ></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-button type="primary">查询</el-button>
          <el-button >重置</el-button>
        </el-col>
      </el-row>
      <el-form-item>
        <el-table v-show="type === 'assignee'"
            border :data="options"
            style="width: 93%;margin: 0 auto">
          <el-table-column align="center" prop="account"
                           label="账户">
          </el-table-column>
          <el-table-column align="center" prop="name" :show-overflow-tooltip="true"
                           label="昵称">
          </el-table-column>
          <el-table-column align="center"
                           label="操作">
            <template slot-scope="scope">
              <div>
                <el-button type="primary" @click="commitForm(scope.$index)">选择</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-table v-show="type === 'candidateUsers'"
            border :data="options"  @selection-change="handleSelectionChange"
            style="width: 93%;margin: 0 auto">
          <!--批量选择用户的时候需要复选框-->
          <el-table-column
              type="selection"
              width="55">
          </el-table-column>
          <el-table-column align="center" prop="account"
                           label="账户">
          </el-table-column>
          <el-table-column align="center" prop="name" :show-overflow-tooltip="true"
                           label="昵称">
          </el-table-column>
        </el-table>
        <el-pagination style="float: right;margin:20px 2.7% 0 0"
            background
            layout="prev, pager, next"
            :total="1000">
        </el-pagination>
      </el-form-item>
    </el-form>
    <!--批量选择用户的时候需要确认按钮-->
    <div slot="footer" class="dialog-footer"  v-if="type === 'candidateUsers'">
      <el-button type="primary"  @click="commitForm()">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "UserSelectDialog",
  data() {
    return {
      activeNames: ['1'],
      formLabelWidth: "200px",
      value: "类",
      searchData: {},
      multipleSelection: [],
      options: [
        {
          name: "张三",
          account: "admin1"
        },
        {
          name: "李四",
          account: "admin2"
        },
        {
          name: "王五",
          account: "admin3"
        }
      ]
    }
  },
  props: {
    formData:{
      type: Object,
      required: true
    },
    type: {
      type: String,
      required: false
    },
    dialogFormVisibleBool:{
      type: Boolean,
      required: false
    },
    modeler: {
      type: Object,
      required: false
    },
    nodeElement:{
      type: Object,
      required: false
    }
  },
  computed:{
    form:{
      get(){
        return this.formData;
      }
    },
    dialogFormVisible:{
      get(){
        return this.dialogFormVisibleBool
      }
    }
  },
  watch:{
    type:{
      handler(){
        console.log(this.type)
      }
    }
  },
  mounted() {
  },
  methods: {
    updateProperties(properties){
      this.modeler.get("modeling").updateProperties(this.nodeElement, properties);
    },
    commitForm(index){
      if (this.type === 'assignee') {
        this.modeler.get("modeling").updateProperties(this.nodeElement, {
          assignee: this.options[index].account
        });
        this.$emit('commitUserForm', this.options[index]);
      } else if(this.type === 'candidateUsers'){
        let updateStr = null
        let candidateUsersStr = this.multipleSelection.map(item => item.account);
        if (candidateUsersStr.length > 0) {
          updateStr = candidateUsersStr.join(",")
        }
        this.modeler.get("modeling").updateProperties(this.nodeElement, {
          candidateUsers: updateStr
        });
        this.$emit('commitUserForm', null);
      }
    },
    close(){
      this.$emit('commitUserForm', null);
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    }
  }
}
</script>

<style scoped>
/deep/.el-dialog > .el-dialog__header{
  padding: 24px 20px
}
</style>