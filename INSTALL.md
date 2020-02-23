# Quick install of the InfComTec Kubernetes tools.

## Maven

Clone the project and run "mvn install" in the top directory.

## GitHub Maven packages

Follow the instructions under the "Packages" tab. This assumes you are set up
to use GitHub as a Maven repository.

## Manually (binaries only)

You will need the Jar file with all the dependencies bundled.
This file can be downloaded from [GitHub][https://github.com/Walter-Stroebel/JK8sCtl]
by following the "package(s)" link, clicking on the version you want and then
downloading the file from the "Assets".

You want the JK8sCtl-X.Y-jar-with-dependencies.jar file where X.Y is the version.

For the bundled scripts to work, place the jar file (and probably the scripts)
in /usr/local/bin. You will need "sudo" to do that.

Else check the scripts and roll your own, if you're here you should have the
skills to do that :)

## Scripts to start the tools

- menu.sh should start the main Menu to all of the GUI-based tools.
- (WIP) alerts,sh should start the alerting tool.
- (WIP) analyze.sh should start an analysis of your Kubernetes cluster, reporting
on various (possible) issues.

  