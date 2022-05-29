// 导入用来创建路由和确定路由模式的两个方法
import {
    createRouter,
    createWebHistory
} from 'vue-router';

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
    //每次进行路由切换都判断一下有没有登陆，如果没有登陆，跳转到登陆页
    return true;
})

// 将路由实例导出
export default router;