
stage ('build deploy') { 
      node{
       git branch: '${BUILD_BRANCH}', credentialsId: 'jenkins-github-key', url: 'https://github.com/blemobi/${JOB_NAME}.git'
       def mvnHome = tool 'M3'
       sh """
       ${mvnHome}/bin/mvn -B -f pom.xml clean install -Dmaven.test.skip
       """

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

       fi 	 
       """       
       }
   }
}