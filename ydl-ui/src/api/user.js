import request from "@/api"

export function login(data) {
    return request({
        url: '/login',
        method: 'post',
        data: data
    })
}

// export function listUser(query) {
//     return request({
//         url: '/user',
//         method: 'get',
//         params: query
//     })
// }
//
// export function addUser(data) {
//     return request({
//         url: '/user',
//         method: 'post',
//         params: data
//     })
// }