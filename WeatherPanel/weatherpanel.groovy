/**
 *  Weather Panel
 *
 *  Copyright 2015 Sidney Johnson
 *  If you like this code, please support the developer via PayPal: https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y
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
 *	Version: 1.1 - Fixed font size not changing the font size
 *	Version: 1.2 - Decoupled weather data refresh from wallpaper refresh
 *	Version: 1.3 - Minor formating tweaks, removed all static data from json
 *
 */
definition(
    name: "Weather Panel",
    namespace: "sidjohn1",
    author: "Sidney Johnson",
    description: "Weather Panel, a SmartThings web client",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather14-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather14-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather14-icn@3x.png",
    oauth: true)

preferences {
	page(name: "selectDevices", install: true, uninstall: true) {
	    section("About") {
			paragraph "Weather Panel displays inside and outside temp and weather infomation as a web page. Also has a random customizable background serviced by Dropbox public folders."
			paragraph "${textVersion()}\n${textCopyright()}"
 	   }
		section("Select...") {
			input "insideTemp", "capability.temperatureMeasurement", title: "Inside Tempature...", multiple: false, required: true
			input "outsideWeather", "device.smartweatherStationTile", title: "Outside Weather...", multiple: false, required: true
			input "fontSize", "enum", title:"Select Font Size", required: true, multiple:false, defaultValue: "Large", metadata: [values: ['Small','Medium','Large']]
			input "fontColor", "enum", title:"Select Font Color", required: true, multiple:false, defaultValue: "White", metadata: [values: ['White','Black']]
		}
		section("Dropbox Wallpaper") {
			input "dbuid", "number", title: "Dropbox Public UID",defaultValue: "57462297", required:false
		}
        section() {
			href "viewURL", title: "View URL"
		}
	}
    page(name: "viewURL")
}

def viewURL() {
	dynamicPage(name: "viewURL", title: "${title ?: location.name} Weather Pannel URL", install:false) {
		section() {
			paragraph "Copy the URL below to any modern browser to view your ${title ?: location.name}s' Weather Panel. Add a shortcut to home screen of your mobile device to run as a native app."
			input "weatherUrl", "text", title: "URL",defaultValue: "${generateURL("html")}", required:false
			href url:"${generateURL("html")}", style:"embedded", required:false, title:"View", description:"Tap to view, then click \"Done\""
		}
	}
}


mappings {
    path("/html") {
		action: [
			GET: "generateHtml",
		]
	}
	path("/json") {
		action: [
			GET: "generateJson",
		]
	}
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "Weather Panel ${textVersion()} ${textCopyright()}"
    generateURL()
    weatherRefresh()
}

def weatherRefresh() {
	log.debug "refreshing weather"
	outsideWeather?.refresh()
    insideTemp?.poll()
}

def generateHtml() {
	render contentType: "text/html", data: "<!DOCTYPE html>\n<html>\n<head>${head()}</head>\n<body>\n${body()}\n</body></html>"
}

def generateJson() {
	render contentType: "application/json", data: "${jsonData()}"
}

def head() {

def font1 = ""
def font2 = ""
def font3 = ""
def color1 = ""
def color2 = ""
def temperatureScale = getTemperatureScale()

switch (fontSize) {
	case "Large":
	font1 = "42"
	font2 = "14"
	font3 = "7"
	break;
	case "Medium":
	font1 = "41"
	font2 = "13"
	font3 = "6"
	break;
	case "Small":
	font1 = "40"
	font2 = "12"
	font3 = "5"
	break;
}

switch (fontColor) {
	case "White":
	color1 = "white"
	color2 = "black"
	break;
	case "Black":
	color1 = "black"
	color2 = "white"
	break;
}

"""<!-- Meta Data -->
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="Description" content="Weather Panel" />
	<meta name="application-name" content="Weather Panel" />
	<meta name="apple-mobile-web-app-title" content="Weather Panel">
	<meta name="keywords" content="weather,panel,smartthings" />
	<meta name="Author" content="sidjohn1" />
<!-- Apple Web App -->
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
	<meta name="viewport" content = "width = device-width, initial-scale = 1.0, maximum-scale=1.0, user-scalable=0" />
	<link rel="apple-touch-icon-precomposed" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.png" />
<!-- Stylesheets -->
<style type="text/css">
body{
	background-size: cover;
	background-attachment: fixed;
	background-color: ${color2};
	overflow:hidden;
	margin:0 0;
	width: 100%;
	height: 100%;
}
b{
	font-size: 20px; font-size: ${font3}vmax;
    vertical-align: super;
}
div{
	font-family:Gotham, "Helvetica Neue", Helvetica, Arial, sans-serif;
	font-size: 20px;
	color: ${color1};
	text-shadow: 2px 1px 0px ${color2};
	margin:0 0;
}
#icon{
	margin-top: 6%;
    margin-left: 4%;
	font-size: 20px; font-size: ${font1}vmax;
	text-align: center;
	width: 95%;
	height: 55%;
}
#temp1{
	font-weight: bold;
	text-align: left;
	float: left;
	width: 48%;
	margin-top: -4%;
	margin-left: 2%;
    vertical-align: text-top;
	font-size: 20px; font-size: ${font2}vmax;
}
#temp2{
	font-weight: bold;
	text-align: right;
	float: right;
	width: 48%;
	margin-top: -4%;
	margin-right: 2%;
    vertical-align: text-top;
	font-size: 20px; font-size: ${font2}vmax;
}
#cond{
	white-space: nowrap;
	font-weight: bold;
	margin-top: -4%;
	text-align: right;
    vertical-align: middle;
	float: left;
	width: 100%;
	font-size: 20px; font-size: ${font3}vmax;
}
</style>
<link type="text/css" rel="stylesheet" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.css"/>
    <!-- Page Title -->
    <title>Weather Panel</title>
  	<!-- Javascript -->
<script type="text/javascript" charset="utf-8" src="https://sidjohn1.github.io/smartthings/WeatherPanel/index.js"></script>
<script type="text/javascript">
\$(window).load(function(){
	var bg = '';
	var tImage = new Image();
	\$("#data").click(function(){
		var path="https://dl.dropboxusercontent.com/u/${dbuid}/Wallpaper/";
		var fileList = "index.json";
		\$.getJSON(path+fileList,function(list,status){
			var mime = '*';
			while (mime.search('image')){
				obj = list[Math.floor(Math.random()*list.length)];
				mime=obj.mime;
			}
			bg = path+obj.path;
			bg = bg.replace('#','%23');
			document.body.background = bg;		
		});
        setTimeout('\$("#data").click()', 1800000);
	});
	\$("#data").click();
});
</script>

<script type="text/javascript">
\$(document).ready(function(){
	weatherData = function () {
    \$("#data").empty();
		\$.getJSON("${generateURL("json")}",function(weather){
		var content = '';
			\$.each(weather.data, function(i,data){
	    		content += '<div id="icon"><i class="wi wi-' + data.icon + '"></i></div>';
	    		content += '<div id="temp1">' + data.temp1 + '°<b>${temperatureScale}</b></div>';
	    		content += '<div id="temp2">' + data.temp2 + '°<b>${temperatureScale}</b></div>';
    			content += '<div id="cond">' + data.cond + '&nbsp;</div>';
    			\$(content).appendTo("#data");
    		});
    	});
    	setTimeout(weatherData, 480000);
	}
	weatherData();
});
</script>
"""
}

def body() {  
"""<div id="data"></div>"""
}

def jsonData(){
weatherRefresh()
	def weatherIcons = [
        "chanceflurries" : "day-snow",
        "chancerain" : "day-rain",
        "chancesleet" : "day-rain-mix",
        "chancesnow" : "day-snow",
        "chancetstorms" : "day-thunderstorm",
        "clear" : "day-sunny",
        "cloudy" : "day-cloudy",
        "flurries" : "day-snow",
        "fog" : "day-fog",
        "hazy" : "day-haze",
        "mostlycloudy" : "day-cloudy",
        "mostlysunny" : "day-sunny",
        "partlycloudy" : "day-cloudy",
        "partlysunny" : "day-cloudy",
        "rain" : "day-rain",
        "sleet" : "day-sleet",
        "snow" : "day-snow",
        "sunny" : "day-sunny",
        "tstorms" : "day-thunderstorm",
        "nt_chanceflurries" : "night-alt-snow",
        "nt_chancerain" : "night-alt-rain",
        "nt_chancesleet" : "night-alt-hail",
        "nt_chancesnow" : "night-alt-snow",
        "nt_chancetstorms" : "night-alt-thunderstorm",
        "nt_clear" : "night-clear",
        "nt_cloudy" : "night-alt-cloudy",
        "nt_flurries" : "night-alt-snow",
        "nt_fog" : "night-fog",
        "nt_hazy" : "dust",
        "nt_mostlycloudy" : "night-alt-cloudy",
        "nt_mostlysunny" : "night-alt-cloudy",
        "nt_partlycloudy" : "night-alt-cloudy",
        "nt_partlysunny" : "night-alt-cloudy",
        "nt_sleet" : "night-alt-rain-mix",
        "nt_rain" : "night-alt-rain",
        "nt_snow" : "night-alt-snow",
        "nt_sunny" : "night-clear",
        "nt_tstorms" : "night-alt-thunderstorm"
        ]

	def icon = weatherIcons[outsideWeather.currentValue("weatherIcon")]
"""{"data": [{"icon":"${icon}","cond":"${outsideWeather.currentValue("weather")}","temp1":"${insideTemp.currentValue("temperature")}","temp2":"${outsideWeather.currentValue("temperature")}"}]}"""
}

private def generateURL(data) {    
	if (!state.accessToken) {
		try {
			createAccessToken()
			log.debug "Creating new Access Token: $state.accessToken"
		} catch (ex) {
			log.error "Enable OAuth in SmartApp IDE settings for Weather Panel"
			log.error ex
		}
    }
	def url = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/${data}?access_token=${state.accessToken}"
	return "$url"
}

private def textVersion() {
    def text = "Version 1.3"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1"
}
