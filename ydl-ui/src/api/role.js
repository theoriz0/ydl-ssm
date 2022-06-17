import request from "@/api"

export function listRole(query) {
    return request({
        url: '/ydlRole',
        method: 'get',
        params: query
    })
}