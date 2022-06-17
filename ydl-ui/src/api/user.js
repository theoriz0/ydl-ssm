import request from "@/api"

export function login(data) {
    return request({
        url: '/login',
        method: 'post',
        data: data
    })
}

export function logout() {
    return request({
        url: '/logout',
        method: 'post'
    })
}

export function listUser(query) {
    return request({
        url: '/ydlUser',
        method: 'get',
        params: query
    })
}

export function getInfo() {
    return request({
        url: '/ydlUser/getInfo',
        method: 'get'
    })
}
//
// export function addUser(data) {
//     return request({
//         url: '/user',
//         method: 'post',
//         params: data
//     })
// }