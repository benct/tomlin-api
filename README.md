# Tomlin.no API

Backend API for homepage (http://tomlin.no) and possible other projects.

[![Website](https://img.shields.io/website-up-down-green-red/https/api.tomlin.no.svg?label=api)](https://api.tomlin.no)
[![GitHub last commit](https://img.shields.io/github/last-commit/benct/tomlin-api.svg)](https://github.com/benct/tomlin-api)
[![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/benct/tomlin-api.svg)](https://github.com/benct/tomlin-api)
[![GitHub repo size in bytes](https://img.shields.io/github/repo-size/benct/tomlin-api.svg)](https://github.com/benct/tomlin-api)
[![GitHub licence](https://img.shields.io/github/license/benct/tomlin-api.svg)](https://github.com/benct/tomlin-api/blob/master/LICENCE)

<sub><i>API, Spring, Spring Boot, REST, Kotlin, Gradle, OkHttp, Nginx, MySQL</i></sub>

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

### Certbot
```bash
sudo apt install certbot python-certbot-nginx
sudo certbot --nginx
sudo certbot renew [--dry-run]
```

### Nginx
```bash
sudo nano /etc/nginx/sites-available/default
sudo nginx -t
sudo systemctl restart nginx
```

Proxy configuration:
```
location / {
    proxy_pass http://localhost:8081/;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Port $server_port;
}
```

### Database
```bash
sudo apt install mariadb-server
mysql_secure_installation
mysql -u root -p
```
