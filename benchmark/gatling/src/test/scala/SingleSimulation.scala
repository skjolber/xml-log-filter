import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * This simulation load-tests a Mule Soap service.
 * 
 */

abstract class SingleSimulation(val body: String, val path: String) extends HttpProxySimulation {

    val baseName = path + "-" + body;
    val requestName = baseName + "-request";
    val noOfUsers = getNoOfUsers(body);
    val scenarioName = baseName + "-" + noOfUsers +"-scenario";

    val testScenario = scenario(scenarioName)
        .during(testTimeSecs) {
            exec(http(requestName)
                .post("/" + path)
                .body(RawFileBody(body + ".xml"))
                .header("Content-Type", "application/xml+soap")
                .check(status.is(expectedHttpStatus))
                 )

        }
    ;

    setUp(
        testScenario
            .inject(rampUsers(noOfUsers) over(rampUpTimeSecs seconds)))
            .protocols(httpConf)  
            
  
}

/*
class Stax01kSimulation extends SingleSimulation("1k", "stax") {

}

class Stax02kSimulation extends SingleSimulation("2k", "stax") {

}

class Stax06kSimulation extends SingleSimulation("6k", "stax") {

}

class Stax21kSimulation extends SingleSimulation("21k", "stax") {

}


class Xmlformatter01kSimulation extends SingleSimulation("1k", "xmlformatter") {

}

class Xmlformatter02kSimulation extends SingleSimulation("2k", "xmlformatter") {

}

class Xmlformatter06kSimulation extends SingleSimulation("6k", "xmlformatter") {

}

class Xmlformatter21kSimulation extends SingleSimulation("21k", "xmlformatter") {

}



class Xmlformatter01kSecureSimulation extends SingleSimulation("1k", "xmlformatter-secure") {

}

class Xmlformatter02kSecureSimulation extends SingleSimulation("2k", "xmlformatter-secure") {

}

class Xmlformatter06kSecureSimulation extends SingleSimulation("6k", "xmlformatter-secure") {

}

class Xmlformatter21kSecureSimulation extends SingleSimulation("21k", "xmlformatter-secure") {

}



class Transformer01kSimulation extends SingleSimulation("1k", "transformer") {

}

class Transformer02kSimulation extends SingleSimulation("2k", "transformer") {

}

class Transformer06kSimulation extends SingleSimulation("6k", "transformer") {

}

class Transformer21kSimulation extends SingleSimulation("21k", "transformer") {

}



class Vanilla01kSimulation extends SingleSimulation("1k", "vanilla") {

}

class Vanilla02kSimulation extends SingleSimulation("2k", "vanilla") {

}

class Vanilla06kSimulation extends SingleSimulation("6k", "vanilla") {

}

class Vanilla21kSimulation extends SingleSimulation("21k", "vanilla") {

}
*/
