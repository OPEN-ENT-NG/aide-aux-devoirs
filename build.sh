#!/bin/bash

if [ ! -e node_modules ]
then
  mkdir node_modules
fi

if [ -z ${USER_UID:+x} ]
then
  export USER_UID=1000
  export GROUP_GID=1000
fi

clean () {
  docker-compose run --rm -u "$USER_UID:$GROUP_GID" gradle gradle clean
}

buildNode () {
  docker-compose run --rm -u "$USER_UID:$GROUP_GID" node sh -c "npm install && node_modules/gulp/bin/gulp.js build"
}

buildGradle () {
  docker-compose run --rm -u "$USER_UID:$GROUP_GID" gradle gradle shadowJar install publishToMavenLocal
}

publish () {
  if [ -e "?/.gradle" ] && [ ! -e "?/.gradle/gradle.properties" ]
  then
    echo "odeUsername=$NEXUS_ODE_USERNAME" > "?/.gradle/gradle.properties"
    echo "odePassword=$NEXUS_ODE_PASSWORD" >> "?/.gradle/gradle.properties"
    echo "sonatypeUsername=$NEXUS_SONATYPE_USERNAME" >> "?/.gradle/gradle.properties"
    echo "sonatypePassword=$NEXUS_SONATYPE_PASSWORD" >> "?/.gradle/gradle.properties"
  fi
  docker-compose run --rm -u "$USER_UID:$GROUP_GID" gradle gradle publish
  RECENT_VERSION_MODULE=$(grep version= gradle.properties | awk -F "=" '{ print $2 }' | sed -e "s/\r//")
  LOCAL_BRANCH=`echo $GIT_BRANCH | sed -e "s|origin/||g"`
  if [ $LOCAL_BRANCH = "test-master" ]
  then
    sed -i "s/version=$RECENT_VERSION_MODULE/version=MASTER/" gradle.properties && sed -i "s/$RECENT_VERSION_MODULE/MASTER/" deployment/*/conf.json.template
    rename 's/$RECENT_VERSION_MODULE/MASTER/' build/libs/*.jar 
  elif [ $LOCAL_BRANCH = "test-dev" ]
  then
    sed -i "s/version=$RECENT_VERSION_MODULE/version=DEV/" gradle.properties && sed -i "s/$RECENT_VERSION_MODULE/DEV/" deployment/*/conf.json.template
    rename 's/$RECENT_VERSION_MODULE/MASTER/' build/libs/*.jar
  elif [ $LOCAL_BRANCH = "test-next" ]
  then
    sed -i "s/version=$RECENT_VERSION_MODULE/version=NEXT/" gradle.properties && sed -i "s/$RECENT_VERSION_MODULE/NEXT/" deployment/*/conf.json.template
    rename 's/$RECENT_VERSION_MODULE/MASTER/' build/libs/*.jar
  fi
  docker-compose run --rm -u "$USER_UID:$GROUP_GID" gradle gradle publish
}

for param in "$@"
do
  case $param in
    clean)
      clean
      ;;
    buildNode)
      buildNode
      ;;
    buildGradle)
      buildGradle
      ;;
    install)
      buildGradle # buildNode && buildGradle
      ;;
    publish)
      publish
      ;;
    *)
      echo "Invalid argument : $param"
  esac
  if [ ! $? -eq 0 ]; then
    exit 1
  fi
done

