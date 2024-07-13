module.exports = {
  apps : [{
    name: 'auth-microservice',
    script: 'docker',
    args: 'run -d --name auth-microservice -p 8081:8080 your-dockerhub-username/auth-microservice:${BUILD_NUMBER}',
    exec_mode: 'fork',
    autorestart: true,
    watch: false
  },
  {
    name: 'food-microservice',
    script: 'docker',
    args: 'run -d --name food-microservice -p 8082:8080 your-dockerhub-username/food-microservice:${BUILD_NUMBER}',
    exec_mode: 'fork',
    autorestart: true,
    watch: false
  }]
};
