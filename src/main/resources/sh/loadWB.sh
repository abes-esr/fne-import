#!/bin/bash
#
# Batch pour intéragir avec un WikiBase, comme : http://fagonie-dev.v102.abes.fr:8181/w/api.php
#
# Pour le lancer : nohup ./loadWB.sh &


if [[ $(ps -edf|grep -c "lib/\* fr.fne.") = 0 ]];then
/usr/java/jdk11/bin/java -Dfile.encoding=UTF-8 -Djava.security.egd=file:///dev/urandom -cp /home/batch/autorites/experimentationFNE/batch-0.0.1-SNAPSHOT.jar:/home/batch/autorites/experimentationFNE/lib/* fr.fne.batch.BatchApplication "load" > /home/batch/autorites/experimentationFNE/cronLoadWB 2> /home/batch/autorites/experimentationFNE/cronLoadWBErrors
fi