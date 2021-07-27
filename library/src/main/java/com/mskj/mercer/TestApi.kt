package com.mskj.mercer

import com.mskj.mercer.annotate.Adaptive
import kotlinx.coroutines.flow.Flow
import retrofit2.http.DELETE
import retrofit2.http.Query


@Adaptive("https://gateway.ing.dev.ihk.ltd:11443")
interface TestApi {

    @DELETE("/orders/food/operation/order/takeaway/finish")
    fun test(
        @Query(value = "id", encoded = true) id: Long
    ): Flow<Any>

}