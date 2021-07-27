#!groovy
pipeline {

  agent any

  tools { nodejs "nodejs" }

  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }

  environment {
    APP_NAME = 'yudao-admin'
    PROJECT_DIR='yudao-admin-ui'
    NGINX_WORKDIR = '/home/pi/mydata/nginx/html/'
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
        sh 'cnpm --prefix '+ "${env.PROJECT_DIR}" +' install'
        sh 'cnpm --prefix '+ "${env.PROJECT_DIR}" +' run build:prod'
      }
    }

    stage('部署') {
      steps {
        sh 'rm -f ' + "${env.APP_NAME}" + '/'+ "${env.APP_NAME}" + '.tar.gz'
        sh 'mkdir -p ' + "${env.NGINX_WORKDIR}" + "${env.APP_NAME}"
        sh 'cp -rf ' + "${env.PROJECT_DIR}" + '/dist/. ' + "${env.NGINX_WORKDIR}" + "${env.APP_NAME}"
        sh 'tar -zcvf ' + "${env.PROJECT_DIR}" + '/'+ "${env.PROJECT_DIR}" + '.tar.gz ' + "${env.PROJECT_DIR}" + '/dist/'
        archiveArtifacts  "${env.PROJECT_DIR}" + '/'+ "${env.PROJECT_DIR}" + '.tar.gz'
        //TODO 考虑刷新缓存的问题
      }
    }
  }
}
