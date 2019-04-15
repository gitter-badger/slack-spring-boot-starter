package io.olaph.slack.client.spring.group.channels

import io.olaph.slack.client.ErrorResponseException
import io.olaph.slack.client.group.ApiCallResult
import io.olaph.slack.client.group.channels.ChannelsInfoMethod
import io.olaph.slack.client.spring.group.SlackRequestBuilder
import io.olaph.slack.dto.jackson.group.channels.ErrorGetChannelInfoResponse
import io.olaph.slack.dto.jackson.group.channels.SlackGetChannelInfoResponse
import io.olaph.slack.dto.jackson.group.channels.SuccessfulGetChannelInfoResponse

@Suppress("UNCHECKED_CAST")
class DefaultGetChannelInfoMethod(private val authToken: String) : ChannelsInfoMethod() {

    override fun request(): ApiCallResult<SuccessfulGetChannelInfoResponse, ErrorGetChannelInfoResponse> {
        val response = SlackRequestBuilder<SlackGetChannelInfoResponse>(authToken)
                .with(this.params)
                .toMethod("channels.info")
                .returnAsType(SlackGetChannelInfoResponse::class.java)
                .postUrlEncoded(mapOf(Pair("token", authToken), Pair("channel", this.params.channel)))

        return when (response.body!!) {
            is SuccessfulGetChannelInfoResponse -> {
                val responseEntity = response.body as SuccessfulGetChannelInfoResponse
                this.onSuccess?.invoke(responseEntity)
                ApiCallResult(success = responseEntity)
            }
            is ErrorGetChannelInfoResponse -> {
                val responseEntity = response.body as ErrorGetChannelInfoResponse
                if (!response.statusCode.is2xxSuccessful) {
                    throw ErrorResponseException(this::class, response.statusCode.name, responseEntity.error)
                }
                this.onFailure?.invoke(responseEntity)
                ApiCallResult(failure = responseEntity)
            }
        }
    }
}
