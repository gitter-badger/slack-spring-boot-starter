package io.olaph.slack.client.spring.group.chat

import io.olaph.slack.client.group.ApiCallResult
import io.olaph.slack.client.group.chat.ChatDeleteMethod
import io.olaph.slack.client.spring.group.SlackRequestBuilder
import io.olaph.slack.dto.jackson.group.chat.ErrorChatDeleteResponse
import io.olaph.slack.dto.jackson.group.chat.SlackDeleteResponse
import io.olaph.slack.dto.jackson.group.chat.SuccessfulChatDeleteResponse

@Suppress("UNCHECKED_CAST")
class DefaultDeleteMethod(private val authToken: String) : ChatDeleteMethod() {

    override fun request(): ApiCallResult<SuccessfulChatDeleteResponse, ErrorChatDeleteResponse> {
        val response = SlackRequestBuilder<SlackDeleteResponse>(authToken)
                .with(this.params)
                .toMethod("chat.delete")
                .returnAsType(SlackDeleteResponse::class.java)
                .postWithJsonBody()

        return when (response.body!!) {
            is SuccessfulChatDeleteResponse -> {
                val responseEntity = response.body as SuccessfulChatDeleteResponse
                this.onSuccess?.invoke(responseEntity)
                ApiCallResult(success = responseEntity)
            }
            is ErrorChatDeleteResponse -> {
                val responseEntity = response.body as ErrorChatDeleteResponse
                this.onFailure?.invoke(responseEntity)
                ApiCallResult(failure = responseEntity)
            }
        }
    }
}
