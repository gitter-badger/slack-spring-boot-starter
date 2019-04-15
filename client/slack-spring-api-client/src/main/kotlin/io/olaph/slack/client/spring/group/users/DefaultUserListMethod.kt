package io.olaph.slack.client.spring.group.users

import io.olaph.slack.client.group.ApiCallResult
import io.olaph.slack.client.group.users.UserListMethod
import io.olaph.slack.client.spring.group.SlackRequestBuilder
import io.olaph.slack.dto.jackson.group.users.ErrorUserListResponse
import io.olaph.slack.dto.jackson.group.users.SuccessfulUserListResponse
import io.olaph.slack.dto.jackson.group.users.UserListResponse

@Suppress("UNCHECKED_CAST")
class DefaultUserListMethod(private val authToken: String) : UserListMethod() {

    override fun request(): ApiCallResult<SuccessfulUserListResponse, ErrorUserListResponse> {
        val response = SlackRequestBuilder<UserListResponse>(authToken)
                .toMethod("users.list")
                .returnAsType(UserListResponse::class.java)
                .postUrlEncoded(this.params.toRequestMap())

        return when (response.body!!) {
            is SuccessfulUserListResponse -> {
                val responseEntity = response.body as SuccessfulUserListResponse
                this.onSuccess?.invoke(responseEntity)
                ApiCallResult(success = responseEntity)
            }
            is ErrorUserListResponse -> {
                val responseEntity = response.body as ErrorUserListResponse
                this.onFailure?.invoke(responseEntity)
                ApiCallResult(failure = responseEntity)
            }
        }
    }
}
