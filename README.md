Marley
===

Marley is a simple Slack bot written with Kotlin.

### What does it do?
* It listens "start standup" command from channels its added
* If there is no other standup started from that channel, it starts one and ask everyone in that channel about their status
* When everyone responds, it notifies the initiator and publishes summary on the channel
* If someone does not respond, it sends a reminder every 15 minutes
* If someone does not respond more than an hour, it sets the persons status as "Not Available"


### Deploy
1. Create a Slack bot and add it to your workspace
2. Deploy to Heroku using the button below  
[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/alpert/marley)
3. Set Slack token
4. On your app management page, configure dyno and set on

### TODO
* Unit tests (!)
* Implement slash command

## Licence
MIT