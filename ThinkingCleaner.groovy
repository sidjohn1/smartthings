/**
 *  Thinking Cleaner
 *
 *	Smartthings Devicetype
 *
 *  Copyright 2014 Sidney Johnson
 *  If you like this device, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
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
 *	Version: 1.1 - Fixed installed and updated functions
 *	Version: 1.2 - Added error tracking, and better icons, link state
 *	Version: 1.3 - Better error tracking, error correction and the ability to change the default port (thx to sidhartha100), fix a bug that prevented auto population of deviceNetworkId
 *
 */
import groovy.json.JsonSlurper

metadata {
	definition (name: "Thinking Cleaner", namespace: "sidjohn1", author: "Sidney Johnson") {
		capability "Battery"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
		capability "Tone"
        
        command "spot"

        attribute "network","string"
	}
    
	preferences {
		input("ip", "text", title: "IP Address", description: "Your Thinking Cleaner Address", required: true, displayDuringSetup: true)
		input("port", "number", title: "Port Number", description: "Your Thinking Cleaner Port Number", defaultValue: "80", required: true, displayDuringSetup: true)
	}

	tiles {
		valueTile("battery", "device.battery", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label:'${currentValue}% battery', unit:"percent", backgroundColors: [
				[value: 20, color: "#bc2323"],
				[value: 50, color: "#ffff00"],
				[value: 96, color: "#79b821"]
			])
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
			state ("docked", label:'${currentValue}', icon: "st.quirky.spotter.quirky-spotter-plugged", backgroundColor: "#79b821")
			state ("docking", label:'${currentValue}', icon: "st.Appliances.appliances13", backgroundColor: "#E5E500")
			state ("error", label:'${currentValue}', icon: "st.Appliances.appliances13", backgroundColor: "#bc2323")
			state ("waiting", label:'${currentValue}', icon: "st.Appliances.appliances13")
		}
		standardTile("network", "device.network", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
			state ("default", label:'unknown', icon: "st.unknown.unknown.unknown")
			state ("Connected", label:'Link Good', icon: "st.Appliances.appliances13", backgroundColor: "#79b821")
			state ("Not Connected", label:'Link Bad', icon: "st.Appliances.appliances13", backgroundColor: "#bc2323")
		}
		main("clean")
			details(["clean","spot","dock","battery","status","network","beep","refresh"])
		}
}

// parse events into attributes
def parse(String description) {
	def map = stringToMap(description)
	def headerString = new String(map.headers.decodeBase64())
//	log.debug headerString
	if (headerString.contains("200 OK")) {
		def bodyString = new String(map.body.decodeBase64())
//		log.debug bodyString
		def slurper = new JsonSlurper()
		def result = slurper.parseText(bodyString)
//		log.debug result.action
		switch (result.action) {
			case "command":
			sendEvent(name: 'network', value: "Connected" as String)
			break;
			case "status":
            sendEvent(name: 'network', value: "Connected" as String)
			sendEvent(name: 'battery', value: result.status.battery_charge as Integer)
            switch (result.status.cleaner_state) {
				case "st_base":
				sendEvent(name: 'status', value: "docked" as String)
				sendEvent(name: 'switch', value: "off" as String)
				break;
				case "st_base_recon":
				sendEvent(name: 'status', value: "charging" as String)
				sendEvent(name: 'switch', value: "off" as String)
				break;
				case "st_base_full":
				sendEvent(name: 'status', value: "charging" as String)
				sendEvent(name: 'switch', value: "off" as String)
				break;
				case "st_base_trickle":
				sendEvent(name: 'status', value: "docked" as String)
				sendEvent(name: 'switch', value: "off" as String)
				break;
				case "st_base_wait":
				sendEvent(name: 'status', value: "docked" as String)
				sendEvent(name: 'switch', value: "off" as String)
				break;
				case "st_clean":
					if (result.status.cleaning == "1"){
						sendEvent(name: 'status', value: "cleaning" as String)
						sendEvent(name: 'switch', value: "on" as String)
					}
					else {
						sendEvent(name: 'status', value: "error" as String)
						sendEvent(name: 'switch', value: "on" as String)
						log.debug result.status.cleaner_state
					}
				break;
                case "st_clean_spot":
                	if (result.status.cleaning == "1"){
                    sendEvent(name: 'status', value: "cleaning" as String)
                	sendEvent(name: 'switch', value: "on" as String)
					}
                	else {
                    sendEvent(name: 'status', value: "error" as String)
                	sendEvent(name: 'switch', value: "on" as String)
					log.debug result.status.cleaner_state
                    }
				break;
                case "st_clean_max":
                	if (result.status.cleaning == "1"){
                    sendEvent(name: 'status', value: "cleaning" as String)
                	sendEvent(name: 'switch', value: "on" as String)
					}
                	else {
                    sendEvent(name: 'status', value: "error" as String)
                	sendEvent(name: 'switch', value: "on" as String)
					log.debug result.status.cleaner_state
                    }
				break;
                case "st_dock":
					if (result.status.cleaning == "1"){
						sendEvent(name: 'status', value: "docking" as String)
						sendEvent(name: 'switch', value: "on" as String)
					}
					else {
						sendEvent(name: 'status', value: "error" as String)
						log.debug result.status.cleaner_state
					}
				break;
                case "st_off":
                sendEvent(name: 'switch', value: "off" as String)
                sendEvent(name: 'status', value: "error" as String)
				break;
                default:
				sendEvent(name: 'status', value: "error" as String)
				break;
			}
			break;
		}
	}
	else {
		sendEvent(name: 'status', value: "error" as String)
		log.debug headerString
	}
}

// handle commands

def installed() {
	log.debug "Installed with settings: ${settings}"
	ipSetup()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    ipSetup()
}
 
def on() {
	log.debug "Executing 'on'"
    ipSetup()
    api('on')
}

def off() {
	log.debug "Executing 'off'"
    api('off')
}

def spot() {
	log.debug "Executing 'spot'"
    ipSetup()
	api('spot')
}

def poll() {
	log.debug "Executing 'poll'"
    api('refresh')
}

def refresh() {
	log.debug "Executing 'refresh'"
    ipSetup()
    api('refresh')
}

def beep() {
	log.debug "Executing 'beep'"
    ipSetup()
	api('beep')
}

def api(String rooCommand, success = {}) {
	def rooPath = ""
	def hubAction = ""
    if (device.currentValue('network') == "unknown"){
    	sendEvent(name: 'network', value: "Not Connected" as String)
        log.debug "Network is not connected"
    }
    else {
		sendEvent(name: 'network', value: "unknown" as String, displayed:false)
    }
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
    
	switch (rooCommand) {
		case "refresh":
        case "beep":
    		try {
				hubAction = new physicalgraph.device.HubAction(
				method: "GET",
				path: rooPath,
				headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"]
        		)
			}
			catch (Exception e) {
				log.debug "Hit Exception $e on $hubAction"
			}
			break;
		default:
			try {
				hubAction = [new physicalgraph.device.HubAction(
				method: "GET",
				path: rooPath,
				headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"]
        		), delayAction(9900), api('refresh')]
        	}
			catch (Exception e) {
				log.debug "Hit Exception $e on $hubAction"
			}
			break;
	}
	return hubAction
}

def ipSetup() {
	def hosthex
    def porthex
	if (settings.ip) {
		hosthex = convertIPtoHex(settings.ip)
	}
    if (settings.port) {
		porthex = convertPortToHex(settings.port)
    }
    if (settings.ip && settings.port) {
		device.deviceNetworkId = "$hosthex:$porthex"
//		log.debug "The device id configured is: ${device.deviceNetworkId}"
	}
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
