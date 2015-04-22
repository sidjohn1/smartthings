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
    section("Select a Roomba to monitor..."){
		input "switch1", "device.ThinkingCleaner", title: "Monitored Roomba", required: true, multiple: false

    }
        section("Select events to be notified of..."){
			input "sendRoombaOn", "enum", title: "Roomba On?", options: ["Yes", "No"], required: false, defaultValue: "No"
            input "sendRoombaOff", "enum", title: "Roomba Off?", options: ["Yes", "No"], required: false, defaultValue: "No"
            input "sendRoombaError", "enum", title: "Roomba Error?", options: ["Yes", "No"], required: false, defaultValue: "No"
    }
}

def installed() {
	sendEvent(descriptionText:"Installed with settings: ${settings}", eventType:"SOLUTION_EVENT", displayed: true)
    log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	sendEvent(descriptionText:"Updated with settings: ${settings}", eventType:"SOLUTION_EVENT", displayed: true)
    log.trace "Updated with settings: ${settings}"
    unsubscribe()
    unschedule("poll")
	initialize()
}

def initialize() {
	subscribe(switch1, "switch.on", eventHandler)
	subscribe(switch1, "switch.off", eventHandler)
    subscribe(switch1, "status.error", eventHandler)
}

def eventHandler(evt) {
	unschedule("poll")
    switch (evt.value) {
    	case "error":
            sendEvent(descriptionText:"${switch1.displayName} has an error, polling every hour", eventType:"SOLUTION_EVENT", displayed: true)
        	log.trace "${switch1.displayName} has an error, polling every hour"
        	def msg = "${switch1.displayName} has an error"
			schedule("0 0/60 * * * ?", "poll")
            if (sendRoombaError == "Yes") {
			sendPush(msg)
			}
		break;
        
		case "on":
        	sendEvent(descriptionText:"${switch1.displayName} is on, polling every minute", eventType:"SOLUTION_EVENT", displayed: true)
        	log.trace "${switch1.displayName} is on, polling every minute"
            def msg = "${switch1.displayName} is on"
        	schedule("0 0/1 * * * ?", "poll")
			if (sendRoombaOn == "Yes") {
			sendPush(msg)
			}
		break;
        
		default:
        	sendEvent(descriptionText:"${switch1.displayName} is off, polling every hour", eventType:"SOLUTION_EVENT", displayed: true)
        	log.trace "${switch1.displayName} is off, polling every hour"
			def msg = "${switch1.displayName} is off"
        	schedule("0 0/60 * * * ?", "poll")
			if (sendRoombaOff == "Yes") {
				sendPush(msg)
			}
		break;
    }
}

def poll() {
	switch1.poll()
}
