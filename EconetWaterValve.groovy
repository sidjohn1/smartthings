/**
 *  Copyright 2015 Sidney Johnson
 *
 *	Smartthings Devicetype
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
 */
metadata {
	definition (name: "Econet Water Valve", namespace: "sidjohn1", author: "Sidney Johnson") {
		capability "Actuator"
        capability "Polling"
		capability "Valve"
		capability "Refresh"
		capability "Sensor"
        
		fingerprint deviceId: "0x1001", inClusters: "0x20,0x25,0x27,0x72,0x86,0x70,0x85"
		fingerprint deviceId: "0x1003", inClusters: "0x25,0x2B,0x2C,0x27,0x75,0x73,0x70,0x86,0x72"
	}

	// simulator metadata
	simulator {
		status "close":  "command: 2503, payload: 00"
		status "open": "command: 2503, payload: FF"

		// reply messages
		reply "2001FF": "command: 2503, payload: FF"
		reply "200100": "command: 2503, payload: 00"
	}

	// tile definitions
	tiles {
		standardTile("contact", "device.contact", width: 2, height: 2, canChangeIcon: true) {
			state "open", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#53a7c0", nextState:"closing"
			state "closed", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#e86d13", nextState:"opening"
			state "opening", label: '${name}', action: "valve.close", icon: "st.valves.water.open", backgroundColor: "#ffe71e"
			state "closing", label: '${name}', action: "valve.open", icon: "st.valves.water.closed", backgroundColor: "#ffe71e"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "contact"
		details(["contact","refresh"])
	}
}

def parse(String description) {
	log.trace description
	def result = null
	def cmd = zwave.parse(description)
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	def value = cmd.value ? "open" : "closed"
	[name: "contact", value: value, descriptionText: "$device.displayName valve is $value"]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[:] // Handles all Z-Wave commands we aren't interested in
}

def open() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0xFF).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 6000)
}

def close() {
	delayBetween([
		zwave.basicV1.basicSet(value: 0x00).format(),
		zwave.switchBinaryV1.switchBinaryGet().format()
	], 6000)
}

def poll() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

def refresh() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}

