package io.olaph.slack.broker.broker

import io.olaph.slack.broker.metrics.InstallationMetricsCollector
import io.olaph.slack.broker.receiver.InstallationReceiver
import io.olaph.slack.broker.store.Team
import io.olaph.slack.broker.store.TeamStore
import io.olaph.slack.client.SlackClient
import io.olaph.slack.dto.jackson.group.oauth.OauthAccessRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

/**
 * InstallationBroker provides an endpoint that is responsible for installation requests.
 *
 * It will execute all registered [InstallationReceiver]s on an installation request
 * If **all** [InstallationReceiver]s * execute successfully (return without exception) will redirect to a configurable successRedirectUrl.
 * If any of the registered [InstallationReceiver]s throws any exception it will redirect to another configurable errorRedirectUrl
 *
 * Notes:
 * At this point there is no guarantee on the execution order of the [InstallationReceiver]s
 */
@RestController
class InstallationBroker constructor(
        private val installationReceivers: List<InstallationReceiver>,
        private val metricsCollector: InstallationMetricsCollector?,
        private val teamStore: TeamStore,
        private val slackClient: SlackClient,
        private val config: Config) {

    companion object {
        private val LOG = LoggerFactory.getLogger(InstallationBroker::class.java)
    }

    /**
     * Installation-endpoint which is called by slack
     * Obtains the token by calling [oauth.access](https://api.slack.com/methods/oauth.access) and saves the response into the TeamStore
     */
    @GetMapping(value = ["/installation"])
    fun onInstall(@RequestParam("code") code: String, @RequestParam("state") state: String): RedirectView {
        return try {

            this.metricsCollector?.installationAttempt()

            val team = obtainOauthAccess(code)
            this.teamStore.put(team)
            this.metricsCollector?.successfulInstallation()

            this.installationReceivers
                    .forEach { receiver ->
                        try {
                            this.metricsCollector?.receiverExecuted()
                            receiver.onReceiveInstallation(code, state, team)
                        } catch (e: Exception) {
                            this.metricsCollector?.receiverExecutionError()
                            InteractiveComponentBroker.LOG.error("{}", e)
                        }
                    }

            RedirectView(this.config.successRedirectUrl)
        } catch (exception: Exception) {
            LOG.error("There was an error during the installation", exception)
            this.metricsCollector?.errorDuringInstallation()
            RedirectView(this.config.errorRedirectUrl)
        }
    }

    private fun obtainOauthAccess(code: String): Team {
        return this.slackClient.oauth().access()
                .with(OauthAccessRequest(config.clientId, config.clientSecret, code))
                .invoke().success?.let {
            Team(it.teamId,
                    it.teamName,
                    Team.IncomingWebhook(it.incomingWebhook.channel,
                            it.incomingWebhook.channelId,
                            it.incomingWebhook.configurationUrl,
                            it.incomingWebhook.url),
                    Team.Bot(it.bot.botUserId,
                            it.bot.botAccessToken)
            )
        } ?: throw IllegalStateException("Could not obtain access-token")
    }

    data class Config(val clientId: String, val clientSecret: String, val successRedirectUrl: String, val errorRedirectUrl: String)


}
