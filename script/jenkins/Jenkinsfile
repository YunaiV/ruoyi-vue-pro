#!groovy
pipeline {

    agent any

    parameters {
        string(name: 'TAG_NAME', defaultValue: '', description: '')
    }

    environment {
        // DockerHub 凭证 ID(登录您的 DockerHub)
        DOCKER_CREDENTIAL_ID = 'dockerhub-id'
        //  GitHub 凭证 ID (推送 tag 到 GitHub 仓库)
        GITHUB_CREDENTIAL_ID = 'github-id'
        // kubeconfig 凭证 ID (访问接入正在运行的 Kubernetes 集群)
        KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
        // 镜像的推送
        REGISTRY = 'docker.io'
        //  DockerHub 账号名
        DOCKERHUB_NAMESPACE = 'docker_username'
        // GitHub 账号名
        GITHUB_ACCOUNT = 'https://gitee.com/zhijiantianya/ruoyi-vue-pro'
        // 应用名称
        APP_NAME = 'yudao-server'
        // 应用部署路径
        APP_DEPLOY_BASE_DIR = '/media/pi/KINGTON/data/work/projects/'
    }

    stages {
        stage('检出') {
            steps {
                git url: "https://gitee.com/will-we/ruoyi-vue-pro.git",
                        branch: "devops"
            }
        }

        stage('构建') {
            steps {
                // TODO 解决多环境链接、密码不同配置临时方案
                sh 'if [ ! -d "' + "${env.HOME}" + '/resources" ];then\n' +
                        '  echo "配置文件不存在无需修改"\n' +
                        'else\n' +
                        '  cp  -rf  ' + "${env.HOME}" + '/resources/*.yaml ' + "${env.APP_NAME}" + '/src/main/resources\n' +
                        '  echo "配置文件替换"\n' +
                        'fi'
                sh 'mvn clean package -Dmaven.test.skip=true'
            }
        }

        stage('部署') {
            steps {
                sh 'cp -f ' + ' bin/deploy.sh ' + "${env.APP_DEPLOY_BASE_DIR}" + "${env.APP_NAME}"
                sh 'cp -f ' + "${env.APP_NAME}" + '/target/*.jar ' + "${env.APP_DEPLOY_BASE_DIR}" + "${env.APP_NAME}" +'/build/'
                archiveArtifacts "${env.APP_NAME}" + '/target/*.jar'
                sh 'chmod +x ' + "${env.APP_DEPLOY_BASE_DIR}" + "${env.APP_NAME}" + '/deploy.sh'
                sh 'bash ' + "${env.APP_DEPLOY_BASE_DIR}" + "${env.APP_NAME}" + '/deploy.sh'
            }
        }
    }
}
