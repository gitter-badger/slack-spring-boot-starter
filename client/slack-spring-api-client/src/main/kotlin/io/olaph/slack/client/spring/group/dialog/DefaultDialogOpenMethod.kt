package io.olaph.slack.client.spring.group.dialog

import io.olaph.slack.client.ErrorResponseException
import io.olaph.slack.client.group.ApiCallResult
import io.olaph.slack.client.group.dialog.DialogOpenMethod
import io.olaph.slack.client.spring.group.SlackRequestBuilder
import io.olaph.slack.dto.jackson.group.dialog.ErrorOpenDialogResponse
import io.olaph.slack.dto.jackson.group.dialog.SlackOpenDialogResponse
import io.olaph.slack.dto.jackson.group.dialog.SuccessfulOpenDialogResponse

@Suppress("UNCHECKED_CAST")
class DefaultDialogOpenMethod(private val authToken: String) : DialogOpenMethod() {

    override fun request(): ApiCallResult<SuccessfulOpenDialogResponse, ErrorOpenDialogResponse> {
        val response = SlackRequestBuilder<SlackOpenDialogResponse>(authToken)
                .with(this.params)
                .toMethod("dialog.open")
                .returnAsType(SlackOpenDialogResponse::class.java)
                .postWithJsonBody()

        return when (response.body!!) {
            is SuccessfulOpenDialogResponse -> {
                val responseEntity = response.body as SuccessfulOpenDialogResponse
                this.onSuccess?.invoke(responseEntity)
                ApiCallResult(success = responseEntity)
            }
            is ErrorOpenDialogResponse -> {
                val responseEntity = response.body as ErrorOpenDialogResponse
                if (!response.statusCode.is2xxSuccessful) {
                    throw ErrorResponseException(this::class, response.statusCode.name, responseEntity.error)
                }
                this.onFailure?.invoke(responseEntity)
                ApiCallResult(failure = responseEntity)
            }
        }
    }
}
