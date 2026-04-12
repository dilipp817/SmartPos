package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.BillDto
import com.autobill.smartpos.data.remote.dto.CreateOrderRequest
import com.autobill.smartpos.data.remote.dto.OrderDto
import com.autobill.smartpos.data.remote.dto.OrderItemRequestDto
import com.autobill.smartpos.data.remote.dto.OrderListDto
import com.autobill.smartpos.data.remote.dto.PendingOrdersCountDto
import com.autobill.smartpos.data.remote.dto.UpdateOrderItemRequest
import com.autobill.smartpos.data.remote.dto.UpdateOrderStatusRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApiService {

    /** POST /api/v1/restaurants/{restaurantId}/orders */
    @POST("restaurants/{restaurantId}/orders")
    suspend fun createOrder(
        @Path("restaurantId") restaurantId: Long,
        @Body request: CreateOrderRequest,
    ): ApiResponse<OrderDto>

    /** GET /api/v1/restaurants/{restaurantId}/orders */
    @GET("restaurants/{restaurantId}/orders")
    suspend fun getAllOrders(
        @Path("restaurantId") restaurantId: Long,
    ): ApiResponse<OrderListDto>

    /** GET /api/v1/restaurants/{restaurantId}/orders/{orderId} */
    @GET("restaurants/{restaurantId}/orders/{orderId}")
    suspend fun getOrderById(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
    ): ApiResponse<OrderDto>

    /** GET /api/v1/restaurants/{restaurantId}/orders/status/{status} */
    @GET("restaurants/{restaurantId}/orders/status/{status}")
    suspend fun getOrdersByStatus(
        @Path("restaurantId") restaurantId: Long,
        @Path("status") status: String,
    ): ApiResponse<OrderListDto>

    /** GET /api/v1/restaurants/{restaurantId}/orders/active */
    @GET("restaurants/{restaurantId}/orders/active")
    suspend fun getActiveOrders(
        @Path("restaurantId") restaurantId: Long,
    ): ApiResponse<OrderListDto>

    /** GET /api/v1/restaurants/{restaurantId}/orders/count/pending */
    @GET("restaurants/{restaurantId}/orders/count/pending")
    suspend fun countPendingOrders(
        @Path("restaurantId") restaurantId: Long,
    ): ApiResponse<PendingOrdersCountDto>

    /** GET /api/v1/restaurants/{restaurantId}/orders/range?start_date=...&end_date=...
     * Both params required. ISO-8601 datetime e.g. "2026-04-12T00:00:00" */
    @GET("restaurants/{restaurantId}/orders/range")
    suspend fun getOrdersByDateRange(
        @Path("restaurantId") restaurantId: Long,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): ApiResponse<OrderListDto>

    /** GET /api/v1/restaurants/{restaurantId}/orders/search?q=...
     * Matches order number, table number, or status keyword. */
    @GET("restaurants/{restaurantId}/orders/search")
    suspend fun searchOrders(
        @Path("restaurantId") restaurantId: Long,
        @Query("q") query: String,
    ): ApiResponse<OrderListDto>

    /** PATCH /api/v1/restaurants/{restaurantId}/orders/{orderId}/status */
    @PATCH("restaurants/{restaurantId}/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
        @Body request: UpdateOrderStatusRequest,
    ): ApiResponse<OrderDto>

    /** POST /api/v1/restaurants/{restaurantId}/orders/{orderId}/items */
    @POST("restaurants/{restaurantId}/orders/{orderId}/items")
    suspend fun addItemToOrder(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
        @Body request: OrderItemRequestDto,
    ): ApiResponse<OrderDto>

    /** PUT /api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId} */
    @PUT("restaurants/{restaurantId}/orders/{orderId}/items/{itemId}")
    suspend fun updateOrderItem(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
        @Path("itemId") itemId: Long,
        @Body request: UpdateOrderItemRequest,
    ): ApiResponse<OrderDto>

    /** PATCH /api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status?new_status=DONE */
    @PATCH("restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status")
    suspend fun updateOrderItemStatus(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
        @Path("itemId") itemId: Long,
        @Query("new_status") newStatus: String,
    ): ApiResponse<OrderDto>

    /** DELETE /api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId} */
    @DELETE("restaurants/{restaurantId}/orders/{orderId}/items/{itemId}")
    suspend fun removeItemFromOrder(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
        @Path("itemId") itemId: Long,
    ): ApiResponse<OrderDto>

    /** DELETE /api/v1/restaurants/{restaurantId}/orders/{orderId} */
    @DELETE("restaurants/{restaurantId}/orders/{orderId}")
    suspend fun cancelOrder(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
    ): ApiResponse<OrderDto>

    /**
     * POST /api/v1/restaurants/{restaurantId}/orders/{orderId}/generate-bill
     * Auto-generates bill with 18% GST (9% CGST + 9% SGST). Preferred over manual bill creation.
     * Optional query param: discount (default: 0) — discount amount in rupees (backendapi.md §4).
     */
    @POST("restaurants/{restaurantId}/orders/{orderId}/generate-bill")
    suspend fun generateBill(
        @Path("restaurantId") restaurantId: Long,
        @Path("orderId") orderId: Long,
        @Query("discount") discount: Double = 0.0,
    ): ApiResponse<BillDto>
}

