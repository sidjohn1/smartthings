/**
 *  Thinking Cleanerer
 *
 *  Copyright 2014 Sidney Johnson
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
 */
definition(
    name: "Thinking Cleanerer",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Handles polling and job notification for Thinking Cleaner",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Monitor This Roomba..."){
		input "switch1", "capability.switch", multiple: false

    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    unsubscribe()
    unschedule("poll")
	initialize()
}

def initialize() {
	subscribe(switch1, "switch.on", eventHandler)
	subscribe(switch1, "switch.off", eventHandler)
}

def eventHandler(evt) {
	if (evt.value == "on") {
    	unschedule("poll")
        schedule("0 0/1 * * * ?", "poll")
	}
	else {
		unschedule("poll")
        schedule("0 0/30 * * * ?", "poll")
	}
}

def poll() {
	switch1.poll()
}
