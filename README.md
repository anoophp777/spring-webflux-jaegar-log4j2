# Integrating Jaegar with Spring Boot using Zipkin

## Overview

Jaegar exposes Zipkin collector. We can use Sleuth+Zipkin in our spring boot app with minimal configurations to achieve 2 things:
	1. Send instrumentation to Jaegar using its Zipkin collector
	2. Spring Trace and Span related information in the logs using either log4j2 or the default logback.

### The problem:

Jaegar is an implementation of OpenTracing. It requires a Jaegar agent to be part of the instrumented app, to collect data and send it to the Jaegar server. 

This is further complicated when we want to use Webflux(Reactor or RxJava) in our application. Reactor or RxJava executes work in separate threads asynchronously. This makes it challenging to gather timing data needed to troubleshoot latency problems in microservice architectures. 

Sleuth is the Spring prescribed solution for distributed tracing. Sleuth also has the additional advantage of being compatible with Webflux AND Spring MVC. Spring Cloud Sleuth sets up useful log formatting for you that prints the trace ID and the span ID. 

**Using Jaegar agent in our classpath will devoid us of the advantages of Sleuth(webflux support + automatic logging of trace data).**

###f The Solution:

OpenZipkin is the fully open-source version of Zipkin, a project that originated at Twitter in 2010, and is based on the Google Dapper papers. Sleuth integrates very well with Zipkin. 

Jaegar exposes Zipkin collector.

[https://www.jaegertracing.io/docs/1.8/getting-started/#migrating-from-zipkin](https://www.jaegertracing.io/docs/1.8/getting-started/#migrating-from-zipkin)


So, I have configured my Spring Boot app to use Sleuth + Zipkin and configured the "spring.zipkin.base-url" to send data to Jaegar's Zipkin collector.

### Pre-Requisites:

If don't have a local set up of Jaegar running in your system, you can easily set it up using docker as below:

	$ docker run -d --name jaeger -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 -p 9411:9411 jaegertracing/all-in-one:1.8
  
The above will expose Zipkin collector at http://localhost:9411.

Zipkin by default sends all trace data to http://localhost:9411. If you Jaegar server is at a different url, you can force Zipkin to send the data to the Jaegar server(at Zipkin collector) by overrding 

	spring.zipkin.base-url=http://<your-jaegar-server>:<port>
  
### How to run?

cd to spring-webflux-jaegar-log4j2 and run
  
	mvn spring-boot:run
	
Once the application is booted up, you can test the reactive endpoint by curling:

	curl http://localhost:7070/byPriceReactive?maxPrice=1
	
You will notice log4j2 is printing in the console the following:

	2018-11-16 22:53:59.094 [{X-B3-SpanId=29721485e7c44eed, X-B3-TraceId=5beefd2f86d88d5529721485e7c44eed, X-Span-Export=true, spanExportable=true, spanId=29721485e7c44eed, traceId=5beefd2f86d88d5529721485e7c44eed}] DEBUG ahallim-1ef960 --- [nio-7070-exec-1] a.h.w.RestaurantController               : starting statement
	2018-11-16 22:53:59.099 [{X-B3-ParentSpanId=29721485e7c44eed, X-B3-SpanId=5d048edf6886199c, X-B3-TraceId=5beefd2f86d88d5529721485e7c44eed, X-Span-Export=true, parentId=29721485e7c44eed, spanExportable=true, spanId=5d048edf6886199c, traceId=5beefd2f86d88d5529721485e7c44eed}] DEBUG ahallim-1ef960 --- [nio-7070-exec-1] a.h.w.RestaurantService                  : inside byPrice Reactive
	2018-11-16 22:54:00.143 [{X-B3-SpanId=29721485e7c44eed, X-B3-TraceId=5beefd2f86d88d5529721485e7c44eed, X-Span-Export=true, spanExportable=true, spanId=29721485e7c44eed, traceId=5beefd2f86d88d5529721485e7c44eed}] DEBUG ahallim-1ef960 --- [     parallel-1] a.h.w.RestaurantController               : found restaurant McDonalds for $1.0
	2018-11-16 22:54:00.143 [{X-B3-ParentSpanId=29721485e7c44eed, X-B3-SpanId=0ccfbf1c4ae956ec, X-B3-TraceId=5beefd2f86d88d5529721485e7c44eed, X-Span-Export=true, parentId=29721485e7c44eed, spanExportable=true, spanId=0ccfbf1c4ae956ec, traceId=5beefd2f86d88d5529721485e7c44eed}] DEBUG ahallim-1ef960 --- [     parallel-1] a.h.w.RestaurantController               : done!
 

To test the traditional non-reactive endpoint: 
	
	curl http://localhost:7070/byPriceMVC?maxPrice=1

You will see in the logs:

	2018-11-16 22:57:19.550 [{X-B3-SpanId=ad8c6f85672b068a, X-B3-TraceId=5beefdf7e1c666e7ad8c6f85672b068a, X-Span-Export=true, spanExportable=true, spanId=ad8c6f85672b068a, traceId=5beefdf7e1c666e7ad8c6f85672b068a}] DEBUG ahallim-1ef960 --- [nio-7070-exec-4] a.h.w.RestaurantController               : starting statement
	2018-11-16 22:57:19.550 [{X-B3-ParentSpanId=ad8c6f85672b068a, X-B3-SpanId=b95326419a7e3af8, X-B3-TraceId=5beefdf7e1c666e7ad8c6f85672b068a, X-Span-Export=true, parentId=ad8c6f85672b068a, spanExportable=true, spanId=b95326419a7e3af8, traceId=5beefdf7e1c666e7ad8c6f85672b068a}] DEBUG ahallim-1ef960 --- [nio-7070-exec-4] a.h.w.RestaurantService                  : inside byPrice MVC
	
In Jaegar you will see the entries as below:
![alt text](https://github.com/anoophp777/spring-webflux-jaegar-log4j2/blob/master/src/main/resources/images/Screen%20Shot%202018-11-16%20at%2011.36.22%20PM.png "Jaegar image of trace")
