package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.CreateTableRequest
import com.autobill.smartpos.data.remote.dto.TableDto
import com.autobill.smartpos.data.remote.dto.TableListDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TableApiService {

    /** GET /api/v1/restaurants/{restaurantId}/tables */
    @GET("restaurants/{restaurantId}/tables")
    suspend fun getAllTables(
        @Path("restaurantId") restaurantId: Long,
    ): ApiResponse<TableListDto>

    /** GET /api/v1/restaurants/{restaurantId}/tables/{id} */
    @GET("restaurants/{restaurantId}/tables/{id}")
    suspend fun getTableById(
        @Path("restaurantId") restaurantId: Long,
        @Path("id") id: Long,
    ): ApiResponse<TableDto>

    /** GET /api/v1/restaurants/{restaurantId}/tables/available?capacity=4 */
    @GET("restaurants/{restaurantId}/tables/available")
    suspend fun getAvailableTables(
        @Path("restaurantId") restaurantId: Long,
        @Query("capacity") minCapacity: Int? = null,
    ): ApiResponse<List<TableDto>>

    /** GET /api/v1/restaurants/{restaurantId}/tables/occupied */
    @GET("restaurants/{restaurantId}/tables/occupied")
    suspend fun getOccupiedTables(
        @Path("restaurantId") restaurantId: Long,
    ): ApiResponse<List<TableDto>>

    /** GET /api/v1/restaurants/{restaurantId}/tables/status/{status} */
    @GET("restaurants/{restaurantId}/tables/status/{status}")
    suspend fun getTablesByStatus(
        @Path("restaurantId") restaurantId: Long,
        @Path("status") status: String,
    ): ApiResponse<List<TableDto>>

    /** GET /api/v1/restaurants/{restaurantId}/tables/count/available */
    @GET("restaurants/{restaurantId}/tables/count/available")
    suspend fun countAvailableTables(
        @Path("restaurantId") restaurantId: Long,
    ): ApiResponse<Int>

    /** POST /api/v1/restaurants/{restaurantId}/tables */
    @POST("restaurants/{restaurantId}/tables")
    suspend fun createTable(
        @Path("restaurantId") restaurantId: Long,
        @Body request: CreateTableRequest,
    ): ApiResponse<TableDto>

    /** PUT /api/v1/restaurants/{restaurantId}/tables/{id} */
    @PUT("restaurants/{restaurantId}/tables/{id}")
    suspend fun updateTable(
        @Path("restaurantId") restaurantId: Long,
        @Path("id") id: Long,
        @Body request: CreateTableRequest,
    ): ApiResponse<TableDto>

    /**
     * PATCH /api/v1/restaurants/{restaurantId}/tables/{id}/status?newStatus=OCCUPIED
     * Status passed as query param — no body required.
     * API contract uses camelCase: ?newStatus= (see API_REFERENCE.md §20)
     */
    @PATCH("restaurants/{restaurantId}/tables/{id}/status")
    suspend fun updateTableStatus(
        @Path("restaurantId") restaurantId: Long,
        @Path("id") id: Long,
        @Query("newStatus") newStatus: String,
    ): ApiResponse<TableDto>

    /** DELETE /api/v1/restaurants/{restaurantId}/tables/{id} */
    @DELETE("restaurants/{restaurantId}/tables/{id}")
    suspend fun deleteTable(
        @Path("restaurantId") restaurantId: Long,
        @Path("id") id: Long,
    ): ApiResponse<String>
}

