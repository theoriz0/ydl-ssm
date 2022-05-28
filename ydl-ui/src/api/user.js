import request from "@/api"

export function listUser(query) {
    return request({
        url: '/user',
        method: 'get',
        params: query
    })
}

export function addUser(data) {
    return request({
        url: '/user',
        method: 'post',
        params: data
    })
}