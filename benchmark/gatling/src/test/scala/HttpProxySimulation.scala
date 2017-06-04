import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.request.builder.HttpRequestBuilder

/**
 * This class defines the basic properties of a Gatling simulation used to load-test a HTTP proxy.
 * 
 */
abstract class HttpProxySimulation extends Simulation {
  
    val endpointPathNone = "/none"
    val endpointPathDefaultXmlFilter = "/DefaultXmlFilter"
    val endpointPathSingleXPathAnonymizeStAXSoapHeaderXmlFilter = "/SingleXPathAnonymizeStAXSoapHeaderXmlFilter"
    val endpointPathSingleXPathPruneStAXSoapHeaderXmlFilter = "/SingleXPathPruneStAXSoapHeaderXmlFilter"
    val endpointPathSingleXPathPruneSoapHeaderXmlFilter = "/SingleXPathPruneSoapHeaderXmlFilter"
    val endpointPathSingleXPathAnonymizeSoapHeaderXmlFilter = "/SingleXPathAnonymizeSoapHeaderXmlFilter"
    val endpointPathSingleXPathPruneMaxNodeLengthStAXXmlFilter = "/SingleXPathPruneMaxNodeLengthStAXXmlFilter"
    val endpointPathSingleXPathAnonymizeMaxNodeLengthStAXXmlFilter = "/SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter"
    val endpointPathSingleXPathAnonymizeXmlFilter = "/SingleXPathAnonymizeXmlFilter"
    val endpointPathSingleXPathPruneXmlFilter = "/SingleXPathPruneXmlFilter"
    val endpointPathMultiXPathXmlFilter = "/MultiXPathXmlFilter"
    val endpointPathSingleXPathAnonymizeMaxNodeLengthXmlFilter = "/SingleXPathAnonymizeMaxNodeLengthXmlFilter"
    val endpointPathSingleXPathPruneMaxNodeLengthXmlFilter = "/SingleXPathPruneMaxNodeLengthXmlFilter"
    val endpointPathMultiXPathMaxNodeLengthXmlFilter = "/MultiXPathMaxNodeLengthXmlFilter"
    val endpointPathW3cDomXPathXmlIndentationFilter = "/W3cDomXPathXmlIndentationFilter"
    
    /* Simulation timing and load parameters. */
    val testTimeSecs = 20
    val warmupRequests = 15000

    val rampUpTimeSecs:Int = 5
    val pauseTimeSecs:Int = 15

    // http://localhost:${server.port}/${base.path}/${service.path}
    //val baseURL = "http://localhost:8080/services/logger";
    val baseURL = "http://localhost:8080/servlet";

    /* Expected response HTTP status. */
    val expectedHttpStatus = 200;
    
    val httpConf = http
        .baseURL(baseURL)
        .acceptHeader("application/xml+soap")
        .header("Content-Encoding", "UTF-8")
        .userAgentHeader("Gatling");
    
    def getNoOfUsers(payload: String) : Int = {
      
       if (payload == "1k") {
          return 100;
       } else if (payload == "2k") {
          return 150;
       } else if (payload == "6k") {
          return 200;
       } else if (payload == "21k") {
          return 70;
       }
       throw new IllegalArgumentException("Unknown payload body " + payload);
    };
    
    def request(requestName:String, body:String, path:String) : HttpRequestBuilder = {
      return http(requestName)
                .post(path)
                .body(RawFileBody(body + ".xml"))
                .header("Content-Type", "application/xml+soap")
                .check(status.is(expectedHttpStatus));
    }
    

}

