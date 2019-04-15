package io.olaph.slack.client.spring.group.channels

import io.olaph.slack.client.ErrorResponseException
import io.olaph.slack.client.group.ApiCallResult
import io.olaph.slack.client.group.channels.ChannelsCreateMethod
import io.olaph.slack.client.spring.group.SlackRequestBuilder
import io.olaph.slack.dto.jackson.group.channels.ErrorChannelCreateResponse
import io.olaph.slack.dto.jackson.group.channels.SlackChannelCreateResponse
import io.olaph.slack.dto.jackson.group.channels.SuccessfulChannelCreateResponse


@Suppress("UNCHECKED_CAST")
class DefaultChannelCreateMethod(private val authToken: String) : ChannelsCreateMethod() {
    override fun request(): ApiCallResult<SuccessfulChannelCreateResponse, ErrorChannelCreateResponse> {

        val response = SlackRequestBuilder<SlackChannelCreateResponse>(authToken)
                .with(this.params)
                .toMethod("channels.create")
                .returnAsType(SlackChannelCreateResponse::class.java)
                .postWithJsonBody()

        return when (response.body!!) {
            is SuccessfulChannelCreateResponse -> {
                val responseEntity = response.body as SuccessfulChannelCreateResponse
                this.onSuccess?.invoke(responseEntity)
                ApiCallResult(success = responseEntity)
            }
            is ErrorChannelCreateResponse -> {
                val responseEntity = response.body as ErrorChannelCreateResponse
                if (!response.statusCode.is2xxSuccessful) {
                    throw ErrorResponseException(this::class, response.statusCode.name, responseEntity.error)
                }
                this.onFailure?.invoke(responseEntity)
                ApiCallResult(failure = responseEntity)
            }
        }
    }
}
