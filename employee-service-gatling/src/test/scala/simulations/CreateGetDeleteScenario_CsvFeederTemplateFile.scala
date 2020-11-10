package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class CreateGetDeleteScenario_CsvFeederTemplateFile extends Simulation {

  //1. Http Conf
  val httpConf = http.baseUrl("http://localhost:8080")
    .header("Accept", "application/json")

  val csvFeeder = csv("data/employeeCreate.csv").queue

  //2. Scenario

  val scn = scenario("Employee Create and Get Scenario")
    .repeat(10) {
      feed(csvFeeder)
        .exec(
          http("Post employee json").post("/employee")
            .body(ElFileBody("bodies/createEmployeeTemplate.json")).asJson
            .check(status.is(201))
            .check(jsonPath("$.fname").is("${fname}"))
            .check(jsonPath("$.id").saveAs("employeeId"))
        )
        .exec { session => println(session); session }
        .pause(2)
        .exec(
          http("Get employee by id").get("/employee/${employeeId}")
            .check(status.is(200))
            .check(jsonPath("$.fname").is("${fname}"))
        )
        .pause(1)
        .exec(
          http("Delete employee by id").delete("/employee/${employeeId}")
            .check(status.is(200))
        )

    }

  //3. Load
  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
