# Tomlin.no API

Backend API for homepage (http://tomlin.no) and possible other projects.


### Build and Run

Run application through IDE or use the following commands:
```bash
./gradlew build

java -jar build/libs/tomlin-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### JVM Options
```jvm
-Dspring.profiles.active=prod
```

### Database
```bash
mysql_secure_installation
mysql -u root -p
mysql.server start
```
