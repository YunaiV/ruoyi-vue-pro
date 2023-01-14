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
-->
<template>
  <div class="app-container">
    <!-- 搜索工作栏 -->
    <el-form ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="accountId" placeholder="请选择公众号">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="public-account-management clearfix" v-loading="loading">
      <!--左边配置菜单-->
      <div class="left">
        <div class="weixin-hd">
            <div class="weixin-title">{{menuName}}</div>
        </div>
        <div class="weixin-menu menu_main clearfix">
          <div class="menu_bottom" v-for="(item, i) of menuList" :key="i" >
            <!-- 一级菜单 -->
            <div @click="menuClick(i, item)" class="menu_item el-icon-s-fold" :class="{'active': isActive === i}">{{item.name}}</div>
            <!-- 以下为二级菜单-->
            <div class="submenu" v-if="isSubMenuFlag === i">
              <div class="subtitle menu_bottom" v-if="item.children" v-for="(subItem, k) in item.children" :key="k">
                <div class="menu_subItem" :class="{'active': isSubMenuActive === i + '' + k}" @click="subMenuClick(subItem, i, k)">
                  {{subItem.name}}
                </div>
              </div>
              <!-- 二级菜单加号， 当长度 小于 5 才显示二级菜单的加号  -->
              <div class="menu_bottom menu_addicon" v-if="!item.children || item.children.length < 5" @click="addSubMenu(i,item)">
                <i class="el-icon-plus" />
              </div>
            </div>
          </div>
          <!-- 一级菜单加号 -->
          <div class="menu_bottom menu_addicon" v-if="menuKeyLength < 3" @click="addMenu"><i class="el-icon-plus"></i></div>
        </div>
        <div class="save_div">
            <!--<el-button class="save_btn" type="warning" size="small" @click="saveFun">保存菜单</el-button>-->
            <el-button class="save_btn" type="success" size="small" @click="saveAndReleaseFun">保存并发布菜单</el-button>
        </div>
      </div>
      <!--右边配置-->
      <div v-if="showRightFlag" class="right">
          <div class="configure_page">
              <div class="delete_btn">
                  <el-button size="mini"  type="danger" icon="el-icon-delete" @click="deleteMenu(tempObj)">删除当前菜单</el-button>
              </div>
              <div>
                  <span>菜单名称：</span>
                  <el-input class="input_width" v-model="tempObj.name" placeholder="请输入菜单名称" :maxlength=nameMaxlength clearable></el-input>
              </div>
              <div v-if="showConfigurContent">
                  <div class="menu_content">
                      <span>菜单内容：</span>
                      <el-select v-model="tempObj.type" clearable placeholder="请选择" class="menu_option">
                          <el-option v-for="item in menuOptions" :label="item.label" :value="item.value" :key="item.value"></el-option>
                      </el-select>
                  </div>
                  <div class="configur_content" v-if="tempObj.type === 'view'">
                      <span>跳转链接：</span>
                      <el-input class="input_width"  v-model="tempObj.url" placeholder="请输入链接" clearable></el-input>
                  </div>
                  <div class="configur_content" v-if="tempObj.type === 'miniprogram'">
                      <div class="applet">
                          <span>小程序的appid：</span>
                          <el-input class="input_width" v-model="tempObj.appid" placeholder="请输入小程序的appid" clearable></el-input>
                      </div>
                      <div class="applet">
                          <span>小程序的页面路径：</span>
                          <el-input class="input_width" v-model="tempObj.pagepath" placeholder="请输入小程序的页面路径，如：pages/index" clearable></el-input>
                      </div>
                      <div class="applet">
                          <span>备用网页：</span>
                          <el-input class="input_width" v-model="tempObj.url" placeholder="不支持小程序的老版本客户端将打开本网页" clearable></el-input>
                      </div>
                      <p class="blue">tips:需要和公众号进行关联才可以把小程序绑定带微信菜单上哟！</p>
                  </div>
                  <div class="configur_content" v-if="tempObj.type == 'article_view_limited'">
                      <el-row>
                          <div class="select-item" v-if="tempObj && tempObj.content && tempObj.content.articles">
                              <WxNews :objData="tempObj.content.articles"></WxNews>
                              <el-row class="ope-row">
                                  <el-button type="danger" icon="el-icon-delete" circle @click="deleteTempObj"></el-button>
                              </el-row>
                          </div>
                          <div v-if="!tempObj.content || !tempObj.content.articles">
                              <el-row>
                                  <el-col :span="24" style="text-align: center">
                                      <el-button type="success" @click="openMaterial">素材库选择<i class="el-icon-circle-check el-icon--right"></i></el-button>
                                  </el-col>
                              </el-row>
                          </div>
                          <el-dialog title="选择图文" :visible.sync="dialogNewsVisible" width="90%">
                              <WxMaterialSelect :objData="{repType:'news'}" @selectMaterial="selectMaterial"></WxMaterialSelect>
                          </el-dialog>
                      </el-row>
                  </div>
                  <div class="configur_content" v-if="tempObj.type == 'click' || tempObj.type == 'scancode_waitmsg'">
                      <WxReplySelect :objData="tempObj" v-if="hackResetWxReplySelect"></WxReplySelect>
                  </div>
              </div>
          </div>
      </div>
      <!--一进页面就显示的默认页面，当点击左边按钮的时候，就不显示了-->
      <div v-if="!showRightFlag" class="right">
          <p>请选择菜单配置</p>
      </div>
    </div>
  </div>
</template>

<script>
// import { save, saveAndRelease ,getList} from '@/api/wxmp/wxmenu'
import WxReplySelect from '@/views/mp/components/wx-news/main.vue'
import WxNews from '@/views/mp/components/wx-news/main.vue';
import WxMaterialSelect from '@/views/mp/components/wx-news/main.vue'
import {getMenuList} from "@/api/mp/menu";
import {getSimpleAccounts} from "@/api/mp/account";

export default {
  name: 'mpMenu',
  components: {
      WxReplySelect,
      WxNews,
      WxMaterialSelect
  },
  data(){
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 查询参数
      accountId: undefined,
      menuList: {
        children: [],
      },

      showRightFlag:false, // 右边配置显示默认详情还是配置详情
      menu:{ // 横向菜单
          button:[
          ]
      },

      isActive: -1,// 一级菜单点中样式
      isSubMenuActive: -1, // 一级菜单点中样式
      isSubMenuFlag: -1, // 二级菜单显示标志
      tempObj:{}, // 右边临时变量，作为中间值牵引关系
      tempSelfObj: {
          // 一些临时值放在这里进行判断，如果放在 tempObj，由于引用关系，menu 也会多了多余的参数
      },
      visible2: false, //素材内容  "选择素材"按钮弹框显示隐藏
      tableData:[], //素材内容弹框数据,
      menuName:'',
      showConfigurContent:true,
      nameMaxlength:0,//名称最大长度
      menuOptions: [{
          value: 'view',
          label: '跳转网页'
      }, {
          value: 'miniprogram',
          label: '跳转小程序'
      }, {
          value: 'click',
          label: '点击回复'
      }, {
          value: 'article_view_limited',
          label: '跳转图文消息'
      }, {
          value: 'scancode_push',
          label: '扫码直接返回结果'
      }, {
          value: 'scancode_waitmsg',
          label: '扫码回复'
      }, {
          value: 'pic_sysphoto',
          label: '系统拍照发图'
      }, {
          value: 'pic_photo_or_album',
          label: '拍照或者相册'
      }, {
          value: 'pic_weixin',
          label: '微信相册'
      }, {
          value: 'location_select',
          label: '选择地理位置'
      }],
      dialogNewsVisible: false,
      hackResetWxReplySelect: false,

      // 公众号账号列表
      accounts: [],
    }
  },
  created() {
    getSimpleAccounts().then(response => {
      this.accounts = response.data;
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.setAccountId(this.accounts[0].id);
      }
      // 加载数据
      this.getList();
    })
  },
  computed: {
    menuKeyLength:function() {
      // menuObj 的长度，当长度小于 3 才显示一级菜单的加号
      return this.menu.button.length;
    }
  },
  methods: {
    // ======================== 列表查询 ========================
    /** 设置账号编号 */
    setAccountId(accountId) {
      this.accountId = accountId;
      // this.uploadData.accountId = accountId;
    },
    getList() {
      this.loading = false;
      getMenuList(this.accountId).then(response => {
        this.menuList = this.handleTree(response.data, "id");
        console.log(this.menuList)
      }).finally(() => {
        this.loading = false;
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      // 默认选中第一个
      if (this.accountId) {
        this.setAccountId(this.accountId)
      }
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm('queryForm')
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.setAccountId(this.accounts[0].id)
      }
      this.handleQuery()
    },

    // TODO 芋艿：未归类

    deleteTempObj(){
        this.$delete(this.tempObj,'repName')
        this.$delete(this.tempObj,'repUrl')
        this.$delete(this.tempObj,'content')
    },
    openMaterial(){
        this.dialogNewsVisible = true
    },
    selectMaterial(item){
        if(item.content.articles.length>1){
            this.$alert('您选择的是多图文，将默认跳转第一篇', '提示', {
                confirmButtonText: '确定'
            })
        }
        this.dialogNewsVisible = false
        this.tempObj.article_id = item.articleId
        this.tempObj.mediaName = item.name
        this.tempObj.url = item.url
        item.mediaType = this.tempObj.mediaType
        item.content.articles = item.content.articles.slice(0,1)
        this.tempObj.content = item.content
    },
    handleClick(tab, event){
        this.tempObj.mediaType = tab.name
    },
    saveAndReleaseFun(){
        this.$confirm('确定要保证并发布该菜单吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            this.loading = true
            saveAndRelease({
                strWxMenu:this.menu
            }).then(response => {
                this.loading = false
                if(response.code == 200){
                    this.$message({
                        showClose: true,
                        message: '发布成功',
                        type: 'success'
                    })
                }else{
                    this.$message.error(response.data.msg)
                }
            }).catch(() => {
                this.loading = false
            })
        }).catch(() => {
        })
    },
    // 一级菜单点击事件
    menuClick(i, item){
        this.hackResetWxReplySelect = false//销毁组件
        this.$nextTick(() => {
            this.hackResetWxReplySelect = true//重建组件
        })
        this.showRightFlag = true;//右边菜单
        this.tempObj = item;//这个如果放在顶部，flag会没有。因为重新赋值了。
        this.tempSelfObj.grand = "1";//表示一级菜单
        this.tempSelfObj.index = i;//表示一级菜单索引
        this.isActive = i; //一级菜单选中样式
        this.isSubMenuFlag = i; //二级菜单显示标志
        this.isSubMenuActive = -1; //二级菜单去除选中样式
        this.nameMaxlength = 4
        if(item.sub_button && item.sub_button.length > 0){
            this.showConfigurContent = false
        }else{
            this.showConfigurContent = true
        }
    },
    // 二级菜单点击事件
    subMenuClick(subItem, index, k){
        this.hackResetWxReplySelect = false//销毁组件
        this.$nextTick(() => {
            this.hackResetWxReplySelect = true//重建组件
        })
        this.showRightFlag = true;//右边菜单
        this.tempObj = subItem;//将点击的数据放到临时变量，对象有引用作用
        this.tempSelfObj.grand = "2";//表示二级菜单
        this.tempSelfObj.index = index;//表示一级菜单索引
        this.tempSelfObj.secondIndex = k;//表示二级菜单索引
        this.isSubMenuActive = index + "" + k; //二级菜单选中样式
        this.isActive = -1;//一级菜单去除样式
        this.showConfigurContent = true;
        this.nameMaxlength = 7
    },
    // 添加横向一级菜单
    addMenu(){
        let menuKeyLength = this.menuKeyLength
        let addButton = {
            name: "菜单名称",
            sub_button: []
        }
        this.$set(this.menu.button,menuKeyLength,addButton)
        this.menuClick(this.menuKeyLength-1, addButton)
    },
    // 添加横向二级菜单
    addSubMenu(i,item){
        if(!item.sub_button||item.sub_button.length<=0){
            this.$set( item, 'sub_button',[])
            this.$delete( item, 'type')
            this.$delete( item, 'pagepath')
            this.$delete( item, 'url')
            this.$delete( item, 'key')
            this.$delete( item, 'article_id')
            this.$delete( item, 'textContent')
            this.showConfigurContent = false
        }
        let subMenuKeyLength = item.sub_button.length;//获取二级菜单key长度
        let addButton = {
            name: "子菜单名称"
        }
        this.$set(item.sub_button,subMenuKeyLength,addButton);
        this.subMenuClick(item.sub_button[subMenuKeyLength], i, subMenuKeyLength)
    },
    //删除当前菜单
    deleteMenu(obj){
        let _this = this;
        this.$confirm('确定要删除吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }).then(() => {
            _this.deleteData();// 删除菜单数据
            _this.tempObj = {};
            _this.showRightFlag = false;
            this.isActive = -1;
            this.isSubMenuActive = -1;
        }).catch(() => {
        });
    },
    // 删除菜单数据
    deleteData(){
        // 一级菜单的删除方法
        if(this.tempSelfObj.grand == "1"){
            this.menu.button.splice(this.tempSelfObj.index,1);
        }
        // 二级菜单的删除方法
        if(this.tempSelfObj.grand == "2"){
            this.menu.button[this.tempSelfObj.index].sub_button.splice(this.tempSelfObj.secondIndex, 1);
        }
        this.$message({
            type: 'success',
            message: '删除成功!'
        });
    }
  },
}
</script>
<!--本组件样式-->
<style lang="less" scoped="scoped">
/* 公共颜色变量 */
.clearfix{*zoom:1;}
.clearfix::after{content: "";display: table; clear: both;}
div{
  text-align: left;
}
.weixin-hd{
  color: #fff;
  text-align: center;
  position: relative;
  bottom: 426px;
  left:0px;
  width: 300px;
  height:64px;
  background: transparent url(assets/menu_head.png) no-repeat 0 0;
  background-position: 0 0;
  background-size: 100%
}
.weixin-title{
  color:#fff;
  font-size:14px;
  width:100%;
  text-align: center;
  position:absolute;
  top: 33px;
  left: 0px;
}
.weixin-menu{
  background: transparent url(assets/menu_foot.png) no-repeat 0 0;
  padding-left: 43px;
  font-size: 12px
}
.menu_option{
  width: 40%!important;
}
.public-account-management{
  min-width: 1200px;
  width: 1200px;
  margin: 0 auto;
  .left{
      float: left;
      display: inline-block;
      width: 350px;
      height: 715px;
      background: url("assets/iphone_backImg.png") no-repeat;
      background-size: 100% auto;
      padding: 518px 25px 88px;
      position: relative;
      box-sizing: border-box;
      /*第一级菜单*/
      .menu_main{
          .menu_bottom{
              position: relative;
              float: left;
              display: inline-block;
              box-sizing: border-box;
              width: 85.5px;
              text-align: center;
              border: 1px solid #ebedee;
              background-color: #fff;
              cursor: pointer;
              &.menu_addicon{
                  height: 46px;
                  line-height: 46px;
              }
              .menu_item{
                  height: 44px;
                  line-height: 44px;
                  text-align: center;
                  box-sizing: border-box;
                  width: 100%;
                  &.active{
                      border: 1px solid #2bb673;
                  }
              }
              .menu_subItem{
                  height: 44px;
                  line-height: 44px;
                  text-align: center;
                  box-sizing: border-box;
                  &.active{
                      border: 1px solid #2bb673;
                  }
              }
          }
          i{
              color:#2bb673;
          }
          /*第二级菜单*/
          .submenu{
              position: absolute;
              width: 85.5px;
              bottom: 45px;
              .subtitle{
                  background-color: #fff;
                  box-sizing: border-box;
              }
          }
      }
      .save_div{
          margin-top: 15px;
          text-align: center;
          .save_btn{
              bottom: 20px;
              left: 100px;
          }
      }
  }
  /*右边菜单内容*/
  .right{
      float: left;
      width: 63%;
      background-color: #e8e7e7;
      padding: 20px;
      margin-left: 20px;
      -webkit-box-sizing: border-box;
      box-sizing: border-box;
      .configure_page{
          .delete_btn{
              text-align: right;
              margin-bottom: 15px;
          }
          .menu_content{
              margin-top: 20px;
          }
          .configur_content{
              margin-top: 20px;
              background-color: #fff;
              padding: 20px 10px;
              border-radius: 5px
          }
          .blue{
              color:#29b6f6;
              margin-top: 10px;
          }
          .applet{
              margin-bottom: 20px;
              span{
                  width: 20%;
              }
          }
          .material{
              .input_width{
                  width: 30%;
              }
              .el-textarea{
                  width: 80%
              }
          }
      }
  }
}
</style>
<!--修改UI框架样式-->
<!--<style lang="less" scoped>-->
<!--    .public-account-management{-->
<!--        .el-input{-->
<!--            width: 70%;-->
<!--            margin-right: 2%;-->
<!--        }-->
<!--    }-->
<!--</style>-->
<!--素材样式-->
<style lang="scss" scoped>
    .pagination{
        text-align: right;
        margin-right: 25px;
    }
    .select-item{
        width: 280px;
        padding: 10px;
        margin: 0 auto 10px auto;
        border: 1px solid #eaeaea;
    }
    .select-item2{
        padding: 10px;
        margin: 0 auto 10px auto;
        border: 1px solid #eaeaea;
    }
    .ope-row{
        padding-top: 10px;
        text-align: center;
    }
    .item-name{
        font-size: 12px;
        overflow: hidden;
        text-overflow:ellipsis;
        white-space: nowrap;
        text-align: center;
    }
</style>
