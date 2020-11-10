package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class CreateGetDeleteScenario extends Simulation {

  //1. Http Conf
  val httpConf = http.baseUrl("http://localhost:8080")
    .header("Accept", "application/json")

  //2. Scenario

  val scn = scenario("Employee Create and Get Scenario")
    .exec(
      http("Post employee json").post("/employee")
        .body(StringBody("""{"fname": "John", "lname": "Doe", "email": "john@mycompany.com", "dob": "12-04-1987"}""")).asJson
        .check(status.is(201))
        .check(jsonPath("$.fname").is("John"))
        .check(jsonPath("$.id").saveAs("employeeId"))
    )
    .exec { session => println(session); session }
    .pause(2)
    .exec(
      http("Get employee by id").get("/employee/${employeeId}")
        .check(status.is(200))
        .check(jsonPath("$.fname").is("John"))
    )
    .pause(1)
    .exec(
      http("Delete employee by id").delete("/employee/${employeeId}")
        .check(status.is(200))
    )

  //3. Load
  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)

}
