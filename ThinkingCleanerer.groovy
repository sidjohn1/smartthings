/**
 *  Thinking Cleanerer
 *  Smartthings SmartApp
 *  Copyright 2014 Sidney Johnson
 *  If you like this app, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Version: 1.0 - Initial Version
 *	Version: 1.2 - Added error push notifcation, and better icons
 *	Version: 1.3 - New interface, better polling, and logging. Added sms notifcations
 *	Version: 1.4 - Added bin full notifcations
 *	Version: 1.4.1 - Fixed SMS send issue
 */
 
definition(
 	name: "Thinking Cleanerer",
	namespace: "sidjohn1",
	author: "Sidney Johnson",
	description: "Handles polling and job notification for Thinking Cleaner",
	category: "Convenience",
	iconUrl: "http://cdn.device-icons.smartthings.com/Appliances/appliances13-icn.png",
	iconX2Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances13-icn@2x.png",
	iconX3Url: "http://cdn.device-icons.smartthings.com/Appliances/appliances13-icn@3x.png")

preferences {
	page name:"pageInfo"
}
def pageInfo() {
	return dynamicPage(name: "pageInfo", title: "Thinking Cleanerer", install: true, uninstall: true) {
	section("About") {
		paragraph "Thinking Cleaner(Roomba) smartapp for Smartthings. This app monitors you roomba and provides job notifacation"
		paragraph "${textVersion()}\n${textCopyright()}"    
	}
		def roombaList = ""
			settings.switch1.each() {
			try {
				roombaList += "$it.displayName is $it.currentStatus. Battery is $it.currentBattery%\n"
			}
            catch (e) {
                log.trace "Error checking status."
                log.trace e
            }
        }
		if (roombaList) {
			section("Roomba Status:") {
				paragraph roombaList.trim()
			}
		}
		section("Select Roomba(s) to monitor..."){
			input "switch1", "device.ThinkingCleaner", title: "Monitored Roomba", required: true, multiple: true, submitOnChange: true
		}
		section(hideable: true, hidden: true, "Event Notifications..."){
			input "sendPush", "bool", title: "Send as Push?", required: false, defaultValue: true
			input "sendSMS", "phone", title: "Send as SMS?", required: false
			input "sendRoombaOn", "bool", title: "Notify when on?", required: false, defaultValue: false
			input "sendRoombaOff", "bool", title: "Notify when off?", required: false, defaultValue: false
			input "sendRoombaError", "bool", title: "Notify on error?", required: false, defaultValue: true
			input "sendRoombaBin", "bool", title: "Notify on full bin?", required: false, defaultValue: true
		}
	}
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "Thinking Cleanerer ${textVersion()} ${textCopyright()}"
	subscribe(switch1, "switch.on", eventHandler)
	subscribe(switch1, "switch.off", eventHandler)
	subscribe(switch1, "status.error", eventHandler)
	subscribe(switch1, "bin.full", eventHandler)
}

def eventHandler(evt) {
	def msg
	switch (evt.value) {
		case "error":
			sendEvent(linkText:app.label, name:"${evt.displayName}", value:"error",descriptionText:"${evt.displayName} has an error", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${evt.displayName} has an error"
			msg = "${evt.displayName} has an error"
			if (sendRoombaError == true) {
				if (settings.sendSMS != null) {
					sendSms(sendSMS, msg) 
				}
				if (settings.sendPush == true) {
					sendPush(msg)
				}
			}
			schedule("39 0/15 * 1/1 * ?", pollErr)
		break;
		case "on":
			sendEvent(linkText:app.label, name:"${evt.displayName}", value:"on",descriptionText:"${evt.displayName} is on", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${evt.displayName} is on"
			msg = "${evt.displayName} is on"
        	schedule("15 0/1 * 1/1 * ?", pollOn)
			if (sendRoombaOn == true) {
				if (settings.sendSMS != null) {
					sendSms(sendSMS, msg) 
				}
				if (settings.sendPush == true) {
					sendPush(msg)
				}
			}
		break;
		case "full":
			sendEvent(linkText:app.label, name:"${evt.displayName}", value:"bin full",descriptionText:"${evt.displayName} bin is full", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${evt.displayName} bin is full"
			msg = "${evt.displayName} bin is full"
			if (sendRoombaBin == true) {
				if (settings.sendSMS != null) {
					sendSms(sendSMS, msg) 
				}
				if (settings.sendPush == true) {
					sendPush(msg)
				}
			}
		break;
        
		case "off":
			sendEvent(linkText:app.label, name:"${evt.displayName}", value:"off",descriptionText:"${evt.displayName} is off", eventType:"SOLUTION_EVENT", displayed: true)
			log.trace "${evt.displayName} is off"
			msg = "${evt.displayName} is off"
			if (sendRoombaOff == true) {
				if (settings.sendSMS != null) {
					sendSms(sendSMS, msg) 
				}
				if (settings.sendPush == true) {
					sendPush(msg)
				}
			}
			schedule("22 4 0/1 1/1 * ? *", pollOff)
		break;
	}
}

def pollOn() {
	def onSwitch1 = switch1.currentSwitch.findAll { switchVal ->
		switchVal == "on" ? true : false
	}
	settings.switch1.each() {
		if (it.currentSwitch == "on") {
			it.poll()
		}
	}
	if (onSwitch1.size() == 0) {
		unschedule(pollOn)
	}
}

def pollOff() {
	def offSwitch1 = switch1.currentSwitch.findAll { switchVal ->
		switchVal == "off" ? true : false
	}
	settings.switch1.each() {
		if (it.currentSwitch == "off") {
			it.poll()
		}
	}
	if (offSwitch1.size() == 0) {
		unschedule(pollOff)
	}
}

def pollErr() {
	def errSwitch1 = switch1.currentStatus.findAll { switchVal ->
		switchVal == "error" ? true : false
	}
	settings.switch1.each() {
		if (it.currentStatus == "error") {
			it.poll()
		}
	}
	if (errSwitch1.size() == 0) {
		unschedule(pollErr)
	}
}
private def textVersion() {
    def text = "Version 1.4.1"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1"
}
