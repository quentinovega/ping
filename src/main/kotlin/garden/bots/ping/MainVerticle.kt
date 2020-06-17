package garden.bots.ping

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.obj
import io.vertx.servicediscovery.Record
import io.vertx.servicediscovery.ServiceDiscovery
import io.vertx.servicediscovery.ServiceDiscoveryOptions
import io.vertx.servicediscovery.types.HttpEndpoint

class MainVerticle : AbstractVerticle() {

  private lateinit var discovery: ServiceDiscovery
  private lateinit var record: Record

  override fun stop(stopPromise: Promise<Void>) {
    println("Unregistration process is started (${record.registration})...")

    discovery.unpublish(record.registration) { ar ->
      when {
        ar.failed() -> {
          println("😡 Unable to unpublish the microservice: ${ar.cause().message}")
          stopPromise.fail(ar.cause())
        }
        ar.succeeded() -> {
          println("👋 bye bye ${record.registration}")
          stopPromise.complete()
        }
      }
    }
  }

  override fun start(startPromise: Promise<Void>) {

    // ===== Discovery ===
    val redisPort= System.getenv("REDIS_PORT")?.toInt() ?: 6379
    val redisHost = System.getenv("REDIS_HOST") ?: "127.0.0.1" // "redis-master.database"
    val redisAuth = System.getenv("REDIS_PASSWORD") ?: null
    val redisRecordsKey = System.getenv("REDIS_RECORDS_KEY") ?: "vert.x.ms" // the redis hash

    val serviceDiscoveryOptions = ServiceDiscoveryOptions()

    discovery = ServiceDiscovery.create(vertx,
      serviceDiscoveryOptions.setBackendConfiguration(
        json {
          obj(
            "host" to redisHost,
            "port" to redisPort,
            "auth" to redisAuth,
            "key" to redisRecordsKey
          )
        }
      ))

    // create the microservice record
    val serviceName = System.getenv("SERVICE_NAME") ?: "john-doe-service"
    val serviceHost = System.getenv("SERVICE_HOST") ?: "john-doe-service.127.0.0.1.nip.io"
    val serviceExternalPort= System.getenv("SERVICE_PORT")?.toInt() ?: 80

    record = HttpEndpoint.createRecord(
      serviceName,
      serviceHost, // or internal ip
      serviceExternalPort, // exposed port (internally it's 8080)
      "/api"
    )

    // --- adding some meta data ---
    record.metadata = json {
      obj(
        "api" to jsonArrayOf("/hello", "/knock-knock")
      )
    }

    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create())

    // use me with other microservices
    // ServiceDiscoveryRestEndpoint.create(router, discovery) // call /discovery route

    router.get("/api/hello").handler { context ->
      context.response().putHeader("content-type", "application/json;charset=UTF-8")
        .end(
          json { obj("message" to "👋 Hello World 🌍") }.encodePrettily()
        )
    }

    router.get("/api/knock-knock").handler { context ->
      println("👋 knock-knock")
      context.response().putHeader("content-type", "application/json;charset=UTF-8")
        .end(
          json {
            obj(
              "from" to serviceName,
              "message" to "🏓 pong"
            )
          }.encodePrettily()
        )
    }

    // serve static assets
    router.route("/*").handler(StaticHandler.create().setCachingEnabled(false))

    // internal port
    val httpPort = System.getenv("PORT")?.toInt() ?: 8080

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(httpPort) { http ->
        if (http.succeeded()) {
          println("🏓 Ping started on $httpPort")

          /* 👋 === publish the microservice record to the discovery backend === */
          discovery.publish(record) { asyncRes ->
            when {
              asyncRes.failed() -> {
                val errorMessage = "😡 Not able to publish the microservice: ${asyncRes.cause().message}"
                println(errorMessage)
                startPromise.fail(asyncRes.cause())
              } // ⬅️ failed

              asyncRes.succeeded() -> {
                val successMessage = "🎉😃 ${serviceName} is published! ${asyncRes.result().registration}"
                println(successMessage)
                println("🖐 $serviceName :")
                println(record.toJson().encodePrettily())
                startPromise.complete()
              } // ⬅️ succeed

            } // ⬅️ when
          } // ⬅️ publish

          println("🌍 HTTP server started on port ${httpPort}")
        } else {
          startPromise.fail(http.cause())
        }
      }
  }
}
