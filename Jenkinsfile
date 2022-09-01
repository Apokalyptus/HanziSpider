pipeline {
   agent any
 
   environment {
      dockerHome = tool 'JenkinsDocker'
      mavenHome = tool 'JenkinsMaven'
      PATH = "$dockerHome/bin:$mavenHome/bin:$PATH"
   }
 
   stages{
 
      stage('Compile') {
         steps{
            sh "mvn clean compile"
         }
      }
 
 //     stage('Test') {
 //        steps{
 //           sh "mvn test"
 //        }
 //     }
 
 //     stage('Integration Test') {
 //        steps{
 //            sh "mvn failsafe:integration-test failsafe:verify"
 //        }
 //     }
 
      stage('Package') {
         steps{
            script{
               sh "mvn package -DskipTests"
            }
         }
      }
   }
}
