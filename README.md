[![Maven](https://img.shields.io/maven-central/v/eu.eureka-bpo.log4j2/gmail-appender.svg)](https://repo.maven.apache.org/maven2/eu/eureka-bpo/log4j2/gmail-appender/)

## GMail Appender for Log4j2

With help of this appender user can receive notifications about errors direct on GMail.

Usage of GMail Appender is possible after [Google OAuth2 settings](https://developers.google.com/identity/protocols/oauth2/service-account) are set.

#### Simplest example

The simplest appender configuration for usage with log4j2:

```xml
  <GMail name="GMailLogger"
    serviceAccountKey="/path/to/key/file.json"
    delegate="example@gmail.com"
    subject="Subject of emails"
    recipients="recipient@example.com" />
```

#### Attributes description

* serviceAccountKey - absolute path to key file of [service account](https://cloud.google.com/iam/docs/service-account-overview)
* delegate - user account, which [delegates](https://developers.google.com/identity/protocols/oauth2/service-account#delegatingauthority) sending emails to service account<br/>__Note__: on step 5 scope `https://www.googleapis.com/auth/gmail.send` should be granted
* subject - subject of sending email
* recipients - list of recipients of sending emails. Must follow [RFC822](https://datatracker.ietf.org/doc/html/rfc822) syntax
* contentType (optional) - mime type of email's body

#### Full example

Full example of log4j2 configuration with GMail appender:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="[Log4j]%d{HH:mm:ss.SSS} %-5level - %msg%n" />
    </Console>
    <GMail name="GMailLogger"
      serviceAccountKey="/path/to/key/file.json"
      delegate="example@gmail.com"
      subject="Subject of emails"
      recipients="recipient@example.com">
    </GMail>
  </Appenders>
  <Loggers>
    <Root level="info">
      <AppenderRef ref="Console" />
      <AppenderRef ref="GMailLogger" level="error" />
    </Root>
  </Loggers>
</Configuration>
```

#### System requirements

Requires Java >= 11 and Log4j >= 2.11.2
