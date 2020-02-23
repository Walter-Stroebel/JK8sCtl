#!/bin/bash
# compiled from source, assuming current directory is project directory?
if [ -f target/JK8sCtl-$version-jar-with-dependencies.jar ]; then
	java -cp target/JK8sCtl-$version-jar-with-dependencies.jar $main
# installed using Maven?
elif [ -f ~/.m2/repository/nl/infcomtec/JK8sCtl/1.3/JK8sCtl-$version-jar-with-dependencies.jar ]; then
	java -cp ~/.m2/repository/nl/infcomtec/JK8sCtl/1.3/JK8sCtl-$version-jar-with-dependencies.jar $main
# installed in /usr/local?
elif [ -f /usr/local/bin/JK8sCtl-$version-jar-with-dependencies.jar ]; then
	java -cp /usr/local/bin/JK8sCtl-$version-jar-with-dependencies.jar $main
else
	echo InfComTec Kubernetes tools not installed or not found. Please follow INSTALL.md
fi
