#  PMS Feeds

This application is for feeding data from Caspian PMS to Recon Engine InfluxDB

## Building

```shell script
./gradlew build
```

## Running Tests

```shell script
./gradlew test
```

## Running the Executable Jar

```shell script
java -jar build/libs/pms-feeds-{version}.jar --spring.config.additional-location=file:{config_location} --spring.profiles.active=credentials
```

### Supporting HTTP/HTTPS proxy

Please specify the system properties **http.proxyHost**, **http.proxyPort**, **https.proxyHost** and **https.proxyPort**.
