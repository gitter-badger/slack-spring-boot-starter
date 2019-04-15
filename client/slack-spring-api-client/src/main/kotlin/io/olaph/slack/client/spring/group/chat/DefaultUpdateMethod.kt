package io.olaph.slack.client.spring.group.chat

import io.olaph.slack.client.group.ApiCallResult
import io.olaph.slack.client.group.chat.ChatUpdateMethod
import io.olaph.slack.client.spring.group.SlackRequestBuilder
import io.olaph.slack.dto.jackson.group.chat.ErrorChatUpdateResponse
import io.olaph.slack.dto.jackson.group.chat.SlackChatUpdateResponse
import io.olaph.slack.dto.jackson.group.chat.SuccessfulChatUpdateResponse

@Suppress("UNCHECKED_CAST")
class DefaultUpdateMethod(private val authToken: String) : ChatUpdateMethod() {

    override fun request(): ApiCallResult<SuccessfulChatUpdateResponse, ErrorChatUpdateResponse> {
        val response = SlackRequestBuilder<SlackChatUpdateResponse>(authToken)
                .with(this.params)
                .toMethod("chat.update")
                .returnAsType(SlackChatUpdateResponse::class.java)
                .postWithJsonBody()

        return when (response.body!!) {
            is SuccessfulChatUpdateResponse -> {
                val responseEntity = response.body as SuccessfulChatUpdateResponse
                this.onSuccess?.invoke(responseEntity)
                ApiCallResult(success = responseEntity)
            }
            is ErrorChatUpdateResponse -> {
                val responseEntity = response.body as ErrorChatUpdateResponse
                this.onFailure?.invoke(responseEntity)
                ApiCallResult(failure = responseEntity)
            }
        }
    }
}
