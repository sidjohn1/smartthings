/**
 *  Pollster - The SmartThings Polling Daemon.
 *
 *  Many SmartThings devices rely on polling to update their status
 *  periodically. SmartThings' built-in polling engine has a fixed polling
 *  interval of approximately 10 minutes, which may not be fast enough for
 *  some devices. Pollster, on the other hand, can poll devices as fast as
 *  every minute. Devices can be arranged into four groups with configurable
 *  polling intervals.
 *
 *  Version 1.3.1 (7/29/2015) updated by sidjohn1 to resolve SSDS
 *
 *  The latest version of this file can be found at:
 *  https://github.com/statusbits/smartthings/blob/master/Pollster/Pollster.groovy
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2014 Statusbits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License. You may obtain a
 *  copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License  for the specific language governing permissions and limitations
 *  under the License.
 */

definition(
    name: "Pollster",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Poll or refresh device status periodically.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("About") {
        paragraph "Pollster works behind the scenes and periodically calls " +
        "poll() or refresh() command for selected devices. Devices can be " +
        "arranged into four polling groups with configurable polling " +
        "intervals down to 1 minute."
        paragraph "${textVersion()}\n${textCopyright()}"
    }

    for (int n = 1; n <= 4; n++) {
        section("Polling Group ${n}") {
            input "group_${n}", "capability.polling", title:"Select devices to be polled", multiple:true, required:false
            input "refresh_${n}", "capability.refresh", title:"Select devices to be refreshed", multiple:true, required:false
            input "interval_${n}", "number", title:"Set polling interval (in minutes)", defaultValue:5
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def onAppTouch(event) {
    TRACE("onAppTouch(${event.value})")

    def devPoll = []
    def devRefresh = []

    for (int n = 1; n <= 4; n++) {
        if (settings["group_${n}"]) {
            devPoll.addAll(settings["group_${n}"])
        }

        if (settings["refresh_${n}"]) {
            devRefresh.addAll(settings["refresh_${n}"])
        }
    }

    defPoll*.poll()
    defRefresh*.refresh()
}

def pollingTask1(Integer min) {
    TRACE("pollingTask1()")
    log.debug "${min}"
	
    if (settings.group_1) {
        settings.group_1*.poll()
    }

    if (settings.refresh_1) {
        settings.refresh_1*.refresh()
    }
    def minutes = settings.interval_1.toInteger()
    
}

def pollingTask2() {
    TRACE("pollingTask2()")

    if (settings.group_2) {
        settings.group_2*.poll()
    }

    if (settings.refresh_2) {
        settings.refresh_2*.refresh()
    }
}

def pollingTask3() {
    TRACE("pollingTask3()")

    if (settings.group_3) {
        settings.group_3*.poll()
    }

    if (settings.refresh_3) {
        settings.refresh_3*.refresh()
    }
}

def pollingTask4() {
    TRACE("pollingTask4()")

    if (settings.group_4) {
        settings.group_4*.poll()
    }

    if (settings.refresh_4) {
        settings.refresh_4*.refresh()
    }
}
 def initialize() {
    log.info "Pollster. ${textVersion()}. ${textCopyright()}"
    TRACE("initialize() with settings: ${settings}")

    for (int n = 1; n <= 4; n++) {
        def min1 = settings."interval_${n}".toInteger()
        def size1 = settings["group_${n}"]?.size() ?: 0
        def size2 = settings["refresh_${n}"]?.size() ?: 0

        if (min1 > 0 && (size1 + size2) > 0) {
            TRACE("Scheduling polling task ${n} to run every ${min1} minutes.")
            def sched = "13 0/${min1} * * * ?"
            switch (n) {
            case 1:
                schedule(sched, "pollingTask1")
                break;
            case 2:
                schedule(sched, "pollingTask2")
                break;
            case 3:
                schedule(sched, "pollingTask3")
                break;
            case 4:
                schedule(sched, "pollingTask4")
                break;
            }
        }
    }

    subscribe(app, onAppTouch)
    schedule("32 3 0/4 1/1 * ?", "updated")
}

private def textVersion() {
    def text = "Version 1.3 (6/10/2015)"
}

private def textCopyright() {
    def text = "Copyright © 2014 Statusbits.com"
}

private def TRACE(message) {
    //log.trace message
}
