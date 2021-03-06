package io.olaph.slack.client.group.users

interface UsersMethodGroup {


    fun info(authToken: String): UsersInfoMethod

    fun list(authToken: String): UserListMethod

    fun conversations(authToken: String): UserConversationsMethod

}
