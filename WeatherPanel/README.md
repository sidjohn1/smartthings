<h1>Weather Panel</h1>
Weather Panel displays inside and outside temp and weather infomation as a web page. Also has a random customizable background serviced by Dropbox public folders.<br>Once installed use the link to run it as a webapp on Andriod, IOS, or any desktop browser. On Andriod devices i highly recomend using "Full Screen Browser" from the Play Store to get rid of the navigation and status bars. (https://play.google.com/store/apps/details?id=tk.klurige.fullscreenbrowser)<br> Weather data is only pulled from the Smartthings default WeatherStation Tile. This can be added from:

(https://graph.api.smartthings.com) > My Devices > + New Device >
<ol>
  <li>Give it a name, "Weather Station" will work fine</li>
  <li>For "Device Network Id" give it a unique value, I used NA03</li>
  <li>For the Type select WeatherStation Tile</li>
  <li>Select values for both location and hub</li>
</ol>
Select Create and your done creating a weather station!
<h2>Instalation</h2>
<ol>
  <li>Copy the code from weatherpanel.groovy into a new smart app</li>
  <li>Install the Weather Panel smartapp in the smartthings app </li>
  <li>Select Inside/Outside Temp Devices</li>
  <li>Select View URL and copy the URL into a broswer</li>
  <li>IOS: Install webapp from browser<br>Android: Run URL in Full Screen Browser</li>
</ol>
<h2>How to use your own Dropbox Account</h2>
<ol>
  <li>Login into your dropbox account and ensure you have a Public Folder</li>
  <li>Enable your Public Folder with this link</li>
  (https://www.dropbox.com/enable_public_folder)
  <li>Create a Wallpaper folder inside your Public Folder</li>
  <li>Upload some pictures to your Public > Wallpaper folder</li>
  <li>Select a picture and click "Copy public link"</li>
  (http://sidjohn1.github.io/smartthings/WeatherPanel/dropbox1.png)
  <li>Copy the number after "/u/" and before "/Wallpaper/". This is your Dropbox Public UID</li>
  (http://sidjohn1.github.io/smartthings/WeatherPanel/dropbox2.png)
  <li>Create a file called index.json in your Public > Wallpaper folder</li>
  Mac Users can use (create-index.json.sh) to automate the process<br>
  Windows Users can use (http://sidjohn1.github.io/smartthings/WeatherPanel/index.json) or (http://sidjohn1.github.io/smartthings/WeatherPanel/index2.json) as an example. Replace 00-05.jpg with the actual filename and add addition entries as needed. Just be sure there is no "," after the last "}".
</ol>


