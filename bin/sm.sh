#!/bin/sh
set -e

JAVA_VERSION=${JAVA_VERSION-8}
JAVA_OPTS="-server -XX:+TieredCompilation -Djava.net.useSystemProxies=true ${JAVA_OPTS}"
version=2.30
artifact=shavenmaven
group=com/googlecode/${artifact}
repo=repo.bodar.com
dir=lib/
jar=${dir}${artifact}.jar
pack=${dir}${artifact}.pack.gz
url=http://${repo}/${group}/${artifact}/${version}/${artifact}-${version}
remote_file=${url}.pack.gz
remote_sh=${url}.sh

type -t setjava > /dev/null && setjava -q ${JAVA_VERSION} || if [ -n "${JAVA_HOME}" ]; then PATH=${JAVA_HOME}/bin:${PATH}; fi

if [ "$1" = "update" ]; then
	rm -f ${jar} ${pack}
fi

if [ ! -f ${jar} ]; then
	mkdir -p ${dir}
	wget -O ${pack} ${remote_file} || curl -o ${pack} ${remote_file}
	unpack200 ${pack} ${jar}
	rm -f ${pack}
	#wget -O $0 ${remote_sh} || curl -o $0 ${remote_sh}
fi
exec java ${JAVA_OPTS} -jar ${jar} $*