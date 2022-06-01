import {login,logout} from '@/api/user'
import storage from '@/util/storage'

const user = {
    state: {
        username: '',
        nickname: '',
        token: ''
    },
    getters: {},
    mutations: {
        SAVE_USERNAME(state, username) {
            state.username = username
        },
        SAVE_TOKEN(state, token) {
            state.token = token
        },
        SAVE_NICKNAME(state, nickname) {
            state.nickname = nickname
        },
    },
    actions: {
        LOGIN({commit}, user) {
            return new Promise(function (resolve, reject) {
                login(user).then(res => {
                    //需要将获取的数据保存起来
                    commit("SAVE_USERNAME", res.data.ydlUser.userName)
                    commit("SAVE_TOKEN", res.data.token)
                    commit("SAVE_NICKNAME", res.data.ydlUser.nickName)
                    storage.saveSessionObject("loginUser", res.data)
                    resolve(res)
                })
            })
        },
        LOGOUT({commit}, user) {
            return new Promise(function (resolve, reject) {
                logout().then(res => {
                    //需要将获取的数据保存起来
                    commit("SAVE_USERNAME", "")
                    commit("SAVE_TOKEN", "")
                    commit("SAVE_NICKNAME", "")
                    storage.remove("loginUser")
                    resolve(res)
                })
            })
        }
    }
}

export default user