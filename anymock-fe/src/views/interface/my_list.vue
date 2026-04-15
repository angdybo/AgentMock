<template>
  <el-container>
    <el-aside width="200px"><spacelist/></el-aside>
    <el-main>
      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" type="border-card" style="height:100%">
        <el-tab-pane label="接口列表" name="list">
          <interfacelist/>
        </el-tab-pane>
        <el-tab-pane label="API 调试" name="tester">
          <apitester/>
        </el-tab-pane>
      </el-tabs>
    </el-main>
  </el-container>
</template>

<script>
import spacelist from './component/space_list'
import interfacelist from './component/interface_list'
import apitester from './component/api_tester'

export default {
  name: 'MyListPage',
  components: { spacelist, interfacelist, apitester },
  data() {
    return {
      activeTab: 'list'
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
  .el-aside {
    color: #333;
    text-align: center;
    line-height: 200px;
    margin-left: 5px;
    position: relative;
  }

  .el-main {
    padding: 0 !important;
  }

  .el-tabs--border-card {
    border: none;
    box-shadow: none;
    height: calc(100vh - 120px);
    
    >>> .el-tabs__header {
      background: #fff;
      margin-bottom: 0;
    }
    
    >>> .el-tabs__content {
      padding: 10px;
      height: calc(100% - 40px);
      overflow-y: auto;
    }
    
    >>> .el-tab-pane {
      height: 100%;
    }
  }
</style>
