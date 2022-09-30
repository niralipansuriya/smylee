package com.urfeed.listener

import retrofit2.Call

interface OnResponse {
    fun onSuccess(code: Int, response: String)
    fun onFailure(call: Call<String>, t: Throwable)
}