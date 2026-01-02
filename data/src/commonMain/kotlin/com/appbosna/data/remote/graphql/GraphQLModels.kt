package com.appbosna.data.remote.graphql

import com.appbosna.data.remote.AdminOrderDto
import com.appbosna.data.remote.AdminOrderItemDto
import com.appbosna.data.remote.AdminUserDto
import com.appbosna.data.remote.CartItemDto
import com.appbosna.data.remote.CategoryDto
import com.appbosna.data.remote.CustomerDto
import com.appbosna.data.remote.LoginRes
import com.appbosna.data.remote.OrderDto
import com.appbosna.data.remote.ProductDto
import com.appbosna.data.remote.RestaurantDto
import kotlinx.serialization.Serializable

/* ============================================================
 * GRAPHQL RAW MODELS (backend može vratiti više ili manje polja)
 * ============================================================ */

@Serializable
data class GraphQLCategory(
    val id: Int? = null,
    val name: String? = null,
    val image: String? = null,
    val is_active: Boolean? = null
)

@Serializable
data class CategoriesResponse(val categories: List<GraphQLCategory> = emptyList())

@Serializable
data class GraphQLProduct(
    val id: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val category_id: Int? = null,
    val image: String? = null,
    val is_active: Boolean? = null,

    // Extended (REST-like)
    val restaurant_id: Int? = null,
    val created_at: String? = null,
    val restaurant_name: String? = null,
    val category_name: String? = null,
    val is_new: String? = null,
    val is_discounted: String? = null,
    val is_popular: String? = null,
    val flavors: String? = null,
    val weight: String? = null,
    val sort_order: Int? = null
)

@Serializable
data class ProductsResponse(val products: List<GraphQLProduct> = emptyList())

@Serializable
data class ProductResponse(val product: GraphQLProduct? = null)

@Serializable
data class GraphQLCartItem(
    val id: Int? = null,
    val product_id: Int? = null,
    val title: String? = null,
    val price: Double? = null,
    val quantity: Int? = null,
    val thumbnail: String? = null
)

@Serializable
data class GraphQLCustomer(
    val id: Int? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val city: String? = null,
    val postal_code: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val cart: List<GraphQLCartItem>? = null
)

@Serializable
data class CustomerResponse(val customer: GraphQLCustomer? = null)

@Serializable
data class GraphQLOrderItem(
    val id: Int? = null,
    val product_id: Int? = null,
    val title: String? = null,
    val price: Double? = null,
    val quantity: Int? = null,
    val thumbnail: String? = null
)

@Serializable
data class GraphQLOrder(
    val id: Int? = null,
    val user_id: Int? = null,
    val total_amount: Double? = null,
    val coupon_code: String? = null,
    val coupon_discount: Double? = null,
    val created_at: String? = null,
    val items: List<GraphQLOrderItem>? = null
)

@Serializable
data class OrderItemInput(
    val productId: Int,
    val quantity: Int
)

@Serializable
data class OrderInput(
    val total: String,
    val items: List<OrderItemInput>,
    val couponCode: String? = null,
    val couponDiscount: String? = null
)

@Serializable
data class OrdersResponse(val orders: List<GraphQLOrder> = emptyList())

@Serializable
data class OrderResponse(val order: GraphQLOrder? = null)

@Serializable
data class GraphQLRestaurant(
    val id: Int? = null,
    val name: String? = null,
    val color: String? = null,
    val image: String? = null,
    val sort_order: Int? = null,
    val open_time: String? = null,
    val close_time: String? = null,
    val active: String? = null,
    val is_open: String? = null
)

@Serializable
data class RestaurantsResponse(val restaurants: List<GraphQLRestaurant> = emptyList())

@Serializable
data class GraphQLSettings(
    val app_name: String? = null,
    val currency: String? = null,
    val tax_rate: String? = null,
    val delivery_fee: String? = null,
    val min_order_amount: String? = null,
    val support_email: String? = null,
    val support_phone: String? = null
)

@Serializable
data class SettingsResponse(val settings: GraphQLSettings? = null)

@Serializable
data class GraphQLAdminOrderItem(
    val product_id: Int? = null,
    val title: String? = null,
    val price: Double? = null,
    val quantity: Int? = null,
    val thumbnail: String? = null
)

@Serializable
data class GraphQLAdminOrder(
    val id: Int? = null,
    val total_amount: Double? = null,
    val created_at: String? = null,
    val coupon_code: String? = null,
    val coupon_discount: Double? = null,
    val items: List<GraphQLAdminOrderItem>? = null
)

@Serializable
data class AdminOrdersResponse(val adminOrders: List<GraphQLAdminOrder> = emptyList())

@Serializable
data class GraphQLAdminUser(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val created_at: String? = null
)

@Serializable
data class AdminUsersResponse(val adminUsers: List<GraphQLAdminUser> = emptyList())

@Serializable
data class CountResponse(val adminUsersCount: Int? = null, val adminOrdersCount: Int? = null)

@Serializable
data class GraphQLMeUser(
    val id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val uid: String? = null,
    val phoneNumber: String? = null,
    val is_admin: String? = null
)

@Serializable
data class MeResponse(val me: GraphQLMeUser? = null)

@Serializable
data class LoginResponse(val login: LoginRes)

@Serializable
data class RegisterResponse(val register: LoginRes)

@Serializable
data class LoginGoogleResponse(val loginOrRegisterWithGoogle: LoginRes)

@Serializable
data class CreateOrderResponse(val createOrder: GraphQLOrder)

/* ============================================================
 * SAFE MAPPERS (pretvaraju GraphQL modele u REST DTO modele)
 * ============================================================ */

fun GraphQLCategory.toDto(): CategoryDto = CategoryDto(
    id = (id ?: "").toString(),
    name = name ?: ""
)

fun GraphQLProduct.toDto(): ProductDto = ProductDto(
    id = (id ?: "").toString(),
    title = title ?: "",
    description = description,
    price = (price ?: 0.0).toString(),
    restaurantId = (restaurant_id ?: 0).toString(),
    thumbnail = image ?: "",
    createdAt = created_at ?: "",
    restaurantName = restaurant_name ?: "",
    categoryName = category_name ?: "",
    categoryId = (category_id ?: 0).toString(),
    isNew = is_new ?: "0",
    isDiscounted = is_discounted ?: "0",
    isPopular = is_popular ?: "0",
    flavors = flavors ?: "",
    weight = weight ?: "",
    isActive = if (is_active == true) "1" else "0",
    sortOrder = (sort_order ?: 0).toString()
)

fun GraphQLCartItem.toDto(): CartItemDto = CartItemDto(
    id = (id ?: "").toString(),
    productId = (product_id ?: "").toString(),
    title = title ?: "",
    price = (price ?: 0.0).toString(),
    quantity = (quantity ?: 1).toString(),
    thumbnail = thumbnail ?: ""
)

fun GraphQLCustomer.toDto(): CustomerDto = CustomerDto(
    id = (id ?: "").toString(),
    firstName = first_name ?: "",
    lastName = last_name ?: "",
    email = email ?: "",
    city = city ?: "",
    postalCode = postal_code ?: "",
    address = address ?: "",
    phone = phone ?: "",
    cart = cart?.map { it.toDto() } ?: emptyList()
)

fun GraphQLOrder.toDto(): OrderDto = OrderDto(
    id = (id ?: "").toString(),
    userId = (user_id ?: "").toString(),
    totalAmount = (total_amount ?: 0.0).toString(),
    couponCode = coupon_code,
    couponDiscount = coupon_discount?.toString(),
    createdAt = created_at ?: ""
)

fun GraphQLRestaurant.toDto(): RestaurantDto = RestaurantDto(
    id = id ?: 0,
    name = name ?: "",
    color = color,
    image = image,
    sortOrder = (sort_order ?: 0).toString(),
    openTime = open_time,
    closeTime = close_time,
    activeRaw = active ?: "0",
    isOpenRaw = is_open ?: "0"
)

fun GraphQLSettings.toMap(): Map<String, String> = buildMap {
    app_name?.let { put("app_name", it) }
    currency?.let { put("currency", it) }
    tax_rate?.let { put("tax_rate", it) }
    delivery_fee?.let { put("delivery_fee", it) }
    min_order_amount?.let { put("min_order_amount", it) }
    support_email?.let { put("support_email", it) }
    support_phone?.let { put("support_phone", it) }
}

fun GraphQLAdminOrder.toDto(): AdminOrderDto = AdminOrderDto(
    id = id ?: 0,
    totalAmount = (total_amount ?: 0.0).toString(),
    createdAt = created_at ?: "",
    couponCode = coupon_code,
    couponDiscount = coupon_discount?.toString(),
    items = items?.map {
        AdminOrderItemDto(
            productId = (it.product_id ?: "").toString(),
            title = it.title ?: "",
            price = (it.price ?: 0.0).toString(),
            quantity = (it.quantity ?: 1).toString(),
            thumbnail = it.thumbnail
        )
    } ?: emptyList()
)

fun GraphQLAdminUser.toDto(): AdminUserDto = AdminUserDto(
    id = id ?: 0,
    name = name ?: "",
    email = email ?: "",
    createdAt = created_at ?: ""
)
