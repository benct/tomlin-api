# Tomlin.no API

Backend API for homepage (http://tomlin.no) and possible other projects.


### Build and Run
```bash
./gradlew build

java -jar build/libs/tomlin-api-1.0.0.jar --spring.profiles.active=prod
```

### JVM Options
```jvm
-Dspring.profiles.active=prod
```

### Upgrade dependencies
```bash
./gradlew dependencyUpdates
```

### Service
Move `tomlin-api.service` to `/etc/systemd/system/`
```bash
sudo systemctl start tomlin-api
sudo systemctl enable tomlin-api
```

### Database
```bash
mysql_secure_installation
mysql -u root -p
```
