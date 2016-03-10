#!/bin/bash
cd
if [ ! -d ~/netide/openflowplugin ]; then

   
   cd netide
   wget https://nexus.opendaylight.org/content/groups/public/org/opendaylight/integration/distribution-karaf/0.4.0-Beryllium/distribution-karaf-0.4.0-Beryllium.tar.gz
   tar -xzf distribution-karaf-0.4.0-Beryllium.tar.gz

   cd distribution-karaf-0.4.0-Beryllium
   
   sed -i 's/44444/44445/g' etc/org.apache.karaf.management.cfg
   sed -i 's/1099/1098/g' etc/org.apache.karaf.management.cfg
   
   cd bin

   chmod +x ./client ./start ./stop
   echo "Installing karaf dependencies for ODL shim"
   ./start
   
   while [ $(./client test 2>&1 | grep "Failed to get the session." | wc -l) -eq 1 ]; do
	echo "No Connection to Karaf server. Trying again..."
	sleep 1
   done
   

   ./client "feature:install odl-netide-rest"
   ./client "feature:install odl-restconf-all"
   sleep 10
   ./stop

   cd 
   
else

   echo "ODL seems to be already installed. Skipping..."
  
fi
