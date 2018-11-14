package com.github.alpert

import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.SlackUser
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class Standup(private val initiator: SlackUser,
              private val users: Collection<SlackUser>,
              private val session: SlackSession,
              private val channel: SlackChannel) : Thread() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val responses = concurrentMapOf<SlackUser, String>()
    private val notificationTime = concurrentMapOf<SlackUser, Long>()
    private val privateChannels = concurrentMapOf<SlackUser, SlackChannel>()

    fun handleMessageEvent(sender : SlackUser,
                           channel: SlackChannel,
                           message : String) {
        if (privateChannels[sender]?.id == channel.id) {
            if (responses.putIfAbsent(sender, message) == null) {
                notificationTime.remove(sender)

                logger.info("Reply from user ${sender.realName}: $message")
            }
        }
    }

    override fun run() {
        val startTime = System.currentTimeMillis()

        logger.info("Sending messages to all users!")
        users.forEach({ it ->
            notificationTime[it] = startTime

            privateChannels[it] = session.openDirectMessageChannel(it).reply.slackChannel
            session.sendMessage(privateChannels.getValue(it), "Hi! New standup has started. What is your status?")
            logger.info("Asking status from ${it.realName}!")
        })

        while (true) {
            notificationTime.forEach({
                val now = System.currentTimeMillis()
                if (it.value - startTime >= TimeUnit.HOURS.toMillis(1)) {
                    responses[it.key] = "Not available."
                    notificationTime.remove(it.key)

                    logger.info("Set ${it.key.realName}'s status as not available")
                } else if (now - it.value >= TimeUnit.MINUTES.toMillis(15)) {
                    session.sendMessage(privateChannels.getValue(it.key), "You did not respond in 15 minutes. Please write your status.")
                    notificationTime[it.key] = now

                    logger.info("${it.key.realName} has been warned")
                }
            })

            if (users.size == responses.size) {
                break
            }

            Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        }

        session.sendMessage(privateChannels.getValue(initiator), "All status information gathered")

        session.sendMessage(channel, "Everyone responded. Here are the responses:")
        responses.forEach({
            session.sendMessage(channel, ">>> ${it.key.realName}: \n${it.value}")
        })
    }

    private fun <K, V> concurrentMapOf(): ConcurrentHashMap<K, V> = ConcurrentHashMap()
}