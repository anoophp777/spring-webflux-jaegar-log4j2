# Integrating Jaeger with Spring Boot using Zipkin and use OpenTracing APIs

## Overview

Jaeger exposes Zipkin collector. We can use Sleuth+Zipkin in our spring boot app with minimal configurations to achieve 3 things:

> 1. Send instrumentation to Jaeger via its Zipkin collector
	
> 2. Print Trace and Span related information in the logs using log4j2

> 3. Be OpenTracing compatible!!

### The problem:

Jaeger is an implementation of OpenTracing. It requires a Jaeger agent to be part of the instrumented app, to collect data and send it to the Jaeger server. 

This is further complicated when we want to use Webflux(Reactor or RxJava) in our application. Reactor or RxJava executes work in separate threads asynchronously. This makes it challenging to gather timing data needed to troubleshoot latency problems in microservice architectures. 

Sleuth is the Spring prescribed solution for distributed tracing. Sleuth also has the additional advantage of being compatible with Webflux AND Spring MVC. Spring Cloud Sleuth sets up useful log formatting for you that prints the trace ID and the span ID.

OpenTracing is a recommended way, as it provides a API specification which will give us the freedom to move to any implementation of OpenTracing in the future.   

**Using Jaeger agent in our classpath will devoid us of the advantages of Sleuth(webflux support + automatic logging of trace data).**

### The Solution:

OpenZipkin is the fully open-source version of Zipkin, a project that originated at Twitter in 2010, and is based on the Google Dapper papers. Sleuth integrates very well with Zipkin. 

Jaeger exposes Zipkin collector.

[https://www.jaegertracing.io/docs/1.8/getting-started/#migrating-from-zipkin](https://www.jaegertracing.io/docs/1.8/getting-started/#migrating-from-zipkin)

Sleuth also supports OpenTracing, provided OpenTracing API's are on the classpath.

So, I have configured my Spring Boot app to use Sleuth + Zipkin and configured the "spring.zipkin.base-url" to send data to Jaeger's Zipkin collector. I've OpenTracing APIs on the classpath which seeing which sleuth will autoconfigure OpenTracing using Brave.

### Pre-Requisites:

If don't have a local set up of Jaeger running in your system, you can easily set it up using docker as below:

	$ docker run -d --name jaeger -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp -p 5778:5778 -p 16686:16686 -p 14268:14268 -p 9411:9411 jaegertracing/all-in-one:1.8
  
The above will expose Jaeger's Zipkin collector at http://localhost:9411.

Sleuth-Zipkin by default sends all trace data to http://localhost:9411. 

If your Jaeger server is at a different url, you can force Zipkin to send the data to the Jaeger server(at Zipkin collector) by overrding 

	spring.zipkin.base-url=http://<your-Jaeger-server>:<port>
  
### How to run?

cd to spring-webflux-Jaeger-log4j2 and run
  
	mvn spring-boot:run
	
Once the application is booted up, you can test the reactive endpoint by curling:

	curl http://localhost:7070/byPriceReactive?maxPrice=1
	
You will notice log4j2 is printing in the console the following:

*NOTE: Don't forget to notice the first 2 logs which print the trace and span using OpenTracing APIs*

	2018-11-19 12:51:38.381 [{X-B3-SpanId=8b1e7c40420e8324, X-B3-TraceId=5bf264826fe743518b1e7c40420e8324, X-Span-Export=true, spanExportable=true, spanId=8b1e7c40420e8324, traceId=5bf264826fe743518b1e7c40420e8324}] DEBUG ahallim-1ef960 --- [nio-7070-exec-2] a.h.w.RestaurantController               : tracer: brave.opentracing.BraveTracer@517bafcb
	2018-11-19 12:51:38.382 [{X-B3-SpanId=8b1e7c40420e8324, X-B3-TraceId=5bf264826fe743518b1e7c40420e8324, X-Span-Export=true, spanExportable=true, spanId=8b1e7c40420e8324, traceId=5bf264826fe743518b1e7c40420e8324}]  INFO ahallim-1ef960 --- [nio-7070-exec-2] a.h.w.RestaurantController               : active span: brave.opentracing.BraveSpan@1c1c76cd
	2018-11-19 12:51:38.382 [{X-B3-SpanId=8b1e7c40420e8324, X-B3-TraceId=5bf264826fe743518b1e7c40420e8324, X-Span-Export=true, spanExportable=true, spanId=8b1e7c40420e8324, traceId=5bf264826fe743518b1e7c40420e8324}] DEBUG ahallim-1ef960 --- [nio-7070-exec-2] a.h.w.RestaurantController               : starting statement
	2018-11-19 12:51:38.393 [{X-B3-ParentSpanId=8b1e7c40420e8324, X-B3-SpanId=9cbbfc8112535548, X-B3-TraceId=5bf264826fe743518b1e7c40420e8324, X-Span-Export=true, parentId=8b1e7c40420e8324, spanExportable=true, spanId=9cbbfc8112535548, traceId=5bf264826fe743518b1e7c40420e8324}] DEBUG ahallim-1ef960 --- [nio-7070-exec-2] a.h.w.RestaurantService                  : inside byPrice Reactive
	2018-11-19 12:51:39.423 [{X-B3-SpanId=8b1e7c40420e8324, X-B3-TraceId=5bf264826fe743518b1e7c40420e8324, X-Span-Export=true, spanExportable=true, spanId=8b1e7c40420e8324, traceId=5bf264826fe743518b1e7c40420e8324}] DEBUG ahallim-1ef960 --- [     parallel-1] a.h.w.RestaurantController               : found restaurant McDonalds for $1.0
	2018-11-19 12:51:39.423 [{X-B3-SpanId=8b1e7c40420e8324, X-B3-TraceId=5bf264826fe743518b1e7c40420e8324, X-Span-Export=true, spanExportable=true, spanId=8b1e7c40420e8324, traceId=5bf264826fe743518b1e7c40420e8324}] DEBUG ahallim-1ef960 --- [     parallel-1] a.h.w.RestaurantController               : done!
 

To test the traditional non-reactive endpoint: 
	
	curl http://localhost:7070/byPriceMVC?maxPrice=1

You will see in the logs:

*NOTE: Don't forget to notice the first 2 logs which print the trace and span using OpenTracing APIs*

	2018-11-19 12:54:41.117 [{X-B3-SpanId=ead6d4e8397e7dd0, X-B3-TraceId=5bf265397a815f2cead6d4e8397e7dd0, X-Span-Export=true, spanExportable=true, spanId=ead6d4e8397e7dd0, traceId=5bf265397a815f2cead6d4e8397e7dd0}] DEBUG ahallim-1ef960 --- [nio-7070-exec-6] a.h.w.RestaurantController               : tracer: brave.opentracing.BraveTracer@43c6e747
	2018-11-19 12:54:41.118 [{X-B3-SpanId=ead6d4e8397e7dd0, X-B3-TraceId=5bf265397a815f2cead6d4e8397e7dd0, X-Span-Export=true, spanExportable=true, spanId=ead6d4e8397e7dd0, traceId=5bf265397a815f2cead6d4e8397e7dd0}]  INFO ahallim-1ef960 --- [nio-7070-exec-6] a.h.w.RestaurantController               : active span: brave.opentracing.BraveSpan@787945bc
	2018-11-19 12:54:41.118 [{X-B3-SpanId=ead6d4e8397e7dd0, X-B3-TraceId=5bf265397a815f2cead6d4e8397e7dd0, X-Span-Export=true, spanExportable=true, spanId=ead6d4e8397e7dd0, traceId=5bf265397a815f2cead6d4e8397e7dd0}] DEBUG ahallim-1ef960 --- [nio-7070-exec-6] a.h.w.RestaurantController               : starting statement
	2018-11-19 12:54:41.118 [{X-B3-ParentSpanId=ead6d4e8397e7dd0, X-B3-SpanId=521cd352679aee4f, X-B3-TraceId=5bf265397a815f2cead6d4e8397e7dd0, X-Span-Export=true, parentId=ead6d4e8397e7dd0, spanExportable=true, spanId=521cd352679aee4f, traceId=5bf265397a815f2cead6d4e8397e7dd0}] DEBUG ahallim-1ef960 --- [nio-7070-exec-6] a.h.w.RestaurantService                  : inside byPrice MVC
		
	
In Jaeger you will see the entries as below:
![alt text](https://github.com/anoophp777/spring-webflux-jaegar-log4j2/blob/master/src/main/resources/images/Screen%20Shot%202018-11-16%20at%2011.36.22%20PM.png "Jaeger image of trace")
