/**
 *  Thinking Cleaner
 *  Smartthings Devicetype
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
import groovy.json.JsonSlurper
preferences {
    input("ip", "text", title: "IP Address", description: "Your Thinking Cleaner Address")
}

metadata {
	definition (name: "Thinking Cleaner", namespace: "sidjohn1", author: "Sidney Johnson") {
		capability "Battery"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Tone"
        
        command "spot"
        
        attribute "percent", "number"
	}

	simulator {
		// TODO: define status and reply messages here
	}

tiles {
    valueTile("battery", "device.battery", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
    	state ("default", label:'${currentValue}% battery', unit:"percent", backgroundColors: [
                    [value: 20, color: "#bc2323"],
                    [value: 50, color: "#ffff00"],
                    [value: 96, color: "#79b821"]
                ]
            )
		}
	standardTile("beep", "device.beep", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
		state "beep", label:'beep', action:"tone.beep", icon:"st.quirky.spotter.quirky-spotter-sound-on", backgroundColor:"#ffffff"
	}
	standardTile("clean", "device.switch", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
		state("on", label: 'clean', action: "switch.on", icon: "st.Appliances.appliances13", backgroundColor: "#79b821")
    }
	standardTile("dock", "device.switch", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
		state("off", label: 'dock', action: "switch.off", icon: "st.Appliances.appliances13", backgroundColor: "#79b821")
	}
	standardTile("spot", "device.spot", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
		state("spot", label: 'spot', action: "spot", icon: "st.Appliances.appliances13", backgroundColor: "#79b821")
	}
	standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
		state("default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon")
	}
	standardTile("status", "device.status", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
    	state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
		state ("charging", label:'${currentValue}', icon: "st.Appliances.appliances13", backgroundColor: "#E5E500")
        state ("cleaning", label:'${currentValue}', icon: "st.Appliances.appliances13", backgroundColor: "#79b821")
		state ("docked", label:'${currentValue}', icon: "st.quirky.spotter.quirky-spotter-plugged")
        state ("docking", label:'${currentValue}', icon: "st.Appliances.appliances13", backgroundColor: "#E5E500")
        state ("off", label:'', icon: "st.thermostat.heating-cooling-off")
        state ("waiting", label:'${currentValue}', icon: "st.Appliances.appliances13")
	}
	main("clean")
    details(["clean","spot","dock","beep","battery","status","refresh"])
	}
}


// parse events into attributes
def parse(String description) {
	def map = stringToMap(description)
	def bodyString = new String(map.body.decodeBase64())
	def slurper = new JsonSlurper()
	def result = slurper.parseText(bodyString)
	log.debug result
	switch (result.action) {
		case "command":
        	log.debug result.action
			break;
		case "status":
        	sendEvent(name: 'battery', value: result.status.battery_charge as Integer)
//			log.debug result.status.cleaner_state
            switch (result.status.cleaner_state) {
				case "st_base":
        		sendEvent(name: 'status', value: "docked", state: "docked" as String)
                sendEvent(name: 'switch', value: "off", state: "off" as String)
				break;
				case "st_base_recon":
        		sendEvent(name: 'status', value: "charging", state: "charging" as String)
                sendEvent(name: 'switch', value: "off", state: "off" as String)
				break;
                case "st_base_full":
        		sendEvent(name: 'status', value: "charging", state: "charging" as String)
                sendEvent(name: 'switch', value: "off", state: "off" as String)
				break;
                case "st_base_trickle":
        		sendEvent(name: 'status', value: "charging", state: "charging" as String)
                sendEvent(name: 'switch', value: "off", state: "off" as String)
				break;
                case "st_base_wait":
        		sendEvent(name: 'status', value: "docked", state: "docked" as String)
                sendEvent(name: 'switch', value: "off", state: "off" as String)
				break;
                case "st_clean":
        		sendEvent(name: 'status', value: "cleaning", state: "cleaning" as String)
                sendEvent(name: 'switch', value: "on", state: "on" as String)
				break;
                case "st_cleanstop":
        		sendEvent(name: 'status', value: "waiting", state: "waiting" as String)
				break;
                case "st_clean_spot":
        		sendEvent(name: 'status', value: "cleaning", state: "cleaning" as String)
                sendEvent(name: 'switch', value: "on", state: "on" as String)
				break;
                case "st_clean_max":
        		sendEvent(name: 'status', value: "cleaning", state: "cleaning" as String)
                sendEvent(name: 'switch', value: "on", state: "on" as String)
				break;
                case "st_dock":
        		sendEvent(name: 'status', value: "docking", state: "docking" as String)
				break;
                case "st_off":
        		sendEvent(name: 'status', value: "off", state: "off" as String)
				break;
                case "st_wait":
        		sendEvent(name: 'status', value: "waiting", state: "waiting" as String)
				break;
			}
			break;
	}
}

// handle commands

def installed() {
    log.debug "Installed with settings: ${settings}"
 	def hosthex = convertIPtoHex(settings.ip)
	def porthex = convertPortToHex("80")
	device.deviceNetworkId = "$hosthex:$porthex"
	log.debug "The device id configured is: $device.deviceNetworkId"
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    def hosthex = convertIPtoHex(settings.ip)
	def porthex = convertPortToHex("80")
	device.deviceNetworkId = "$hosthex:$porthex"
	log.debug "The device id configured is: $device.deviceNetworkId"
}

def on() {
	log.debug "Executing 'on'"
    api('on')
}

def off() {
	log.debug "Executing 'off'"
    api('off')
}
def spot() {
	log.debug "Executing 'spot'"
	api('spot')
}
def poll() {
	log.debug "Executing 'poll'"
    api('refresh')
}

def refresh() {
	log.debug "Executing 'refresh'"
    api('refresh')
}

def beep() {
	log.debug "Executing 'beep'"
	api('beep')
}
def api(String rooCommand, success = {}) {
    def rooPath = ""
    switch (rooCommand) {
		case "on":
			rooPath = "/command.json?command=clean"
			log.debug "The Clean Command was sent"
			break;
		case "off":
			rooPath = "/command.json?command=dock"
			log.debug "The Dock Command was sent"
			break;
        case "spot":
			rooPath = "/command.json?command=spot"
			log.debug "The Spot Command was sent"
			break;
		case "refresh":
        	rooPath = "/status.json"
            log.debug "The Status Command was sent"
            break;
		case "beep":
        	rooPath = "/command.json?command=find_me"
            log.debug "The Beep Command was sent"
            break;
}
	def hubAction = ""
	if (rooCommand != "refresh"){
		hubAction = [new physicalgraph.device.HubAction(
		method: "GET",
		path: rooPath,
		headers: [HOST: "${ip}:80", Accept: "application/json"]
        ), delayAction(4500), api('refresh')]
    }
	else {
		hubAction = new physicalgraph.device.HubAction(
		method: "GET",
		path: rooPath,
		headers: [HOST: "${ip}:80", Accept: "application/json"]
        )
    }
	return hubAction
}

private String convertIPtoHex(ip) { 
    String hexip = ip.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hexip
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}
private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}
