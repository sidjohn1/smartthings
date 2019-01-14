/**
 *  Forecast High/Low Trigger
 *  Smartthings SmartApp
 *
 *  Copyright 2015-2017 Sidney Johnson
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
 *  Version: 1.1 - Update by David Smith - github: gunkl - Added ability to control a thermostat as well as switch.
 *  Version: 1.11 - Update by David Smith - github: gunkl - Change scheduled updates to be every other hour at 5mins after the hour.  
 *                - New setting to NOT set thermostat if it is currently OFF. Avoids collisions with other apps, like those that turn it off when the door closes.
 *
 */
definition(
    name: "Forecast High/Low Trigger",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Triggers a switch or thermostat mode based off the forecasted highs or lows for the day.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather11-icn@3x.png")


preferences {
	page name:"pageInfo"
}

def pageInfo() {
	return dynamicPage(name: "pageInfo", install: true, uninstall: true) {
		dailyForecast()
		section("About") {
			paragraph "Forecast High/Low Trigger smartapp for Smartthings. This app triggers a switch or thermostat mode based off the forecasted highs or lows for the day"
			paragraph "${textVersion()}\n${textCopyright()}"    
		}
		section("Current Forecast - High: ${state.forecastDayHigh}°${getTemperatureScale()} Low:${state.forecastDayLow}°${getTemperatureScale()}\nIf todays...") {
        	if (highLow != true) {
				input name: "highLow", type: "bool", title: "High", required: false, multiple:false, defaultValue: false, submitOnChange: true
			}
        	else {
				input name: "highLow", type: "bool", title: "Low", required: false, multiple:false, defaultValue: false, submitOnChange: true
			}
		}
		section("is") {
			if (aboveBelow != true) {
				input name: "aboveBelow", type: "bool", title: "Above or equal to", required: false, multiple:false, defaultValue: false, submitOnChange: true
			}
			else {
				input name: "aboveBelow", type: "bool", title: "Below or equal to", required: false, multiple:false, defaultValue: false, submitOnChange: true
			}
			input name: "setTemp", type: "number", title: "this temperature... (°${getTemperatureScale()})", required: true, multiple: false, defaultValue: empty
		}
		section("Then set a switch...") {
			input name: "switch1", type: "capability.switch", title: "Switch", required: false, multiple: true, defaultValue: empty
			if (switch1OnOff != true) {
				input name: "switch1OnOff", type: "bool", title: "On", required: false, multiple:false, defaultValue: false, submitOnChange: true
			}
			else {
				input name: "switch1OnOff", type: "bool", title: "Off", required: true, multiple:false, defaultValue: false, submitOnChange: true
			}
			input name: "switch1Schedule", type: "time", title: "at this time", required: false, multiple: false, defaultValue: empty
			input name: "switch1Timmer", type: "number", title: "for this lengh of time (minutes)", required: false, multiple: false, defaultValue: empty
		}
		section("Then set the thermostat mode...") {
            input name: "thermostat", type: "capability.thermostat", title: "thermostat", required: false, multiple: true, defaultValue: empty
            input name: "thermostatmode", type: "enum", title: "Mode?", metadata: [values:["auto", "heat", "cool", "off"]]
    		if (dontsettstatalways != true) {
				input name: "dontsettstatalways", type: "bool", title: "Set thermostat always", required: false, multiple:false, defaultValue: false, submitOnChange: true
			}
			else {
				input name: "dontsettstatalways", type: "bool", title: "Do not set thermostat if mode is OFF", required: false, multiple:false, defaultValue: false, submitOnChange: true
			}
		}
		section([title:"Options", mobileOnly:true]) {
			label(title:"Assign a name", required: false, defaultValue: "Forecast High/Low Trigger")
            mode(title: "Set for specific mode(s)")
		}
	} 
}

def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.info "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	state.thermostatMode = thermostat.currentValue("thermostatMode")	
    log.info "Forecast Trigger ${textVersion()} ${textCopyright()}"
    log.info "Thermostat is currently set to: ${state.thermostatMode}"
	dailyForecast
    // update forecast every hour at 5 minutes after the hour.
	schedule("0 5 0/2 * * ?", dailyForecast)
    if (settings.switch1Schedule) {
		schedule(settings.switch1Schedule, scheduledRun)
    }
}

def dailyForecast() {
	def forecast
	def forecastDayHigh
	def forecastDayLow
	def temperatureScale = getTemperatureScale()
    
	if (settings.zipcode) {
		forecast = getWeatherFeature("forecast", settings.zipcode)
	}
	else {
		forecast = getWeatherFeature("forecast")
	}
	if (temperatureScale == "F") {
		state.forecastDayHigh = forecast.forecast.simpleforecast.forecastday[0].high.fahrenheit.toInteger()
		state.forecastDayLow = forecast.forecast.simpleforecast.forecastday[0].low.fahrenheit.toInteger()
	}
	else {
		state.forecastDayHigh = forecast.forecast.simpleforecast.forecastday[0].high.celsius.toInteger()
		state.forecastDayLow = forecast.forecast.simpleforecast.forecastday[0].low.celsius.toInteger()
	}
	state.update = new Date().format("yyyy-MM-dd HH:mm", location.timeZone)
	if (settings.switch1Schedule == null) {
		scheduledRun()
	}
}

def scheduledRun() {
	def delay
	def forecastDay
    state.thermostatMode = thermostat.currentValue("thermostatMode")
    
	if (settings.switch1Timmer != null || 0) {
		delay = switch1Timmer * 60
	}

	if (settings.highLow == false) {
		forecastDay = state.forecastDayHigh.toInteger()
	}
	else {
		forecastDay = state.forecastDayLow.toInteger()
	}
    
    if (settings.thermostat) {
        log.debug "Dont set always: $dontsettstatalways - Thermostat mode: $state.thermostatMode"
    	if ((dontsettstatalways == true) && (state.thermostatMode != ["off"])) {
        	// if thermostat is off and setting is true then dont set thermostat mode
		    setThermostatMode()
        }
        if (dontsettstatalways == false) {
        	// if setting is false, then always set thermostat mode
        	setThermostatMode()
        }
    }

	if (settings.switch1) {
	    if ((settings.aboveBelow == false) && (settings.switch1OnOff == false)) {
			if (forecastDay >= settings.setTemp) {
        		switch1.on()
				if (settings.switch1Timmer != null || 0) {
					runIn(delay, "turnOff")
				}
			}
		}
		if ((settings.aboveBelow == false) && (settings.switch1OnOff == true)) {
			if (forecastDay >= settings.setTemp) {
	   	     	switch1.off()
				if (settings.switch1Timmer != null || 0) {
					runIn(delay, "turnOn")
				}
			}
		}
		if ((settings.aboveBelow == true) && (settings.switch1OnOff == false)) {
			if (forecastDay <= settings.setTemp) {
       	 		switch1.on()
				if (settings.switch1Timmer != null || 0) {
					runIn(delay, "turnOff")
				}
			}
		}
		if ((settings.aboveBelow == true) && (settings.switch1OnOff == true)) {
			if (forecastDay <= settings.setTemp) {
   		     	switch1.off()
				if (settings.switch1Timmer != null || 0) {
					runIn(delay, "turnOn")
				}
			}
		}
    }
}

def turnOff() {
	switch1.off()
}

def turnOn() {
	switch1.on()
}

private def textVersion() {
    def text = "Version 1.11"
}

private def textCopyright() {
    def text = "Copyright © 2015-2017 Sidjohn1"
}

private def setThermostatMode() {
        // aboveBelow false means temp above or equal
        if ((settings.aboveBelow == false) && (forecastDay >= settings.setTemp)) {
        	thermostat."${settings.thermostatmode}"()
        }
        // aboveBelow true means temp below or equal
        if ((settings.aboveBelow == true) && (forecastDay <= settings.setTemp)) {
        	thermostat."${settings.thermostatmode}"()
        }
}
