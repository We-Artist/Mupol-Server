# Mupol Spring Server

## 배포 환경
Spring boot jar 생성
```
./mvnw package 
```

MySQL
```
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password --name mupol-mysql mysql:8.0.23
```  

Spring boot
```
nohup java -jar "jar파일이름" > log.out &
```

현재 배포된 주소
```
http://3.34.90.247:8080/v1
http://3.34.90.247:8080/swagger-ui.html
```

## 패키지 관리

- spring-boot 2.4.4
- maven 
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-oauth2-client
- lombok
- jpa
- mysql
- springfox-swagger2
- springfox-swagger-ui
- mysql-connector-java
- gson
- jjwt
- spring-cloud-starter-aws
- yaml-resource-bundle