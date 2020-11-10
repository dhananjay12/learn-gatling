import io.gatling.core.Predef._
import io.gatling.http.Predef._

class MyFirstTest extends Simulation {

  //1. Http Conf
  val httpConf = http.baseUrl("http://localhost:8080")
    .header("Accept", "application/json")

  //2. Scenario

  val scn = scenario("Employee Get test")
    .exec(http("Get employee by id")
      .get("/employee/1"))

  //3. Load

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)

}
