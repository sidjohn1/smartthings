/**
 *  URI Dimmer
 *
 *	Smartthings Devicetype
 *
 *  Copyright 2015 Sidney Johnson
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
 *	Version: 1.1 - Added device health check
 *	Version: 1.2 - Added support for samsung connect
 *	Version: 1.3 - Added Dimmer Multiplyer
 *
 */
preferences {
	input("deviceIp", "text", title: "IP Address", description: "Device IP Address", required: true, displayDuringSetup: true)
	input("devicePort", "number", title: "Port Number", description: "Device Port Number (Default:80)", defaultValue: "80", required: true, displayDuringSetup: true)
	input("deviceOnPath", "text", title: "On Path (/blah?q=this)", required: false)
	input("deviceOffPath", "text", title: "Off Path (/blah?q=this)", required: false)
	input("deviceDimPath", "text", title: "Dim Path (/blah?q=this)", required: false)
	input("deviceStatusPath", "text", title: "Status Path (/blah?q=this)", required: false)
    input("deviceMuliplier", "decimal", title: "Dimming Multiplier", description: "Dimming Multiplier (Default:1, Fully:2.55)", defaultValue: "1", required: true, displayDuringSetup: true)
}

metadata {
	definition (name: "URI Dimmer Switch", namespace: "sidjohn1", author: "sidjohn1", ocfDeviceType: "oic.d.switch", vid: "generic-dimmer") {
	capability "Actuator"
	capability "Switch"
	capability "Switch Level"
    capability "Sensor"
	capability "Refresh"
	capability "Health Check"
	}

	// UI tile definitions
    
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"off"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"on"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}
		standardTile("offButton", "device.button", width: 1, height: 1, canChangeIcon: true) {
			state "default", label: 'Force Off', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}
		standardTile("onButton", "device.switch", width: 1, height: 1, canChangeIcon: true) {
			state "default", label: 'Force On', action: "switch.on", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","onButton","offButton","refresh"])
	}
}

def parse(String description) {
	def map
	def headerString
    
    map = stringToMap(description)
	headerString = new String(map.headers.decodeBase64())
	if (headerString.contains("200 OK")) {
		sendEvent(name: "${state.saveEvent[0]}", value: "${state.saveEvent[1]}", displayed: true)
		sendEvent(name: "status", value: "online", displayed: true, isStateChange: true)
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "online", displayed: false, isStateChange: true)
		log.debug "${state.saveEvent[0]} ${state.saveEvent[1]}"
		state.statusCount = 0
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
	log.info "URI Dimmer ${textVersion()} ${textCopyright()}"
	ipSetup()
	state.statusCount = 0
    sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\", \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)
	sendEvent(name: "checkInterval", value: 14 * 60, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID], displayed: false)
    autoPoll()
}

def autoPoll() {
	unschedule()
	def sec = Math.round(Math.floor(Math.random() * 60))
	def cron = "$sec 0/8 * * * ?" // every 8 min
	schedule(cron, ping)
	runEvery15Minutes(ping)
}

def on() {
	sendCommand('on')
}

def off() {
	sendCommand('off')
}

def setLevel(value) {
	sendCommand("$value")
}

def ping() {
	sendCommand('refresh')
}

def refresh() {
	sendCommand('refresh')
}

def sendCommand(command) {
	if (isLocal(settings.deviceIp)==false){
		def cmd = "${settings.external_off_uri}";
		log.debug "Sending request cmd[${cmd}]"
		httpGet(cmd) {resp ->
			if (resp.data) {
				log.info "${resp.data}"
			} 
		}
	}
	if (isLocal(settings.deviceIp)==true){
	def hubAction
	def sendPath
	switch (command) {
		case "on":
		sendPath = "${deviceOnPath}"
		state.saveEvent = ["switch","on"]
		log.debug "Executing On" 
		break;
		
		case "off":
		sendPath = "${deviceOffPath}"
		state.saveEvent = ["switch","off"]
		log.debug "Executing Off" 
		break;
		
		case "refresh":
        	sendPath = "${deviceStatusPath}"
		break;
		
		default:
		sendPath = "${deviceDimPath}${command.toInteger()*2.55.toInteger()}"
		state.saveEvent = ["level","${command}"]
		break;
	}
	state.statusCount = state.statusCount++
	if (state.statusCount >= 3) {
		sendEvent(name: "status", value: "offline", displayed: true, isStateChange: true)
        sendEvent(name: "DeviceWatch-DeviceStatus", value: "offline", displayed: false, isStateChange: true)
	}
	try {
		hubAction = [new physicalgraph.device.HubAction(
		method: "GET",
		path: sendPath,
		headers: [HOST: "${settings.deviceIp}:${settings.devicePort}"]
		)]
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
//	log.debug "$hubAction"
	return hubAction
	}
}

private def isLocal(ip) {
	String hubNetwork = "${location.hubs.localIP}".tokenize( '.' )[0,1].join().replace("[","")
	String deviceNetwork = ip.tokenize( '.' )[0,1].join().replace("[","")
	if (hubNetwork == deviceNetwork){
		return true
	}
	else{
		return false
	}
}

def ipSetup() {
	def hosthex
	def porthex
	if (settings.deviceIp) {
		hosthex = convertIPtoHex(settings.deviceIp)
	}
	if (settings.devicePort) {
		porthex = convertPortToHex(settings.devicePort)
	}
	if (settings.deviceIp && settings.devicePort) {
		device.deviceNetworkId = "$hosthex:$porthex"
		log.info "Setting up Network ID $hosthex:$porthex"
	}
	else {
		log.debug "Error Setting up Network ID"
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
private String textVersion() {
	def text = "Version 1.3"
}

private String textCopyright() {
	def text = "Copyright © 2019 Sidjohn1"
}
