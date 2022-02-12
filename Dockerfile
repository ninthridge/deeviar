FROM ubuntu
RUN apt-get update && apt-get -y upgrade && apt-get -y install autoconf automake build-essential hdhomerun-config libass-dev libfdk-aac-dev \
 libfreetype6-dev libmp3lame-dev libopus-dev libsdl2-dev libtheora-dev libtool libva-dev libvdpau-dev libvorbis-dev libvpx-dev libx264-dev \
 libx265-dev libxcb1-dev libxcb-shm0-dev libsmbclient-dev libxcb-xfixes0-dev openjdk-8-jre pkg-config texinfo wget xauth yasm zlib1g-dev

#RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
#RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections
#RUN apt-get -y install --no-install-recommends software-properties-common
#RUN add-apt-repository ppa:webupd8team/java
#RUN apt-get update
#RUN apt-get -y install --no-install-recommends oracle-java8-installer

RUN mkdir -p /tmp/ffmpeg_sources
WORKDIR /tmp/ffmpeg_sources
RUN wget http://ffmpeg.org/releases/ffmpeg-snapshot.tar.bz2
RUN tar xjvf ffmpeg-snapshot.tar.bz2
WORKDIR /tmp/ffmpeg_sources/ffmpeg
RUN ./configure \
 --pkg-config-flags="--static" \
 --bindir="/usr/local/bin" \
 --enable-gpl \
 --enable-libass \
 --enable-libfdk-aac \
 --enable-libfreetype \
 --enable-libmp3lame \
 --enable-libopus \
 --enable-libtheora \
 --enable-libvorbis \
 --enable-libvpx \
 --enable-libx264 \
 --enable-nonfree \
 --enable-pthreads \
 --enable-version3 \
 --enable-libsmbclient
RUN PATH="$HOME/bin:$PATH" make
RUN make install
WORKDIR /
RUN rm -rf /tmp/ffmpeg_sources

COPY target/deeviar.jar /opt/deeviar.jar
COPY biftool /usr/local/bin/
ENTRYPOINT ["java","-Xms256m","-Xmx1024m","-jar","/opt/deeviar.jar","-dir","/opt/deeviar"]
