package com.hsy.btverification2.entity

/**
 * @项目名: BTVerification2
 * @类位置: com.hsy.btverification2.entity
 * @创始人: hsy
 * @创建时间: 2021/9/17 15:00
 * @类描述:
 * @修改人: hsy
 * @修改时间: 2021/9/17 15:00
 * @修改描述:
 */
class TokenInfo(
    var client_id: String,
    var client_secret: String,
    var grant_type: String,
    var scope: String,
) {
}