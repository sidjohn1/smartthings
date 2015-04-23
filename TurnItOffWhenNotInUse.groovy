/**
 *  Turn It Off When Not in Use
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
 *	Version: 1.0 - Initial Version
 *	Version: 1.1 - Added the ability to turn off a differnt switch than the one being monitored
 *
 */
definition(
    name: "Turn It Off When Not in Use",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Turns off device when wattage drops below a set level after a set time. Retires every 10min",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select switch to monitor power...") {
	input name: "switch1", type: "capability.powerMeter", multiple: false
	}
    section("Select switch to turn off...") {
	input name: "switch2", type: "capability.switch", multiple: false
	}
    section("Turn them off at...") {
	input name: "stopTime", type: "time", multiple: false
	}
    section("When wattage drops below...") {
	input name: "wattageLow", type: "number", multiple: false
	}    
	section("Turn them on at...") {
	input name: "startTime", type: "time", multiple: false
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(stopTime, "stopTimerCallback")

}

def updated(settings) {
	unschedule()
	schedule(stopTime, "stopTimerCallback")
}

def startTimerCallback() {
	log.debug "Turning on switches"
	switch2.on()

}

def stopTimerCallback() {
    if (switch1.currentPower <= wattageLow) {
    log.debug "Turning off switches. Current Wattage: ${switch1.currentPower}"
	switch2.off()
    schedule(startTime, "startTimerCallback")
	}
    else {
	log.debug "Waiting for next poll cycle. Current Wattage: ${switch1.currentPower}"
    def timeDelay = 60 * 10
	runIn(timeDelay, stopTimerCallback, [overwrite: true])
    }
}
