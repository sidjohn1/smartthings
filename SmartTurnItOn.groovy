/**
 *  Smart turn it on
 *  Smartthings Smartapp
 *  Author: sidjohn1@gmail.com
 *  Date: 2013-10-21
 */

// Automatically generated. Make future change here.
definition(
    name: "Smart turn it on",
    namespace: "sidjohn1",
    author: "sidjohn1@gmail.com",
    description: "Turns on selected device(s) at a set time on selected days of the week only if a selected person is present and turns off selected device(s) after a set time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("Turn on which device?"){
		input "switchOne", "capability.switch",title:"Select Light", required: true, multiple: true
	}
    section("For Whom?") {
		input "presenceOne", "capability.presenceSensor", title: "Select Person", required: true, multiple: true
	}
    section("On which Days?") {
		input "dayOne", "enum", title:"Select Days", required: true, multiple:true, metadata: [values: ['Mon','Tue','Wed','Thu','Fri','Sat','Sun']]
	}
	section("At what time?") {
		input "timeOne", "time", title: "Select Time", required: true, multiple: false
	}
	section("For how long?") {
		input "timeTwo", "number", title: "Number of minutes", required: true, multiple: false
	}
}

def installed() {
	initialize()	
}

def updated() {
	unschedule()
    initialize()
}

def initialize() {
	log.debug "scheduling Smart turn it on to run at $timeOne"
	schedule(timeOne, "turnOnOne")
}

def turnOnOne(){
log.debug "Smart Turn it ON"
	def dayCheck = dayOne.contains(new Date().format("EEE"))
	if(dayCheck){
        def presenceTwo = presenceOne.latestValue("presence").contains("present")
		if (presenceTwo) {
        	switchOne.on()
			def delay = timeTwo * 60
			runIn(delay, "turnOffOne")
		}   
    }
}
  
def turnOffOne() {
	log.debug "Smart Turn it OFF"
	switchOne.off()
} 
