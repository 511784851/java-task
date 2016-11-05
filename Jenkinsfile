
stage ('build deploy') { 
      node{

       dir ('src/github.com/blemobi/java-gamification'){
          git branch: '${BUILD_BRANCH}', credentialsId: 'jenkins-github-key', url: 'https://github.com/blemobi/${JOB_NAME}.git'
       }
       def mvnHome = tool 'M3'

       withCredentials([[$class: 'StringBinding', credentialsId: 'ssh-shenzhen-saltmaster-jenkins-p1', variable: 'sshszp1']]){
       withCredentials([[$class: 'StringBinding', credentialsId: 'ssh-shenzhen-saltmaster-jenkins-p2', variable: 'sshszp2']]){
       sh """
       cd ${WORKSPACE}/src/github.com/blemobi/java-gamification
       ${mvnHome}/bin/mvn -B -f pom.xml clean install -Dmaven.test.skip
       mv target/blemobi-task-app.jar  blemobi-gamification.jar
       zip -j blemobi-java-gamification.zip blemobi-gamification.jar

       if [[ ${rsync_salt} == "true" ]]
       then
	rsync -ravz -e "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ${sshszp1}" \
	--progress blemobi-${JOB_NAME}.zip \
	${sshszp2}:~/test/run/gamification/
    
	ssh -tt -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ${sshszp1}\
	${sshszp2} "sudo salt '*gamification*' state.highstate"
	fi

       """
	}
	}


	withCredentials([[$class: 'StringBinding', credentialsId: 'jenkins-github-admin-key', variable: 'TOKEN']]) {
       sh """
       if [[ ${release} == "true"  &&   -v releaseVersion ]]
       then
         export GITHUB_TOKEN=${TOKEN}
	 cd $WORKSPACE/src/github.com/blemobi/${JOB_NAME}

	 github-release release \
	     	 --user blemobi \
	         --repo ${JOB_NAME} \
    		 --tag ${releaseVersion} \
		 --name "${JOB_NAME} ${releaseVersion}" \
    		 --description "${JOB_NAME} ${releaseVersion}" 

	 github-release upload --user blemobi --repo ${JOB_NAME} \
     	         --tag ${releaseVersion} --name blemobi-${JOB_NAME}.zip \
                 --file blemobi-${JOB_NAME}.zip

       fi 	 
       """       
       }
   }
}