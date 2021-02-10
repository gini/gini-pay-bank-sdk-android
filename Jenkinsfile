#!/usr/bin/env groovy
pipeline {
    agent any
    environment {
        NEXUS_MAVEN = credentials('external-nexus-maven-repo-credentials')
        GIT = credentials('github')
//        COMPONENT_API_EXAMPLE_APP_KEYSTORE_PSW = credentials('gini-vision-library-android_component-api-example-app-release-keystore-password')
//        COMPONENT_API_EXAMPLE_APP_KEY_PSW = credentials('gini-vision-library-android_component-api-example-app-release-key-password')
//        SCREEN_API_EXAMPLE_APP_KEYSTORE_PSW = credentials('gini-vision-library-android_screen-api-example-app-release-keystore-password')
//        SCREEN_API_EXAMPLE_APP_KEY_PSW = credentials('gini-vision-library-android_screen-api-example-app-release-key-password')
//        EXAMPLE_APP_CLIENT_CREDENTIALS = credentials('gini-vision-library-android_gini-api-client-credentials')
        JAVA9 = '/Users/mobilecd/java-vm/jdk-9.0.4.jdk/Contents/Home'
    }
    stages {
        stage('Import Pipeline Libraries') {
            steps{
                library 'android-tools'
            }
        }
        stage('Build') {
            when {
                anyOf {
                    not {
                        branch 'main'
                    }
                    allOf {
                        branch 'main'
                        expression {
                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                            return !tag.isEmpty()
                        }
                    }
                }
            }
            steps {
                sh './gradlew clean ginipaybank:assembleDebug ginipaybank:assembleRelease'
            }
        }
        stage('Unit Tests') {
            when {
                anyOf {
                    not {
                        branch 'main'
                    }
                    allOf {
                        branch 'main'
                        expression {
                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                            return !tag.isEmpty()
                        }
                    }
                }
            }
            steps {
                sh './gradlew ginipaybank:testDebugUnitTest -Dorg.gradle.java.home=$JAVA9'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'ginipaybank/build/outputs/test-results/testDebugUnitTest/*.xml'
                    publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'ginipaybank/build/reports/tests/testDebugUnitTest', reportFiles: 'index.html', reportName: 'Unit Test Results', reportTitles: ''])
                }
            }
        }
        stage('Code Coverage') {
            when {
                anyOf {
                    not {
                        branch 'main'
                    }
                    allOf {
                        branch 'main'
                        expression {
                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                            return !tag.isEmpty()
                        }
                    }
                }
            }
            steps {
                sh './gradlew ginipaybank:jacocoTestDebugUnitTestReport -Dorg.gradle.java.home=$JAVA9'
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'ginipaybank/build/jacoco/jacocoHtml', reportFiles: 'index.html', reportName: 'Code Coverage Report', reportTitles: ''])
            }
        }
        stage('Code Analysis') {
            when {
                anyOf {
                    not {
                        branch 'main'
                    }
                    allOf {
                        branch 'main'
                        expression {
                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                            return !tag.isEmpty()
                        }
                    }
                }
            }
            steps {
                sh './gradlew ginipaybank:lint ginipaybank:checkstyle ginipaybank:pmd'
                androidLint canComputeNew: false, defaultEncoding: '', healthy: '', pattern: 'ginipaybank/build/reports/lint-results.xml', unHealthy: ''
                checkstyle canComputeNew: false, defaultEncoding: '', healthy: '', pattern: 'ginipaybank/build/reports/checkstyle/checkstyle.xml', unHealthy: ''
                pmd canComputeNew: false, defaultEncoding: '', healthy: '', pattern: 'ginipaybank/build/reports/pmd/pmd.xml', unHealthy: ''
            }
        }
        stage('Build Documentation') {
            when {
                anyOf {
                    not {
                        branch 'main'
                    }
                    allOf {
                        branch 'main'
                        expression {
                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                            return !tag.isEmpty()
                        }
                    }
                }
            }
            steps {
                withEnv(["PATH+=/usr/local/bin"]) {
                    sh 'scripts/build-sphinx-doc.sh'
                }
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'ginipaybank/src/doc/build/html', reportFiles: 'index.html', reportName: 'Documentation', reportTitles: ''])
            }
        }
        stage('Generate Dokka') {
            when {
                anyOf {
                    not {
                        branch 'main'
                    }
                    allOf {
                        branch 'main'
                        expression {
                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                            return !tag.isEmpty()
                        }
                    }
                }
            }
            steps {
                sh './gradlew ginipaybank:dokkaHtml'
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'ginipaybank/build/dokka/ginipaybank', reportFiles: 'index.html', reportName: 'Gini Pay Bank KDoc', reportTitles: ''])
            }
        }
        stage('Archive Artifacts') {
            when {
                anyOf {
                    not {
                        branch 'main'
                    }
                    allOf {
                        branch 'main'
                        expression {
                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                            return !tag.isEmpty()
                        }
                    }
                }
            }
            steps {
                sh 'cd ginipaybank/build/jacoco && zip -r testCoverage.zip jacocoHtml && cd -'
                archiveArtifacts 'ginipaybank/build/outputs/aar/*.aar,ginipaybank/build/jacoco/testCoverage.zip'
            }
        }
//        stage('Build Example Apps') {
//            when {
//                anyOf {
//                    not {
//                        branch 'main'
//                    }
//                    allOf {
//                        branch 'main'
//                        expression {
//                            def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
//                            return !tag.isEmpty()
//                        }
//                    }
//                }
//            }
//            steps {
//                sh './gradlew screenapiexample::clean screenapiexample::assembleRelease -PreleaseKeystoreFile=screen_api_example.jks -PreleaseKeystorePassword="$SCREEN_API_EXAMPLE_APP_KEYSTORE_PSW" -PreleaseKeyAlias=screen_api_example -PreleaseKeyPassword="$SCREEN_API_EXAMPLE_APP_KEY_PSW" -PclientId=$EXAMPLE_APP_CLIENT_CREDENTIALS_USR -PclientSecret=$EXAMPLE_APP_CLIENT_CREDENTIALS_PSW'
//                sh './gradlew componentapiexample::clean componentapiexample::assembleRelease -PreleaseKeystoreFile=component_api_example.jks -PreleaseKeystorePassword="$COMPONENT_API_EXAMPLE_APP_KEYSTORE_PSW" -PreleaseKeyAlias=component_api_example -PreleaseKeyPassword="$COMPONENT_API_EXAMPLE_APP_KEY_PSW" -PclientId=$EXAMPLE_APP_CLIENT_CREDENTIALS_USR -PclientSecret=$EXAMPLE_APP_CLIENT_CREDENTIALS_PSW'
//                archiveArtifacts 'screenapiexample/build/outputs/apk/release/screenapiexample-release.apk,componentapiexample/build/outputs/apk/release/componentapiexample-release.apk,screenapiexample/build/outputs/mapping/release/mapping.txt,componentapiexample/build/outputs/mapping/release/mapping.txt'
//            }
//        }
        stage('Release Documentation') {
            when {
                expression {
                    def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                    return !tag.isEmpty()
                }
                expression {
                    boolean publish = false
                    try {
                        def version = sh(returnStdout: true, script: './gradlew -q printLibraryVersion').trim()
                        def sha = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                        input "Release documentation for ${version} from branch ${env.BRANCH_NAME} commit ${sha}?"
                        publish = true
                    } catch (final ignore) {
                        publish = false
                    }
                    return publish
                }
            }
            steps {
                sh 'scripts/release-javadoc.sh $GIT_USR $GIT_PSW'
                sh 'scripts/release-doc.sh $GIT_USR $GIT_PSW'
            }
        }
        stage('Release Library Snapshot') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
                    ./gradlew ginipaybank:uploadArchives \
                    -PmavenSnapshotsRepoUrl=https://repo.gini.net/nexus/content/repositories/snapshots \
                    -PrepoUser=$NEXUS_MAVEN_USR \
                    -PrepoPassword=$NEXUS_MAVEN_PSW
                '''
            }
        }
        stage('Release Library') {
            when {
                expression {
                    def tag = sh(returnStdout: true, script: 'git tag --contains $(git rev-parse HEAD)').trim()
                    return !tag.isEmpty()
                }
                expression {
                    boolean publish = false
                    try {
                        def version = sh(returnStdout: true, script: './gradlew -q printLibraryVersion').trim()
                        def sha = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                        input "Release ${version} from branch ${env.BRANCH_NAME} commit ${sha}?"
                        publish = true
                    } catch (final ignore) {
                        publish = false
                    }
                    return publish
                }
            }
            steps {
                sh '''
                    ./gradlew ginipaybank:uploadArchives \
                    -PmavenRepoUrl=https://repo.gini.net/nexus/content/repositories/open \
                    -PrepoUser=$NEXUS_MAVEN_USR \
                    -PrepoPassword=$NEXUS_MAVEN_PSW
                '''
            }
        }
    }
}
