package io.olaph.slack.client.spring.group.chat

import io.olaph.slack.client.ErrorResponseException
import io.olaph.slack.client.group.ApiCallResult
import io.olaph.slack.client.group.chat.ChatPostMessageMethod
import io.olaph.slack.client.spring.group.SlackRequestBuilder
import io.olaph.slack.dto.jackson.group.chat.ErrorPostMessageResponse
import io.olaph.slack.dto.jackson.group.chat.SlackPostMessageResponse
import io.olaph.slack.dto.jackson.group.chat.SuccessfulPostMessageResponse

@Suppress("UNCHECKED_CAST")
class DefaultPostMessageMethod(private val authToken: String) : ChatPostMessageMethod() {

    override fun request(): ApiCallResult<SuccessfulPostMessageResponse, ErrorPostMessageResponse> {
        val response = SlackRequestBuilder<SlackPostMessageResponse>(authToken)
                .with(this.params)
                .toMethod("chat.postMessage")
                .returnAsType(SlackPostMessageResponse::class.java)
                .postWithJsonBody()

        return when (response.body!!) {
            is SuccessfulPostMessageResponse -> {
                val responseEntity = response.body as SuccessfulPostMessageResponse
                this.onSuccess?.invoke(responseEntity)
                ApiCallResult(success = responseEntity)
            }
            is ErrorPostMessageResponse -> {
                val responseEntity = response.body as ErrorPostMessageResponse
                if (!response.statusCode.is2xxSuccessful) {
                    throw ErrorResponseException(this::class, response.statusCode.name, responseEntity.error)
                }
                this.onFailure?.invoke(responseEntity)
                ApiCallResult(failure = responseEntity)
            }
        }
    }
}
