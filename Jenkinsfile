pipeline {
    agent any

    tools {
        maven 'maven-3.9.14'
    }

    parameters {
        choice(
            name: 'branch',
            choices: ['master', 'dev'],
            description: '请选择要构建的分支'
        )
    }

    environment {
        APP_NAME = 'platform-admin'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        // 阶段1：拉取选中的分支
        stage('拉取代码') {
            steps {
                echo "正在拉取分支: ${params.branch}"
                // TODO: 替换为 platform 项目实际的 Git 地址与凭据
                git branch: "${params.branch}",
                    url: 'git@your-git.com:your-org/platform-parent.git',
                    credentialsId: 'gitee'
            }
        }

        // 阶段2：Maven 打包（多模块，仅打包 platform-admin 并联编其依赖模块）
        stage('Maven打包') {
            steps {
                sh 'mvn clean package -pl platform-admin -am -DskipTests=true'
            }
        }

        // 阶段3：归档产物
        stage('归档产物') {
            steps {
                dir('platform-admin') {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
                echo "✅ 分支 ${params.branch} 打包完成"
            }
        }

        // 阶段4：构建 Docker 镜像（构建上下文为 platform-admin 目录）
        stage('构建Docker镜像') {
            steps {
                dir('platform-admin') {
                    sh 'docker build -t ${APP_NAME}:${IMAGE_TAG} .'
                    echo "✅ Docker 镜像构建成功: ${APP_NAME}:${IMAGE_TAG}"
                }
            }
        }

        // 阶段5：删除旧容器和镜像
        stage('清理旧资源') {
            steps {
                script {
                    def OLD_TAG = (BUILD_NUMBER as int) - 1
                    sh """
                            docker stop ${APP_NAME} 2>/dev/null || true
                            docker rm ${APP_NAME} 2>/dev/null || true
                            docker rmi ${APP_NAME}:${OLD_TAG} 2>/dev/null || true
                        """
                    echo "✅ 清理完成"
                }
            }
        }

        // 阶段6：查看镜像信息
        stage('查看镜像') {
            steps {
                sh "docker images | grep ${APP_NAME}"
            }
        }

        // 阶段7：用 docker-compose 启动容器（依赖 MySQL/Redis/RabbitMQ 走外部 192.168.244.128）
        stage('启动容器') {
            steps {
                script {
                    def SPRING_PROFILE = (params.branch == 'master') ? 'prod' : 'dev'
                    sh """
                            export APP_NAME=${APP_NAME}
                            export IMAGE_TAG=${IMAGE_TAG}
                            export SPRING_PROFILE=${SPRING_PROFILE}
                            docker-compose down
                            docker-compose up -d
                        """
                    echo "✅ 容器启动成功！"
                }
            }
        }
    }

    post {
        success {
            echo "构建成功！分支: ${params.branch}"
            echo "镜像: ${APP_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "构建失败！分支: ${params.branch}"
        }
    }
}
