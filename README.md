# slf4j-metrics-publisher

[![Build Status](https://travis-ci.org/societe-generale/slf4j-metrics-publisher.svg?branch=master)](https://travis-ci.org/societe-generale/slf4j-metrics-publisher)
[![Coverage Status](https://coveralls.io/repos/github/societe-generale/slf4j-metrics-publisher/badge.svg?branch=master)](https://coveralls.io/github/societe-generale/slf4j-metrics-publisher?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.societegenerale.commons/slf4j-metrics-publisher/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.societegenerale.commons/slf4j-metrics-publisher)


## Context

This library is used when using Elastic stack, and sending the logs to Logstash using Logback with a config similar to this : 

      <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
            <destination>${LOGSTASH_URL}</destination>
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
               <customFields>{"service.name":"my-service-name",
                  "service.hostname":"\${HOSTNAME}",
                  "service.version": "${project_version}"}
               </customFields>
            </encoder>
            <keepAliveDuration>1 minutes</keepAliveDuration>
      </appender> 

With above configuration and a properly configured Elastic stack, your applicative logs are sent to logstash "wrapped" as a Json document, and you'll be able to browse them in Kibana.

But what if in addition to the applicative logs, you would also like to send custom metrics ?

## Getting started

This library contains only a single class, but packaging it like this brings consistency to the projects that use it (instead of copy/pasting the class in all your projects).

Import the library in your Maven project by adding this dependency : 

```xml
<dependency>
  <groupId>com.societegenerale.commons</groupId>
  <artifactId>slf4j-metrics-publisher</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Publishing the metric and its value(s)

Once the library is added to your classpath, creating and publishing events is as simple as this : 

``` java
  Metric userLoggedInMetric=Metric.functional("user-logged-in");
  userLoggedInMetric.addAttribute("duration",duration);
  userLoggedInMetric.publish(); 
```

- We first create the metric, giving it a type (_functional_) and a name (_user-logged-in_)
- We then add an attribute to it (_duraton_) : it's a key/value pair, the value being a String. The attributes are stored in a Map, and we can add as many entries as we want.
- When "publishing" the metric, the attributes entries are read, and put in the MDC (see [here](http://logback.qos.ch/manual/mdc.html) for more infos), and a regular logging call is made. But it is "enriched" with all the key/value pairs from the MDC, ie our metric and its attributes.     

### Receiving the metric

The metric is "piggy-backing" on a regular log event, encoded in Json and sent to Logstash. It has special attributes that enable to configure a filter in Logstash and redirect the Json document to a special Elastic index (if that's what you want to do).

The Json document will have these basic attributes : 
- **metricName** : in our example above, it's _user-logged-in_
- **metricType** : typically, _TECHNICAL_ or _FUNCTIONAL_ - but you can create metrics with custom types.    

but also all the attributes that you've added in it. 

### Outcome

By storing these "metric documents" in a special ElasticSearch index, you can then make a Kibana dashboard showing them in few minutes, so for example the number of "user-logged-in" events, and the min/max/average/percentiles duration for it. 

