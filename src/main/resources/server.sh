#!/bin/bash

#######################
# @autor Andrea Mayer #
#######################

##### Configurazione ######

ARCHIVE_NAME="proxy2tor-1.0.0.jar"
APPLICATION_PROPERTIES_FILENAME="application.properties"

### Fine configurazione ###
###########################
###########################
##### NON MODIFICARE ######
###########################
###########################

JAVA_CMD="java"
APPLICATION_PROPERTIES_BOOT_LOAD="-DpropertySource=file:${APPLICATION_PROPERTIES_FILENAME}"

####################
# Avvio del server #
####################

echo ""
echo ""
echo "  _____                     ___ _______                _____                          "
echo " |  __ \                   |__ \__   __|              / ____|                         "
echo " | |__) | __ _____  ___   _   ) | | | ___  _ __ _____| (___   ___ _ ____   _____ _ __ "
echo " |  ___/ '__/ _ \ \/ / | | | / /  | |/ _ \| '__|______\___ \ / _ \ '__\ \ / / _ \ '__|"
echo " | |   | | | (_) >  <| |_| |/ /_  | | (_) | |         ____) |  __/ |   \ V /  __/ |   "
echo " |_|   |_|  \___/_/\_\\\__, |____| |_|\___/|_|        |_____/ \___|_|    \_/ \___|_|  "
echo "                       __/ |                                                          "
echo "                      |___/                                                           "
echo ""
echo ""

# Qualora non fosse stato chiuso correttamente il ProxyTor tramite ^C, viene forzata
# la terminazione di qualunque processo che abbia nel suo comando
# "java qualcosa.proxy-tor-versione.jar"
kill -9 $(ps ax | grep "[j]ava .*.${ARCHIVE_NAME}" | awk '{print $1}') 2>/dev/null

# Lancia il server.
"${JAVA_CMD}" "${APPLICATION_PROPERTIES_BOOT_LOAD}" -jar "${ARCHIVE_NAME}"

