package com.graphscout.service

import com.graphscout.data.RequestMetaInfo

object RequestVault {
    val vault = mutableMapOf<Int, RequestMetaInfo>()
    fun add(requestMetaInfo: RequestMetaInfo) {
        vault.put(requestMetaInfo.requestID, requestMetaInfo)
    }

    fun get(id: Int) : RequestMetaInfo? {
        return vault[id]
    }

    fun delete(id: Int) {
        vault.remove(id)
    }
}