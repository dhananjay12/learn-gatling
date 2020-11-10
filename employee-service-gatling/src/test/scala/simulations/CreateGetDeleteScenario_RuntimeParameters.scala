package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class CreateGetDeleteScenario_RuntimeParameters extends Simulation {

  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def userCount: Int = getProperty("USERS", "5").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "60").toInt

  before {
    println("Running test with ${userCount} users")
    println("Ramping users over ${rampDuration} seconds")
    println("Total test duration: ${testDuration} seconds")
  }


  //1. Http Conf
  val httpConf = http.baseUrl("http://localhost:8080")
    .header("Accept", "application/json")

  val csvFeeder = csv("data/employeeCreate.csv").queue

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
    scn.inject(
      nothingFor(5 seconds),
      rampUsers(userCount) during (rampDuration second)
    )
  ).protocols(httpConf)
    .maxDuration(testDuration seconds)
}
