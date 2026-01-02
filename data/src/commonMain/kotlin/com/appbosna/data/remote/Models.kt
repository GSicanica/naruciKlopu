package com.appbosna.data.remote

import android.os.Build
import androidx.annotation.RequiresApi
import com.appbosna.data.usecase.toIntSafe
import com.appbosna.shared.domain.CartItem
import com.appbosna.shared.domain.Customer
import com.appbosna.shared.domain.OpenStatus
import com.appbosna.shared.domain.Order
import com.appbosna.shared.domain.OrderItem
import com.appbosna.shared.domain.Product
import com.appbosna.shared.domain.Restaurant
import java.time.Duration
import java.time.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* ============================================================
 * VALIDATION EXTENSIONS
 * ============================================================ */

/**
 * Extension functions for safe type conversion with validation
 */
fun String?.toIntSafeOrDefault(default: Int = 0): Int = 
    this?.toIntOrNull() ?: default

fun String?.toBooleanSafe(trueValue: String = "1"): Boolean = 
    this == trueValue

fun String?.toDoubleSafeOrDefault(default: Double = 0.0): Double = 
    this?.toDoubleOrNull() ?: default

fun String.isValidEmail(): Boolean = 
    matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))

fun String.isValidPhone(): Boolean = 
    matches(Regex("^[+]?[0-9]{10,15}$"))

fun String?.orDefault(default: String): String = 
    if (isNullOrBlank()) default else this

/* ============================================================
 * AUTH
 * ============================================================ */

@Serializable
data class ApiUser(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("displayName") val displayName: String = "",
    @SerialName("photoUrl") val photoUrl: String = "",
    @SerialName("uid") val uid: String = "",
    @SerialName("phoneNumber") val phoneNumber: String = "",
    @SerialName("is_admin") val isAdmin: String = ""
) {
    /**
     * Validates if the user data is complete
     */
    fun isValid(): Boolean = 
        id.isNotBlank() && 
        name.isNotBlank() && 
        email.isNotBlank() && 
        email.isValidEmail()
    
    /**
     * Checks if user has admin privileges
     */
    fun isAdminUser(): Boolean = isAdmin == "1" || isAdmin.equals("true", ignoreCase = true)
    
    /**
     * Get display name with fallback
     */
    fun getDisplayNameOrDefault(): String = displayName.ifBlank { name.ifBlank { email } }
}

@Serializable
data class LoginReq(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
) {
    /**
     * Validates login request data
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (email.isBlank()) errors.add("Email is required")
        else if (!email.isValidEmail()) errors.add("Invalid email format")
        
        if (password.isBlank()) errors.add("Password is required")
        else if (password.length < 6) errors.add("Password must be at least 6 characters")
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

@Serializable
data class RegisterReq(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
) {
    /**
     * Validates registration request data
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) errors.add("Name is required")
        else if (name.length < 2) errors.add("Name must be at least 2 characters")
        
        if (email.isBlank()) errors.add("Email is required")
        else if (!email.isValidEmail()) errors.add("Invalid email format")
        
        if (password.isBlank()) errors.add("Password is required")
        else if (password.length < 8) errors.add("Password must be at least 8 characters")
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()
}

/**
 * Check if validation is valid
 */
fun ValidationResult.isValid(): Boolean = this is ValidationResult.Valid

/**
 * Get error messages
 */
fun ValidationResult.getErrorMessages(): List<String> = when (this) {
    is ValidationResult.Invalid -> errors
    is ValidationResult.Valid -> emptyList()
}

/**
 * Odgovara AuthTokenResponse (login / google-login).
 * Fields are nullable to handle API errors gracefully.
 */
@Serializable
data class LoginRes(
    @SerialName("token") val token: String = "",
    @SerialName("token_expires_in") val tokenExpiresIn: Long? = null,
    @SerialName("token_expires_at") val tokenExpiresAt: Long? = null,
    @SerialName("user") val user: ApiUser? = null,
    @SerialName("error") val error: String? = null  // For error responses
)

/**
 * Stvarni odgovor za /api/auth/me (nema token polja, samo user + token_expires_at).
 */
@Serializable
data class MeRes(
    @SerialName("user") val user: ApiUser,
    @SerialName("token_expires_at") val tokenExpiresAt: Long? = null
)

/* ============================================================
 * CATEGORY
 * ============================================================ */

@Serializable
data class CategoryDto(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = ""
)

/* ============================================================
 * PRODUCT
 * ============================================================ */

@Serializable
data class ProductDto(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("price") val price: String = "",
    @SerialName("restaurant_id") val restaurantId: String = "0",
    @SerialName("thumbnail") val thumbnail: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("restaurant_name") val restaurantName: String = "",
    @SerialName("category_name") val categoryName: String = "",
    @SerialName("category_id") val categoryId: String = "",
    @SerialName("is_new") val isNew: String = "0",
    @SerialName("is_discounted") val isDiscounted: String = "0",
    @SerialName("is_popular") val isPopular: String = "0",
    @SerialName("flavors") val flavors: String = "",
    @SerialName("weight") val weight: String = "",
    @SerialName("is_active") val isActive: String = "1",
    @SerialName("sort_order") val sortOrder: String = "0"
) {
    /**
     * Converts DTO to domain model with proper type conversion and validation
     */
    fun toDomain() = Product(
        id = id,
        createdAt = createdAt,
        title = title,
        description = description.orEmpty(),
        thumbnail = thumbnail,
        restaurantId = restaurantId.toIntSafe(),
        restaurantName = restaurantName,
        categoryName = categoryName.ifBlank { null },
        categoryId = categoryId.toIntOrNull(),
        flavors = flavors,
        ingredients = emptyList(),
        weight = weight.ifBlank { null },
        price = price,
        isNew = isNew.toBooleanSafe(),
        isDiscounted = isDiscounted.toBooleanSafe(),
        isPopular = isPopular.toBooleanSafe(),
        isActive = isActive.toBooleanSafe("1"),
        sortOrder = sortOrder.toIntSafeOrDefault(0)
    )
    
    /**
     * Validates product data
     */
    fun isValid(): Boolean = 
        title.isNotBlank() && 
        price.toDoubleOrNull()?.let { it > 0 } == true &&
        restaurantId.toIntOrNull()?.let { it > 0 } == true
    
    /**
     * Gets price as Double with fallback
     */
    fun getPriceAsDouble(): Double = price.toDoubleSafeOrDefault(0.0)
}


fun Product.toDtoForCreate() = ProductDto(
    id = "0",
    title = title,
    description = description,
    price = price,
    restaurantId = restaurantId.toString(),
    thumbnail = thumbnail,
    createdAt = "",
    restaurantName = restaurantName ?: "",
    categoryName = categoryName ?: "",
    categoryId = categoryId?.toString() ?: "",
    isNew = if (isNew) "1" else "0",
    isDiscounted = if (isDiscounted) "1" else "0",
    isPopular = if (isPopular) "1" else "0",
    flavors = flavors ?: "",
    weight = weight ?: "",
    isActive = if (isActive) "1" else "0",
    sortOrder = sortOrder.toString()
)

fun Product.toDtoForUpdate() = ProductDto(
    id = id,
    title = title,
    description = description,
    price = price,
    restaurantId = restaurantId.toString(),
    thumbnail = thumbnail,
    createdAt = createdAt,
    restaurantName = restaurantName ?: "",
    categoryName = categoryName ?: "",
    categoryId = categoryId?.toString() ?: "",
    isNew = if (isNew) "1" else "0",
    isDiscounted = if (isDiscounted) "1" else "0",
    isPopular = if (isPopular) "1" else "0",
    flavors = flavors ?: "",
    weight = weight ?: "",
    isActive = if (isActive) "1" else "0",
    sortOrder = sortOrder.toString()
)

/* ============================================================
 * CART
 * ============================================================ */

@Serializable
data class CartItemDto(
    @SerialName("id") val id: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("price") val price: String = "",
    @SerialName("quantity") val quantity: String = "",
    @SerialName("thumbnail") val thumbnail: String = "",
    @SerialName("flavor") val flavor: String? = null
)

@OptIn(ExperimentalUuidApi::class)
fun CartItemDto.toDomain() = CartItem(
    id = id.ifBlank { Uuid.random().toHexString() },
    productId = productId,
    title = title,
    price = price,
    quantity = quantity,
    thumbnail = thumbnail.ifBlank { null },
    flavor = flavor
)

fun CartItem.toDto() = CartItemDto(
    id = id,
    productId = productId,
    title = title,
    price = price,
    quantity = quantity,
    thumbnail = thumbnail ?: "",
    flavor = flavor
)

/* ============================================================
 * CUSTOMER
 * ============================================================ */

@Serializable
data class CustomerDto(
    @SerialName("id") val id: String = "",
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("city") val city: String = "",
    @SerialName("postal_code") val postalCode: String = "",
    @SerialName("address") val address: String = "",
    @SerialName("phone") val phone: String = "",
    @SerialName("cart") val cart: List<CartItemDto> = emptyList()
)

fun CustomerDto.toDomain(apiUser: ApiUser? = null): Customer = Customer(
    id = id.ifBlank { apiUser?.id ?: "" },
    firstName = firstName,
    lastName = lastName,
    email = email.ifBlank { apiUser?.email ?: "" },
    city = city.ifBlank { null },
    postalCode = postalCode.toIntOrNull(),
    address = address.ifBlank { null },
    phoneNumber = phone.ifBlank { null },
    cart = cart.map { it.toDomain() },
    isAdmin = apiUser?.isAdmin ?: ""
)

fun Customer.toDto() = CustomerDto(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    city = city ?: "",
    postalCode = postalCode?.toString() ?: "",
    address = address ?: "",
    phone = phoneNumber ?: "",
    cart = cart.map { it.toDto() }
)

/* ============================================================
 * ORDER (user side)
 * ============================================================ */

@Serializable
data class OrderDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("total_amount") val totalAmount: String = "",
    @SerialName("coupon_code") val couponCode: String? = null,
    @SerialName("coupon_discount") val couponDiscount: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

/* ============================================================
 * RESTAURANT WITH OPEN STATUS
 * ============================================================ */

@Serializable
data class RestaurantDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("color") val color: String? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("sort_order") val sortOrder: String = "0",
    @SerialName("open_time") val openTime: String? = null,
    @SerialName("close_time") val closeTime: String? = null,
    @SerialName("active") val activeRaw: String = "0",
    @SerialName("is_open") val isOpenRaw: String = "0"
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun toDomain(): Restaurant {
        val status = calculateOpenStatus(openTime, closeTime)

        return Restaurant(
            id = id,
            name = name,
            color = color,
            image = image,
            active = activeRaw == "1",
            sortOrder = sortOrder.toIntOrNull() ?: 0,
            openTime = openTime,
            closeTime = closeTime,
            isOpen = status == OpenStatus.OPEN,
            openStatus = status
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateOpenStatus(open: String?, close: String?): OpenStatus {
    if (open.isNullOrBlank() || close.isNullOrBlank()) return OpenStatus.CLOSED

    val now = LocalTime.now()

    val tOpen = runCatching { LocalTime.parse(open) }.getOrNull() ?: return OpenStatus.CLOSED
    val tClose = runCatching { LocalTime.parse(close) }.getOrNull() ?: return OpenStatus.CLOSED

    if (now.isBefore(tOpen) || now.isAfter(tClose)) return OpenStatus.CLOSED

    val minutesLeft = Duration.between(now, tClose).toMinutes()

    return when {
        minutesLeft < 30 -> OpenStatus.CLOSING_SOON
        else -> OpenStatus.OPEN
    }
}

/* ============================================================
 * ADMIN DTOs
 * ============================================================ */

@Serializable
data class AdminOrderDto(
    @SerialName("id") val id: Int,
    @SerialName("total_amount") val totalAmount: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("coupon_code") val couponCode: String? = null,
    @SerialName("coupon_discount") val couponDiscount: String? = null,
    @SerialName("items") val items: List<AdminOrderItemDto>
)

@Serializable
data class AdminOrderItemDto(
    @SerialName("product_id") val productId: String,
    @SerialName("title") val title: String,
    @SerialName("price") val price: String,
    @SerialName("quantity") val quantity: String,
    @SerialName("thumbnail") val thumbnail: String? = null
)

@Serializable
data class AdminUserDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("created_at") val createdAt: String
)


@kotlinx.serialization.Serializable
data class OrderCartItemRequest(
    @kotlinx.serialization.SerialName("product_id") val productId: Int,
    @kotlinx.serialization.SerialName("title") val title: String,
    @kotlinx.serialization.SerialName("price") val price: String,
    @kotlinx.serialization.SerialName("quantity") val quantity: Int,
    @kotlinx.serialization.SerialName("thumbnail") val thumbnail: String?
)

@kotlinx.serialization.Serializable
data class OrderCreateRequest(
    @kotlinx.serialization.SerialName("total") val total: String,
    @kotlinx.serialization.SerialName("items") val items: List<OrderCartItemRequest>,
    @kotlinx.serialization.SerialName("coupon_code") val couponCode: String? = null,
    @kotlinx.serialization.SerialName("coupon_discount") val couponDiscount: String? = null
)




/* ============================================================
 * Admin DTO -> Domain (Order / Customer)
 * ============================================================ */

fun AdminOrderDto.toDomainOrder(): Order {
    return Order(
        customerId = "",
        orderId = id.toString(),
        items = items.map { it.toDomainCartItem() },
        totalAmount = totalAmount.toDoubleOrNull() ?: 0.0,
        createdAt = createdAt
    )
}

fun AdminOrderItemDto.toDomainCartItem(): CartItem {
    return CartItem(
        id = productId,
        productId = productId,
        title = title,
        price = price,
        quantity = quantity,
        thumbnail = thumbnail,
        flavor = null
    )
}

fun AdminOrderItemDto.toDomainItem(): OrderItem {
    return OrderItem(
        id = null,
        productId = productId,
        quantity = quantity,
        price = price,
        title = title,
        thumbnail = thumbnail
    )
}

fun AdminUserDto.toDomainCustomer(): Customer {
    val (first, last) = splitName(name)
    return Customer(
        id = id.toString(),
        firstName = first,
        lastName = last,
        email = email,
        city = null,
        postalCode = null,
        address = null,
        phoneNumber = null,
        createdAt = createdAt,
        cart = emptyList(),
        isAdmin = "1"
    )
}

/* ============================================================
 * MEDIA UPLOAD
 * ============================================================ */

@Serializable
data class UploadResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("filename") val filename: String = "",
    @SerialName("url") val url: String = ""
)

/* ============================================================
 * Utility (ime)
 * ============================================================ */

private fun splitName(full: String): Pair<String, String> {
    val p = full.trim().split("\\s+".toRegex())
    return when (p.size) {
        0 -> "" to ""
        1 -> p[0] to ""
        else -> p.first() to p.drop(1).joinToString(" ")
    }
}
