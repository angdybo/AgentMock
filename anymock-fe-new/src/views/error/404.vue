<template>
  <div class="error-page">
    <h1>404 - 页面不存在</h1>
    <el-button type="primary" @click="$router.push('/home')">返回首页</el-button>
  </div>
</template>

<script setup>
</script>

<style scoped>
.error-page {
  padding: 100px;
  text-align: center;
}
</style>
