# nifi-registry-upgrade
Upgrade all instances of your process group to latest version from your registry
This small groovy script can be used to upgrade your process groups to latest or a specific version if they are under version control in nifi-registry.

nifi-registry flow name is used to identify wich nifi process groups that is targetet for upgrade.

You can also read the article with includes good documentation and snapshots.

NiFi has a nice registry function to manage versioning of process groups it is called nifi-registry https://nifi.apache.org/registry.html

In this article I will show how you can maintain versions with nifi-registry and how you can upgrade or downgrade all your instances of your process groups.

I will not cover how you get started with nifi-registry as it is already well documented in this video Getting Started With Apache NiFi Registry.

I will instead show you my nifi-registry and my nifi canvas to describe how you can work with multiple instances of a process group and version control it and put it into production which means upgrading all instances to the version you like to use.

The code is written in groovy and requires following two jar file that you can collect from maven repository httpclient-4.5.5.jar & httpcore-4.4.10.jar.

Read the full article and documentation here http://max.bback.se/index.php/2018/07/06/nifi-version-control-deployment-automation/

