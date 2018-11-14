package com.github.alpert

import com.ullink.slack.simpleslackapi.SlackChannel
import com.ullink.slack.simpleslackapi.SlackSession
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StandupBotTest {
    private val bot : SlackBot

    init {
        bot = SlackBot()
    }

    @Test
    fun `new standup started`() {
        val session : SlackSession = mockk()
        val channel : SlackChannel = mockk()

        val event : SlackMessagePosted = mockk()
        event.channel = channel
        event.messageContent = "start standup"

        every { channel.isDirect } returns false

        Assertions.assertThat(bot.standups.size).isEqualTo(0)
        bot.handleSlackMessage(event, session)
        Assertions.assertThat(bot.standups.size).isEqualTo(1)

    }
}