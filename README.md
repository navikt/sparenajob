# sparenajob
Sparenajob er en cronjobb som brukes for å identifisere planlagte arena-meldinger som det skal gjøres noe med. 

## Technologies used
* Kotlin
* Ktor
* Gradle
* Jackson
* Postgres

## Requirements

* JDK 17
* Docker

## Getting started
### Building the application
#### Compile and package application
To build locally and run the integration tests you can simply run `./gradlew shadowJar` or  on windows 
`gradlew.bat shadowJar`

#### Creating a docker image
Creating a docker image should be as simple as `docker build -t sparenajob .`

#### Running a docker image
`docker run --rm -it -p 8080:8080 sparenajob`

### Upgrading the gradle wrapper
Find the newest version of gradle here: https://gradle.org/releases/ Then run this command:

```./gradlew wrapper --gradle-version $gradleVersjon```

### Contact

This project is maintained by [navikt/teamsykmelding](CODEOWNERS)

Questions and/or feature requests? Please create an [issue](https://github.com/navikt/sparenajob/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-sykmelding](https://nav-it.slack.com/archives/CMA3XV997)
