# Description

Deeviar's front end software runs as a Roku channel. Deeviar's back end software runs on a computer within your home network that runs a Linux operating system.  To watch live tv, you will need an HDHomeRun device and it is highly recommended to set up a SchedulesDirect subscription.  Deeviar uses SchedulesDirect information to populate the interactive guide.

To build and run, the following dependencies are required:
* Java 7 or higher
* Maven
* https://github.com/ninthridge/schedulesdirectclient
* hdhomerun_config
* ffmpeg built with modules  --enable-gpl --enable-libass --enable-libfdk-aac --enable-libfreetype --enable-libmp3lame --enable-libopus --enable-libtheora --enable-libvorbis --enable-libvpx --enable-libx264 --enable-nonfree --enable-pthreads --enable-version3 --enable-libsmbclient

# Build
mvn clean install

# Run
java -jar target/deeviar.jar

# Running in Docker
docker build -t ninthridge/deeviar:latest .
sudo mkdir /opt/deeviar
docker run -d -v /opt/deeviar:/opt/deeviar -v /etc/localtime:/etc/localtime:ro -v /etc/timezone:/etc/timezone:ro --net=host --restart always ninthridge/deeviar:latest

(Please note that deeviar is only able to communicate with hdhomerun devices in docker the host system is Linux)

# Configuration
Visit http://{ip address}:7111
Click configuration in the upper right hand corner.  The default username is "admin".  The default password is "password".
Click on "Devices & Schedules".
If no devices are shown, then Deeviar was unable to discover an HDHomeRun device on your local network.
When Deeviar detects a new HDHomeRun device, it will initiate a device scan to determine which channels are available.  This scan can take up to 15 minutes.  Please wait until the status changes to "Available" before proceeding.
Once the devices are "Available", click on its "Select Lineup" button.
If needed, visit http://schedulesdirect.org to create a SchedulesDirect account and subscription.  (A SchedulesDirect account is required if you are planning to use the interactive programming guide.)
Enter your SchedulesDirect username and password and enter your zip code.  Click "Retrieve Lineups".  Select the lineup that corresponds with your HDHomeRun device and click "Save".

# Install Deeviar's Roku channel
Visit https://my.roku.com/account/add?channel=5L9HCR
Log into your Roku account when prompted.
Click on the "Yes, Add Channel" button.
From your Roku device's home screen, go to "Settings" -> "System" -> "System update" -> "Check now".
The first time that the channel launches, it will prompt you for the ip address of the computer where you installed the Deeviar back end software.



