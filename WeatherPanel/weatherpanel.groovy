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
			input "weatherUrl", "text", title: "URL",defaultValue: "${generateURL()}", required:false
			href url:"${generateURL()}", style:"embedded", required:false, title:"View", description:"Tap to view, then click \"Done\""
		}
	}
}


mappings {
    path("/ui") {
		action: [
			GET: "generateHtml",
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
    runEvery15Minutes(weatherRefresh)
}

def weatherRefresh() {
	log.debug "refreshing weather"
	outsideWeather?.refresh()
    insideTemp?.poll()
}

def generateHtml() {
	render contentType: "text/html", data: "<!DOCTYPE html>\n<html>\n<head>${head()}</head>\n<body>\n${body()}\n</body></html>"
}

def head() {

def font1 = ""
def font2 = ""
def font3 = ""
def color1 = ""
def color2 = ""

switch (fontSize) {
	case "Large":
	font1 = "42"
	font2 = "14"
	font3 = "7"
	break;
	case "Medium":
	font1 = "42"
	font2 = "14"
	font3 = "7"
	break;
	case "Small":
	font1 = "42"
	font2 = "14"
	font3 = "7"
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

"""
    <!-- Meta Data -->
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="Description" content="Weather Panel" />
	<meta name="application-name" content="Weather Panel" />
	<meta name="apple-mobile-web-app-title" content="Weather Panel">
    <meta name="keywords" content="weather,panel,smartthings" />
    <meta name="Author" content="sidjohn1" />
    <meta http-equiv="refresh" content="900" />
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
div{
	font-family:Gotham, "Helvetica Neue", Helvetica, Arial, sans-serif;
	font-size: 20px;
	color: ${color1};
	text-shadow: 2px 1px 0px ${color2};
	margin:0 0;
}
marquee{
margin-top: 5%;
}
#icon{
margin-top: 6%;
font-size: 20px; font-size: ${font1}vmax;
text-align: center;
width: 100%;
height: 55%;
}
#temp1 {
font-weight: bold;
text-align: left;
float: left;
width: 48%;
margin-top: -5%;
margin-left: 2%;
font-size: 20px; font-size: ${font2}vmax;
}
#temp2 {
font-weight: bold;
text-align: right;
float: right;
width: 48%;
margin-top: -5%;
margin-right: 2%;
font-size: 20px; font-size: ${font2}vmax;
}
#cond {
white-space: nowrap;
font-weight: bold;
margin-top: -3%;
text-align: left;
float: left;
width: 96%;
margin-left: 2%;
font-size: 20px; font-size: ${font3}vmax;
}
</style>
<link type="text/css" rel="stylesheet" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.css"/>
    <!-- Page Title -->
    <title>Weather Panel</title>
  	<!-- Javascript -->
<script type="text/javascript" src="https://sidjohn1.github.io/smartthings/WeatherPanel/index.js"></script>
<script type="text/javascript">
\$(window).load(function(){
	var bg = '';
	var tImage = new Image();
	\$("#icon").click(function(){
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
	});
	\$("#icon").click();
});
var orz=function(){alert('orz');};
</script>
<script type="text/javascript">
	setTimeout(function(){window.location.href=window.location.href},900000);
</script>
"""
}

def body() {  
	def weatherIcons = [
        "chanceflurries" :		"wi-day-snow",
        "chancerain" :			"wi-day-rain",
        "chancesleet" :			"wi-day-rain-mix",
        "chancesnow" :			"wi-day-snow",
        "chancetstorms" :		"wi-day-thunderstorm",
        "clear" :				"wi-day-sunny",
        "cloudy" :				"wi-day-cloudy",
        "flurries" :			"wi-day-snow",
        "fog" :					"wi-day-fog",
        "hazy" :				"wi-day-haze",
        "mostlycloudy" :		"wi-day-cloudy",
        "mostlysunny" :			"wi-day-sunny",
        "partlycloudy" :		"wi-day-cloudy",
        "partlysunny" :			"wi-day-cloudy",
        "rain" :				"wi-day-rain",
        "sleet" :				"wi-day-sleet",
        "snow" :				"wi-day-snow",
        "sunny" :				"wi-day-sunny",
        "tstorms" :				"wi-day-thunderstorm",
        "nt_chanceflurries" :	"wi-night-alt-snow",
        "nt_chancerain" :		"wi-night-alt-rain",
        "nt_chancesleet" :		"wi-night-alt-hail",
        "nt_chancesnow" :		"wi-night-alt-snow",
        "nt_chancetstorms" :	"wi-night-alt-thunderstorm",
        "nt_clear" :			"wi-night-clear",
        "nt_cloudy" :			"wi-night-alt-cloudy",
        "nt_flurries" :			"wi-night-alt-snow",
        "nt_fog" :				"wi-night-fog",
        "nt_hazy" :				"wi-dust",
        "nt_mostlycloudy" :		"wi-night-alt-cloudy",
        "nt_mostlysunny" :		"wi-night-alt-cloudy",
        "nt_partlycloudy" :		"wi-night-alt-cloudy",
        "nt_partlysunny" :		"wi-night-alt-cloudy",
        "nt_sleet" :			"wi-night-alt-rain-mix",
        "nt_rain" :				"wi-night-alt-rain",
        "nt_snow" :				"wi-night-alt-snow",
        "nt_sunny" :			"wi-night-clear",
        "nt_tstorms" :			"wi-night-alt-thunderstorm"
        ]
	def icon = weatherIcons[outsideWeather.currentValue("weatherIcon")]
	def temperatureScale = getTemperatureScale()
"""
	<div id="icon"><i class="wi ${icon}"></i></div>
	<div id="temp1">${outsideWeather.currentValue("temperature")}°${temperatureScale}</div>
	<div id="temp2">${insideTemp.currentValue("temperature")}°${temperatureScale}</div>
	<div id="cond">${outsideWeather.currentValue("weather")}</div>
"""
}
def generateURL() {
	log.debug "resetOauth: $settings.resetOauth, $resetOauth, $settings.resetOauth"
    
	if (!state.accessToken) {
		try {
			createAccessToken()
			log.debug "Creating new Access Token: $state.accessToken"
		} catch (ex) {
			log.error "Did you forget to enable OAuth in SmartApp IDE settings for SmartTiles?"
			log.error ex
		}
    }
	def url = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/ui?access_token=${state.accessToken}"
	log.debug "${title ?: location.name}s' Weather Panel URL: $url"
	return "$url"
}

private def textVersion() {
    def text = "Version 1.0"
}

private def textCopyright() {
    def text = "Copyright © 2015 Sidjohn1"
}
