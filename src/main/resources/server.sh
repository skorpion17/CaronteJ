#*******************************************************************************
# Copyright (c) 2015, 2016 Emanuele Altomare, Andrea Mayer
#
# This file is part of Proxy2Tor.
# Proxy2Tor is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or 
# (at your option) any later version.
#
# Proxy2Tor is distributed in the hope that it will be useful, 
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Proxy2Tor.  If not, see <http://www.gnu.org/licenses/>
#*******************************************************************************
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

