#!/bin/sh


#${GCCHOME} setup as: /usr/bin/gcc
GCC=gcc
OSNAME="Darwin"
UNAME=`uname -a`

if [[ $UNAME == *$OSNAME* ]] 
then
    echo "Mac"
    BINDIR="macos"
    LIBDIR="mac"
    LIBFILE="libgui.dylib"
else
    echo "Linux"
    echo "UBUNTU lib install : sudo apt-get install openjdk-8-jdk gcc libxi-dev libxcursor-dev libxrandr-dev libgl1-mesa-dev libxinerama-dev"
    echo "CentOS lib install : yum -y install java-1.8.0-openjdk gcc mesa-libGL-devel libXi-devel libXcursor-devel libXrandr-devel libXinerama-devel"

    BINDIR="centos_x64"
    LIBDIR="centos_x64"
    LIBFILE="libgui.so"
fi

echo "compile mini_jvm"
CSRC="../minijvm/c"
#echo ${CSRC}
#
#

SRCLIST=`find ${CSRC}  -type f  -name "*.c" -not -path "${CSRC}/utils/sljit/*"  -not -path "${CSRC}/cmake-*" -not -path "${CSRC}/.*"`
#echo ${SRCLIST}
${GCC}  -o mini_jvm -I${CSRC}/jvm -I${CSRC}/utils/ -I${CSRC}/utils/sljit/ -I${CSRC}/utils/https/ -I${CSRC}/utils/https/mbedtls/include/  $SRCLIST ${CSRC}/utils/sljit/sljitLir.c   -pthread  -lpthread -lm -ldl


echo "compile glfw_gui"

CSRC="../desktop/glfw_gui/c"
SRCLIST=`find ${CSRC} -type f -name "*.c"  -not -path "${CSRC}/cmake-*" -not -path "${CSRC}/.*"`
#
if [[ $UNAME == *$OSNAME* ]] 
then
    ${GCC} -shared -fPIC -o ${LIBFILE} -I../minijvm/c/jvm -I${CSRC}/ -I${CSRC}/deps/include -L${CSRC}/deps/lib/${LIBDIR} -lpthread -lglfw3 -framework Cocoa -framework IOKit -framework OpenGL -framework CoreFoundation -framework CoreVideo $SRCLIST
else
    ${GCC} -shared -fPIC -o ${LIBFILE} -I../minijvm/c/jvm -I${CSRC}/ -I${CSRC}/deps/include -L${CSRC}/deps/lib/${LIBDIR}   $SRCLIST -pthread -lglfw3 -lX11 -lXi -lpthread -lXcursor -lXrandr -lGL -lXinerama
fi
mv mini_jvm ${LIBFILE} ${BINDIR}/

