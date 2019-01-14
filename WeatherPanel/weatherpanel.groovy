/**
 *  Weather Panel
 *
 *  Copyright 2016 Sidney Johnson
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
 *	Version: 2.0 - Addeded 3 day forcast and more formating and presentation tweaks. Removed weather station requirement
 *	Version: 2.1 - Preloads images for smoother transitions
 *	Version: 2.1.1 - Added dynamic API URL
 *	Version: 2.2 - Added support for user selectable Station ID
 *	Version: 2.2.1 - Added better browser support
 *	Version: 2.3 - Upgraded Icons
 *	Version: 2.4 - TWC Weather Update
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
    page(name: "selectDevices")
    page(name: "viewURL")
}

def selectDevices() {
	dynamicPage(name: "selectDevices", install: true, uninstall: true) {
	    section("About") {
			paragraph "Weather Panel displays inside and outside temp and weather infomation as a web page. Also has a random customizable background serviced by Dropbox public folders."
			paragraph "${textVersion()}\n${textCopyright()}"
 	   }
		section("Select...") {
			input "insideTemp", "capability.temperatureMeasurement", title: "Inside Tempature...", multiple: false, required: true
            input "outsideTemp", "capability.temperatureMeasurement", title: "Outside Tempature...", multiple: false, required: false
			input "showForcast", "bool", title:"Show Forcast", required: false, multiple:false
            input "stationID", "text", title:"Station ID (Optional)", required: false, multiple:false
		}
		section(hideable: true, hidden: true, "Optional Settings") {
        	input "fontColor", "bool", title: "Font Color Black", required: false
			input "fontSize", "enum", title:"Select Font Size", required: true, multiple:false, defaultValue: "Medium", metadata: [values: ['xSmall','Small','Medium','Large']]
			input "outsideWeather", "capability.temperatureMeasurement", title: "Clear to free weather device", multiple: true, required: false
		}
		section("Wallpaper URL") {
			input "wallpaperUrl", "text", title: "Wallpaper URL",defaultValue: "http://", required:false
		}
        section() {
			href "viewURL", title: "View URL"
		}
	}
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
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "Weather Panel ${textVersion()} ${textCopyright()}"
	generateURL()
}

def generateHtml() {
	render contentType: "text/html", headers: ["Access-Control-Allow-Origin": "*"], data: "<!DOCTYPE html>\n<html>\n<head>${head()}</head>\n<body>\n${body()}\n</body></html>"
}

def generateJson() {
	render contentType: "application/json", headers: ["Access-Control-Allow-Origin": "*"], data: "${jsonData()}"
}

def head() {

def color1
def color2
def font1
def font2
def font3
def iconW
def temp1TA
def temperatureScale = getTemperatureScale()
def weatherDataContent

switch (fontSize) {
	case "Large":
	font1 = "50"
	font2 = "20"
	font3 = "10"
	break;
	case "Medium":
	font1 = "48"
	font2 = "18"
	font3 = "10"
	break;
	case "Small":
	font1 = "46"
	font2 = "16"
	font3 = "10"
	break;
    case "xSmall":
	font1 = "44"
	font2 = "16"
	font3 = "7"
	break;
}

if (settings.fontColor) {
	color1 = "0,0,0"
	color2 = "255,255,255"
}
else {
	color1 = "255,255,255"
	color2 = "0,0,0"
}


if (showForcast == true) {
	iconW = "47"
	temp1TA = "right"
	weatherDataContent = """	    		content += '<div id="icon"><i class="wi wi-' + item.icon + '"></i></div>';
	    		content += '<div id="temp1" class="text3"><p>' + item.temp1 + '°<b>${temperatureScale}&nbsp;<br>Inside&nbsp;</b><br>' + item.temp2 + '°<b>${temperatureScale}&nbsp;<br>Outside&nbsp;</b><br></p></div>';
    			content += '<div id="cond" class="text2"><p>' + item.cond + '&nbsp;</p></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay + '<br><i class="wi wi-' + item.forecastIcon + '"></i>&nbsp;&nbsp;' + item.forecastDayHigh + '<br><u>' + item.forecastDayLow + '</u></p><br></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay1 + '<br><i class="wi wi-' + item.forecastIcon1 + '"></i>&nbsp;&nbsp;' + item.forecastDayHigh1 + '<br><u>' + item.forecastDayLow1 + '</u></p><br></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay2 + '<br><i class="wi wi-' + item.forecastIcon2 + '"></i>&nbsp;&nbsp;' + item.forecastDayHigh2 + '<br><u>' + item.forecastDayLow2 + '</u></p><br></div>';
    			content += '<div id="forecast" class="text3"><p>' + item.forecastDay3 + '<br><i class="wi wi-' + item.forecastIcon3 + '"></i>&nbsp;&nbsp;' + item.forecastDayHigh3 + '<br><u>' + item.forecastDayLow3 + '</u></p><br></div>';"""
}
   else {
	iconW = "100"
	temp1TA = "left"
   	weatherDataContent = """	    		content += '<div id="icon"><i class="wi wi-' + item.icon + '"></i></div>';
	    		content += '<div id="temp1" class="text1"><p>' + item.temp1 + '°<b>${temperatureScale}<br>Inside</b></p></div>';
	    		content += '<div id="temp2" class="text1"><p>' + item.temp2 + '°<b>${temperatureScale}<br>Outside</b></p></div>';
    			content += '<div id="cond" class="text1"><p>' + item.cond + '&nbsp;</p></div>';"""
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
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" />
	<link rel="apple-touch-icon-precomposed" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.png" />
<!-- Stylesheets -->
<style type="text/css">
body{
	background-size: cover;
	-webkit-background-size: cover;
	-moz-background-size: cover;
	-o-background-size: cover;
	background-attachment: fixed;
	background-color: rgb(${color2});
	background-position: center;
    background-repeat: no-repeat;
	overflow: hidden;
	margin: 0 0;
	width: 100%;
	height: 100%;
}
b{
	font-size: 20px;
	font-size: ${font3}vh;
	vertical-align: super;
}
p{
	font-family:Gotham, "Helvetica Neue", Helvetica, Arial, sans-serif;
	color: rgb(${color1});
	text-shadow: 2px 2px 1px rgb(${color2});
	margin:0 0;
	opacity: 0.9;
}
i{
	color: rgb(${color1});
	text-shadow: 2px 2px 1px rgb(${color2});
	vertical-align: middle;
	opacity: 0.9;
}
div{
	background: transparent;
}
u{
	text-decoration: overline;
}
.text1 {
	font-weight: bold;
	vertical-align: text-top;
	margin-top: -3%;
}
.text2 {
	font-weight: 900;
    letter-spacing: 5px;
	vertical-align: super;
	margin-top: -3%;
	margin-bottom: 1%;
}
.text3 {
	font-weight: bold;
	vertical-align: super;
}
#data {
	display: flex;
	display: -webkit-flex;
	flex-direction: row;
	-webkit-flex-direction: row;
	flex-wrap: wrap;
	-webkit-flex-wrap: wrap;
}
#icon{
	margin: 3% 0 0 1%;
	font-size: 20px;
	font-size: ${font1}vh;
	text-align: center;
	width: ${iconW}%;
}
#temp1{
	text-align: ${temp1TA};
	float: left;
	width: 48%;
	margin-left: 2%;
	font-size: 20px;
	font-size: ${font2}vh;
	line-height: 13vh;
}
#temp2{
	text-align: right;
	float: right;
	width: 48%;
	margin-right: 2%;
	font-size: 20px;
	font-size: ${font2}vh;
	line-height: 13vh;
}
#cond{
	white-space: nowrap;
	text-align: right;
	width: 100%;
	font-size: 20px;
	font-size: ${font3}vh;
}
#forecast{
	white-space: nowrap;
	text-align: right;
	width: 26%;
	font-size: 20px;
	font-size: 7vh;
	background: rgba(${color2},.5);
	vertical-align: middle;
    margin-left: -3%;
}
</style>
<link type="text/css" rel="stylesheet" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.css"/>
<link rel="shortcut icon" type="image/png" href="https://sidjohn1.github.io/smartthings/WeatherPanel/index.png"/>
<link rel="manifest" href="https://sidjohn1.github.io/smartthings/WeatherPanel/manifest.json">
    <!-- Page Title -->
    <title>Weather Panel</title>
  	<!-- Javascript -->
<script type="text/javascript" charset="utf-8" src="https://sidjohn1.github.io/smartthings/WeatherPanel/index.js"></script>
<script type="text/javascript">
\$(window).load(function(){
	var bg = '';
	var tImage = new Image();
	\$("#data").click(function(){
		var path = "${wallpaperUrl}";
		var fileList = "index.json";
		\$.getJSON(path+fileList,function(list,status){
			var mime = '*';
			while (mime.search('image')){
				obj = list[Math.floor(Math.random()*list.length)];
				mime=obj.mime;
			}
			bg = path+obj.path;
			bg = bg.replace('#','%23');
            \$('<img src="'+bg+'"/>');
            setTimeout(function(){
				document.body.background = bg;
			},3000);
		});
        setTimeout('\$("#data").click()', 1790000);
	});
	\$("#data").click();
});
</script>

<script type="text/javascript">
\$(document).ready(function(){
	weatherData = function () {
		\$.getJSON("${generateURL("json")}",function(weather){
		var content = '';
			\$.each(weather.data, function(i,item){
${weatherDataContent}
				\$("#data").empty();
    			\$(content).appendTo("#data");
    		});
    	});
    	setTimeout(weatherData, 180500);
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
//log.debug "refreshing weather"
sendEvent(linkText:app.label, name:"weatherRefresh", value:"refreshing weather", descriptionText:"weatherRefresh is refreshing weather", eventType:"SOLUTION_EVENT", displayed: true)

def alerts
def astronomy
def current
def currentTemp
def forecast
def forecastDayHigh
def forecastDayHigh1
def forecastDayHigh2
def forecastDayHigh3
def forecastDayLow
def forecastDayLow1
def forecastDayLow2
def forecastDayLow3
def temperatureScale = getTemperatureScale()

def weatherIcons = []

if (settings.stationID) {
	forecast = getTwcForecast()
	current = getTwcConditions()
}
else if (settings.zipcode) {
	forecast = getTwcForecast()
	current = getTwcConditions()
}
else {
	forecast = getTwcForecast()
	current = getTwcConditions()
}
if (temperatureScale == "F") {
	currentTemp = current.temperature ?: "??"
	forecastDayHigh = forecast.temperatureMax[0] ?: current.temperatureMaxSince7Am
	forecastDayHigh1 = forecast.temperatureMax[1] ?: "??"
	forecastDayHigh2 = forecast.temperatureMax[2] ?: "??"
	forecastDayHigh3 = forecast.temperatureMax[3] ?: "??"
	forecastDayLow = forecast.temperatureMin[0] ?: current.temperatureMin24Hour
	forecastDayLow1 = forecast.temperatureMin[1] ?: "??"
	forecastDayLow2 = forecast.temperatureMin[2] ?: "??"
	forecastDayLow3 = forecast.temperatureMin[3] ?: "??"
}
else {
	currentTemp = Math.round(current.current_observation.temp_c)
	forecastDayHigh = forecast.forecast.simpleforecast.forecastday[0].high.celsius ?: "??"
	forecastDayHigh1 = forecast.forecast.simpleforecast.forecastday[1].high.celsius ?: "??"
	forecastDayHigh2 = forecast.forecast.simpleforecast.forecastday[2].high.celsius ?: "??"
	forecastDayHigh3 = forecast.forecast.simpleforecast.forecastday[3].high.celsius ?: "??"
	forecastDayLow = forecast.forecast.simpleforecast.forecastday[0].low.celsius ?: "??"
	forecastDayLow1 = forecast.forecast.simpleforecast.forecastday[1].low.celsius ?: "??"
	forecastDayLow2 = forecast.forecast.simpleforecast.forecastday[2].low.celsius ?: "??"
	forecastDayLow3 = forecast.forecast.simpleforecast.forecastday[3].low.celsius ?: "??"
}

if (current.dayOrNight == "D"){
	weatherIcons = ["0" : "tornado", "1" : "hurricane", "2" : "hurricane", "3" : "day-thunderstorm", "4" : "day-thunderstorm", "5" : "day-rain-mix", "6" : "day-sleet", "7" : "day-sleet", "8" : "day-sleet", "9" : "day-sprinkle", "10" : "day-sleet", "11" : "day-sprinkle", "12" : "day-rain", "13" : "day-snow", "14" : "day-snow", "15" : "day-snow", "16" : "day-snow", "17" : "day-hail", "18" : "day-hail", "19" : "dust", "20" : "day-fog", "21" : "day-haze", "22" : "day-haze", "23" : "day-light-wind", "24" : "day-windy", "25" : "snowflake-cold", "26" : "day-cloudy", "27" : "day-cloudy", "28" : "day-cloudy", "29" : "night-alt-cloudy", "30" : "day-cloudy", "31" : "night-clear", "32" : "day-sunny", "33" : "night-clear", "34" : "day-sunny", "35" : "day-hail", "36" : "hot", "37" : "day-thunderstorm", "38" : "thunderstorm", "39" : "day-showers", "40" : "day-storm-showers", "41" : "day-snow", "42" : "day-snow", "43" : "day-snow", "44" : "na", "45" : "night-showers", "46" : "night-snow", "47" : "day-thunderstorm"]//}
}
else if (current.dayOrNight == "N"){
	weatherIcons = ["0" : "tornado", "1" : "hurricane", "2" : "hurricane", "3" : "night-alt-thunderstorm", "4" : "night-alt-thunderstorm", "5" : "night-alt-rain-mix", "6" : "night-alt-sleet", "7" : "night-alt-sleet", "8" : "night-alt-sleet", "9" : "night-alt-sprinkle", "10" : "night-alt-sleet", "11" : "night-alt-sprinkle", "12" : "night-alt-rain", "13" : "night-alt-snow", "14" : "night-alt-snow", "15" : "night-alt-snow", "16" : "night-alt-snow", "17" : "night-alt-hail", "18" : "night-alt-hail", "19" : "dust", "20" : "night-fog", "21" : "night-fog", "22" : "night-fog", "23" : "night-alt-cloudy-windy", "24" : "night-alt-cloudy-gusts", "25" : "snowflake-cold", "26" : "night-alt-cloudy", "27" : "night-alt-cloudy", "28" : "night-alt-cloudy", "29" : "night-alt-cloudy", "30" : "night-alt-cloudy", "31" : "night-clear", "32" : "night-clear", "33" : "night-clear", "34" : "night-clear", "35" : "night-alt-hail", "36" : "hot", "37" : "night-alt-thunderstorm", "38" : "thunderstorm", "39" : "night-alt-showers", "40" : "night-alt-storm-showers", "41" : "night-alt-snow", "42" : "night-alt-snow", "43" : "night-alt-snow", "44" : "na", "45" : "night-showers", "46" : "night-snow", "47" : "night-alt-thunderstorm"]
}
else {
	weatherIcons = ["0" : "tornado", "1" : "hurricane", "2" : "hurricane", "3" : "thunderstorm", "4" : "thunderstorm", "5" : "rain-mix", "6" : "sleet", "7" : "sleet", "8" : "sleet", "9" : "sprinkle", "10" : "sleet", "11" : "sprinkle", "12" : "rain", "13" : "snow", "14" : "snow", "15" : "snow", "16" : "snow", "17" : "hail", "18" : "hail", "19" : "dust", "20" : "fog", "21" : "smoke", "22" : "smoke", "23" : "windy", "24" : "strong-wind", "25" : "snowflake-cold", "26" : "cloudy", "27" : "cloudy", "28" : "day-cloudy", "29" : "night-alt-cloudy", "30" : "day-cloudy", "31" : "night-clear", "32" : "day-sunny", "33" : "night-clear", "34" : "day-sunny", "35" : "day-hail", "36" : "hot", "37" : "day-thunderstorm", "38" : "thunderstorm", "39" : "day-showers", "40" : "storm-showers", "41" : "day-snow", "42" : "snow", "43" : "snow", "44" : "na", "45" : "night-showers", "46" : "night-snow", "47" : "thunderstorm"]
}

def forecastNow = weatherIcons[current.iconCode.toString()]
def forecastDayIcon = weatherIcons[forecast.daypart[0].iconCode[1].toString()]
def forecastDay1Icon = weatherIcons[forecast.daypart[0].iconCode[3].toString()]
def forecastDay2Icon = weatherIcons[forecast.daypart[0].iconCode[4].toString()]
def forecastDay3Icon = weatherIcons[forecast.daypart[0].iconCode[7].toString()]

def dop = new java.text.SimpleDateFormat("E' - 'dd")
def dip = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
def forcastDate = dip.parse(forecast.validTimeLocal[0])
def forcastDate1 = dip.parse(forecast.validTimeLocal[1])
def forcastDate2 = dip.parse(forecast.validTimeLocal[2])
def forcastDate3 = dip.parse(forecast.validTimeLocal[3])

"""{"data": [{"icon":"${forecastNow}","cond":"${current.wxPhraseLong}","temp1":"${Math.round(insideTemp.currentValue("temperature"))}","temp2":"${Math.round(outsideTemp.currentValue("temperature"))}"
,"forecastDay":"${dop.format(forcastDate)}","forecastIcon":"${forecastDayIcon}","forecastDayHigh":"${forecastDayHigh}","forecastDayLow":"${forecastDayLow}"
,"forecastDay1":"${dop.format(forcastDate1)}","forecastIcon1":"${forecastDay1Icon}","forecastDayHigh1":"${forecastDayHigh1}","forecastDayLow1":"${forecastDayLow1}"
,"forecastDay2":"${dop.format(forcastDate2)}","forecastIcon2":"${forecastDay2Icon}","forecastDayHigh2":"${forecastDayHigh2}","forecastDayLow2":"${forecastDayLow2}"
,"forecastDay3":"${dop.format(forcastDate3)}","forecastIcon3":"${forecastDay3Icon}","forecastDayHigh3":"${forecastDayHigh3}","forecastDayLow3":"${forecastDayLow3}"}]}"""
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
	def url = "${getApiServerUrl()}/api/smartapps/installations/${app.id}/${data}?access_token=${state.accessToken}"
return "$url"
}

private def textVersion() {
    def text = "Version 2.4"
}

private def textCopyright() {
    def text = "Copyright © 2016 Sidjohn1"
}
