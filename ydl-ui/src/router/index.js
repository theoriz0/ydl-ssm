// 导入用来创建路由和确定路由模式的两个方法
import store from '@/store';
import {
    createRouter,
    createWebHistory
} from 'vue-router';
import storage from '@/util/storage';

/**
 * 定义路由信息
 * 
 */
const routes = [{
    name: 'login',
    path: '/login',
    component: () => import('@/components/login')
},{
    name: 'main',
    alias: '/',
    path: '/main',
    component: () => import('@/components/main')
}];

// 创建路由实例并传递 `routes` 配置
// 我们在这里使用 html5 的路由模式，url中不带有#，部署项目的时候需要注意。
const router = createRouter({
    history: createWebHistory(),
    routes, 
});


// 全局的路由守卫
router.beforeEach((to, from) => {
    //1. 如果去的是登陆，放行
    if (to.name === 'login') {
        return true
    }
    //2. 否则检查是否登陆，如果已经登陆，则放行
    if (store.getters.isLogin) {
        return true
    }
    //3. 没有登陆就跳转到登陆
    if (!storage.getSessionObject("loginUser")) {
        router.push({name: 'login'})
    }
    //4. 登陆则读取storage到vuex
    store.dispatch("RECOVERY_USER")
    
    return true;
})

// 将路由实例导出
export default router;