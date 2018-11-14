package com.github.alpert

import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory

class SlackBot {
    val standups = linkedMapOf<SlackChannel, Standup>()

    fun handleSlackMessage(event: SlackMessagePosted, session: SlackSession) {
        if (!event.channel.isDirect && event.messageContent.contains("start standup")) {
            if (standups.containsKey(event.channel) && standups.getValue(event.channel).isAlive)
                session.sendMessage(event.channel, "There is a standup already started!")
            else {
                val users = event.channel.members.filter { it -> !it.isBot }
                standups[event.channel] = Standup(event.sender, users, session, event.channel)
                standups.getValue(event.channel).start()
            }
        } else {
            standups.values.filter { it.isAlive }.forEach({
                it -> it.handleMessageEvent(event.sender, event.channel, event.messageContent)
            })
        }
    }
}

fun main(args: Array<String>) {
    val bot = SlackBot()

    val session = SlackSessionFactory.createWebSocketSlackSession(System.getenv("SLACK_TOKEN"))
    session.connect()

    session.addMessagePostedListener({ event, session ->
        bot.handleSlackMessage(event, session)
    })

    Runtime.getRuntime().addShutdownHook(Thread {
        session.disconnect()
    })
}
