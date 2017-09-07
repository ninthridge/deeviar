Deeviar is a digital video recorder that uses HDHomeRun devices as its input.  There is a docker image available with all the required dependencies that can be run on Linux computers.  Installation instructions are found here: http://deeviar.com/installation/

To build and run, the following dependencies are required:
* Java 7 or higher
* Maven
* https://github.com/ninthridge/schedulesdirectclient
* hdhomerun_config
* ffmpeg built with modules  --enable-gpl --enable-libass --enable-libfdk-aac --enable-libfreetype --enable-libmp3lame --enable-libopus --enable-libtheora --enable-libvorbis --enable-libvpx --enable-libx264 --enable-nonfree --enable-pthreads --enable-version3 --enable-libsmbclient

To build, run the command "mvn clean install"

To run, run command "java -jar target/deeviar.jar"

The web console can be accessed by visiting http://localhost:7111 in your browser
