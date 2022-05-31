import axios from 'axios'
import store from '@/store'

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
    headers: {'Content-Type': 'application/json;charset=utf-8'}
})
// Add a request interceptor
request.interceptors.request.use(function (config) {
    if (store.state.user.token) {
        config.headers['Authorization'] = store.state.user.token
    }
    // Do something before request is sent
    return config;
}, function (error) {
    // Do something with request error
    return Promise.reject(error);
});

export default request