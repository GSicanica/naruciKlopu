package com.appbosna.data.remote.graphql

object GraphQLQueries {

    // ----------- PRODUCTS -----------
    const val GET_PRODUCTS = """
        query GetProducts(${'$'}categoryId: Int) {
          products(categoryId: ${'$'}categoryId) {
            id
            title
            description
            price
            category_id
            image
          }
        }
    """

    const val GET_PRODUCT = """
        query GetProduct(${'$'}id: Int!) {
          product(id: ${'$'}id) {
            id
            title
            description
            price
            category_id
            image
          }
        }
    """

    // ----------- CATEGORIES -----------
    const val GET_CATEGORIES = """
        query GetCategories {
          categories {
            id
            name
            image
            is_active
          }
        }
    """

    // ----------- AUTH -----------
    const val GET_ME = """
        query Me {
          me {
            id
            name
            email
            displayName
            photoUrl
            uid
            phoneNumber
            is_admin
          }
        }
    """

    // ----------- CUSTOMER -----------
    const val GET_CUSTOMER = """
        query GetCustomer {
          customer {
            id
            first_name
            last_name
            email
            city
            postal_code
            address
            phone
          }
        }
    """

    // ----------- CART -----------
    const val GET_CART = """
        query GetCart {
          cart {
            id
            product_id
            title
            price
            quantity
            thumbnail
          }
        }
    """

    // ----------- ORDERS -----------
    const val GET_MY_ORDERS = """
        query GetMyOrders {
          orders {
            id
            user_id
            total_amount
            coupon_code
            coupon_discount
            created_at
          }
        }
    """

    const val GET_ORDER = """
        query GetOrder(${'$'}id: Int!) {
          order(id: ${'$'}id) {
            id
            user_id
            total_amount
            coupon_code
            coupon_discount
            created_at
          }
        }
    """

    // ----------- RESTAURANTS -----------
    const val GET_RESTAURANTS = """
        query GetRestaurants {
          restaurants {
            id
            name
            color
            image
            sort_order
            active
          }
        }
    """

    // ----------- SETTINGS -----------
    const val GET_SETTINGS = """
        query GetSettings {
          settings {
            app_name
            currency
            tax_rate
            delivery_fee
            min_order_amount
            support_email
            support_phone
          }
        }
    """

    // ----------- ADMIN -----------
    const val ADMIN_GET_USERS = """
        query AdminGetUsers {
          adminUsers {
            id
            name
            email
            created_at
          }
        }
    """

    const val ADMIN_GET_ORDERS = """
        query AdminGetOrders(${'$'}start: Long!, ${'$'}end: Long!) {
          adminOrders(start: ${'$'}start, end: ${'$'}end) {
            id
            total_amount
            created_at
            coupon_code
            coupon_discount
            items {
              product_id
              title
              price
              quantity
              thumbnail
            }
          }
        }
    """

    const val ADMIN_GET_USERS_COUNT = """
        query AdminGetUsersCount {
          adminUsersCount
        }
    """

    const val ADMIN_GET_ORDERS_COUNT = """
        query AdminGetOrdersCount {
          adminOrdersCount
        }
    """
}
