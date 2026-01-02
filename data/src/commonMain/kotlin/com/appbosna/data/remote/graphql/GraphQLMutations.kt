package com.appbosna.data.remote.graphql

object GraphQLMutations {

    // ----------- AUTH -----------
    const val LOGIN = """
        mutation Login(${'$'}email: String!, ${'$'}password: String!) {
          login(email: ${'$'}email, password: ${'$'}password) {
            token
            token_expires_in
            user {
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
        }
    """

    const val REGISTER = """
        mutation Register(${'$'}name: String!, ${'$'}email: String!, ${'$'}password: String!) {
          register(name: ${'$'}name, email: ${'$'}email, password: ${'$'}password) {
            token
            token_expires_in
            user {
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
        }
    """

    // ----------- AUTH GOOGLE -----------
    const val LOGIN_OR_REGISTER_WITH_GOOGLE = """
        mutation LoginOrRegisterWithGoogle(
          ${'$'}name: String!,
          ${'$'}email: String!,
          ${'$'}uid: String!
        ) {
          loginOrRegisterWithGoogle(name: ${'$'}name, email: ${'$'}email, uid: ${'$'}uid) {
            token
            token_expires_in
            user {
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
        }
    """

    // ----------- CUSTOMER UPDATE -----------
    const val UPDATE_CUSTOMER = """
        mutation UpdateCustomer(
          ${'$'}firstName: String,
          ${'$'}lastName: String,
          ${'$'}email: String,
          ${'$'}city: String,
          ${'$'}postalCode: String,
          ${'$'}address: String,
          ${'$'}phone: String
        ) {
          updateCustomer(
            first_name: ${'$'}firstName,
            last_name: ${'$'}lastName,
            email: ${'$'}email,
            city: ${'$'}city,
            postal_code: ${'$'}postalCode,
            address: ${'$'}address,
            phone: ${'$'}phone
          )
        }
    """

    // ----------- CART -----------
    const val ADD_CART_ITEM = """
        mutation AddCartItem(${'$'}productId: Int!, ${'$'}quantity: Int!) {
          addCartItem(product_id: ${'$'}productId, quantity: ${'$'}quantity) {
            id
          }
        }
    """

    const val UPDATE_CART_ITEM = """
        mutation UpdateCartItem(${'$'}productId: Int!, ${'$'}quantity: Int!) {
          updateCartItem(product_id: ${'$'}productId, quantity: ${'$'}quantity) {
            id
          }
        }
    """

    const val DELETE_CART_ITEM = """
        mutation DeleteCartItem(${'$'}id: Int!) {
          deleteCartItem(id: ${'$'}id)
        }
    """

    const val CLEAR_CART = """
        mutation ClearCart {
          clearCart
        }
    """

    // ----------- ORDER CREATE -----------
    const val CREATE_ORDER = """
        mutation CreateOrder(
          ${'$'}items: [OrderItemInput!]!,
          ${'$'}totalAmount: String!,
          ${'$'}couponCode: String,
          ${'$'}couponDiscount: String
        ) {
          createOrder(
            items: ${'$'}items,
            total_amount: ${'$'}totalAmount,
            coupon_code: ${'$'}couponCode,
            coupon_discount: ${'$'}couponDiscount
          ) {
            id
          }
        }
    """

    // ----------- PRODUCT CRUD (admin) -----------
    const val CREATE_PRODUCT = """
        mutation CreateProduct(${'$'}input: ProductInput!) {
          createProduct(input: ${'$'}input) {
            id
          }
        }
    """

    const val UPDATE_PRODUCT = """
        mutation UpdateProduct(${'$'}id: Int!, ${'$'}input: ProductInput!) {
          updateProduct(id: ${'$'}id, input: ${'$'}input)
        }
    """

    const val DELETE_PRODUCT = """
        mutation DeleteProduct(${'$'}id: Int!) {
          deleteProduct(id: ${'$'}id)
        }
    """

    // ----------- MEDIA DELETE -----------
    const val DELETE_IMAGE = """
        mutation DeleteImage(${'$'}filename: String!) {
          deleteImage(filename: ${'$'}filename)
        }
    """
}
