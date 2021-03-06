package io.olaph.slack.broker.receiver

import io.olaph.slack.broker.store.Team
import io.olaph.slack.dto.jackson.SlackCommand
import io.olaph.slack.dto.jackson.SlackEvent
import io.olaph.slack.dto.jackson.InteractiveComponentResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders

class SL4JLoggingReceiver : EventReceiver, SlashCommandReceiver, InteractiveComponentReceiver, InstallationReceiver {

    override fun onReceiveInstallation(code: String, state: String, team: Team) {
        LOG.info("Received installation request")
    }

    companion object {
        val LOG = LoggerFactory.getLogger(SL4JLoggingReceiver::class.java)!!
    }

    override fun onReceiveEvent(slackEvent: SlackEvent, headers: HttpHeaders, team: Team) {
        LOG.info("Received SlackCallBack    {}\nHeaders:  {}", slackEvent, headers)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        LOG.info("Received SlackCommand:    {}\nHeaders:    {}", slackCommand, headers)
    }

    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveComponentResponse, headers: HttpHeaders, team: Team) {
        LOG.info("Received Interactive Component:   {}\nHeaders:    {}", interactiveComponentResponse, headers)
    }
}
