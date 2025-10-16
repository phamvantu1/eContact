1. clone source code : 
  https://github.com/phamvantu1/eContact

2. cài đặt intellji

3. Cài đặt postgres trên môi trường máy tình local ( xem YT ) 
 -  Hoặc sử dụng docker  :
      Cài đặt docker
      Vào cmd sử dụng các lệnh
      
      docker create --name postgres-local -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=123456 -p 5432:5432 postgres:15
      
      docker start postgres-local
      
      CREATE DATABASE eContract_customer;
      CREATE DATABASE eContract_notification;
      …..
  
 -  sử dụng DBeaver để tạo và quản lý DB
 -  Tạo các DB : (eContract_customer ,eContract_notification)  

4. Cài đặt redis (docker)
  - docker create --name redis-local -p 6379:6379 redis:latest ( tạo )
  
  - docker start redis-local ( bật )
  
 - docker stop redis-local (tắt)

5. cài đặt java 21 + maven
 -  build file jar
    trỏ đến lần lượt từng service để build file jar
      chạy câu lệnh :  mvn clean install -DskipTests
        thứ tự
        common-library
        gateway
        các service còn lại ( trừ discovery )

6. chạy service
 -  thứ tự chạy lần lượt
      discovery
      gateway
      auth
      các service còn lại
7. swagger
  - auth service
      http://localhost:8082/api/auth/swagger-ui/index.html
  - customer service
      http://localhost:8083/api/auth/swagger-ui/index.html
  - contract service
      http://localhost:8084/api/auth/swagger-ui/index.html
  - notification service
    http://localhost:8085/api/auth/swagger-ui/index.html

8. gateway  : cổng 8080
