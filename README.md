SmartThings
=====================

A collection of code for smartthings
If you find this code usefull, please support the developer via PayPal:<br/> [![PayPal](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif) sidjohn1@gmail.com](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=XKDRYZ3RUNR9Y)

<h2>Details:</h2>
<b>TimelyPresence:</b> Smartthings smartapp that displays the current local time and the presence of the members of the house as the background. Images change from greyscale to color indicating presence.<br>
<b>WeatherPanel ([Appshot](http://sidjohn1.github.io/smartthings/weatherpannel.jpg)):</b> Smartthings smartapp that displays inside and outside temp and weather infomation as a web page. Also has a random customizable background serviced by Dropbox public folders.<br>
<b>Lights On, On Vibration.groovy:</b> Smartthings smartapp that turns on and back off a light when a sensor is vibrated. This app works well with a Smartsence Multi attached to your doorbell to detect when it has rung. The vibration of the ring will trigger the selected light to turn on.<br>
<b>PlantLink-DirectMonitor.groovy</b> Monitors your Plantlinks via [Kristopher Kubicki's plantlink-direct devicetype](https://github.com/KristopherKubicki/device-plantlink-direct) , and sends notifacations when your plants need water.<br>
<b>SmartTurnItOn.groovy:</b> Turns on a device at a set time on set day(s) of the week on if a persons presense is detected. Also turns the device off after a set time.<br>
<b>ThermostatAutoAway.groovy:</b> Smartthings smartapp that sets a thermostat to "Away" when presence(s) are no longer detected.<br>
<b>ThermostatAutoHome.groovy:</b> Smartthings smartapp that sets a thermostat to "Home" when presence(s) are detected.<br>
<b>ThinkingCleaner.groovy:</b> Thinking Cleaner(Roomba) devicetype for Smartthings, Allows Smartthings to scheulde and control your roomba.<br>
<b>ThinkingCleanerer.groovy:</b> Thinking Cleaner(Roomba) smartapp for Smartthings, Monitors your roomba and reports any errors.<br>
<b>TurnItOffWhenNotInUse.groovy:</b> Smartthings smartapp that monitors a Smart Power Outlet(powermeter) to turn off a device at a set time if the power usage is below a set point. Can also turn it back on at a set time.<br>

<h2>FAQ:</h2>
<b>How do i add a smartapp to Smartthings?</b>
<ol type="1">
  <li>Goto "https://graph.api.smartthings.com/ide/app/create" in your browser</li>
  <li>If the app requires OAuth Click "Enable OAuth in Smart App"</li>
  <li>Click "From Code"</li>
  <li>Copy all text from .groovy file and paste into text box (RAW makes this easier)</li>
  <li>Click "Create"</li>
  <li>Click "Save"</li>
  <li>Click "Publish > For Me"</li>
</ol>
<img src='http://sidjohn1.github.io/smartthings/smartapp.gif'><br>
<b>How do i run SmartSetup to install a smartapp after it has been added to Smartthings and make it run?</b>
<ol type="1">
  <li>Open the Smartthings app on your mobile device (smart phone, table, chrome extention)</li>
  <li>Click "+" at the very botton of the list</li>
  <li>At the top where you see "Things" and "Alerts" scroll right till you reach "My Apps"</li>
  <li>Scroll down till you find the app you want to install and touch it</li>
  <li>Enter the requested data and touch "Done"</li>
</ol>


