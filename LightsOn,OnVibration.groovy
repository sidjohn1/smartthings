/**
 *  Lights On,On Vibration
 *
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
 *
 */
definition(
    name: "Lights On, On Vibration",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Turns on a light when a sensor is vibrated",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home30-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png")

preferences {
	section("About") {
		paragraph "Lights On, On Vibration, Turns on and back off a light when a sensor is vibrated. This app works well with a Smartsence Multi attached to your doorbell to detect when it has rung. The vibration of the ring will trigger the selected light to turn on."
		paragraph "${textVersion()}\n${textCopyright()}"
 	   }
	section ("When this sensor vibrates...") {
		input "contact1", "capability.accelerationSensor", title: "Where?", required: true, multiple: false
	}
	section ("Turn on a light...") {
		input "switch1", "capability.switch", required: true, multiple: false
	}
	section("For how long?") {
		input "time1", "number", title: "Number of minutes", required: true, multiple: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(contact1, "acceleration.active", initialize)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    subscribe(contact1, "acceleration.active", initialize)
}

def initialize(evt) {
	log.debug "Turning switch ON"
	switch1.on()
    def delay = time1 * 60
	runIn(delay, "turnOff")
}
def turnOff() {
	log.debug "Turning switch OFF"
	switch1.off()
}
private def textVersion() {
    def text = "Version 1.0"
}

private def textCopyright() {
    def text = "Copyright © 2014 Sidjohn1"
}
