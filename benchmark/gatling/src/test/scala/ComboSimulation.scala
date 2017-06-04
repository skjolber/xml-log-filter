import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.request.builder.HttpRequestBuilder

/**
 * This simulation load-tests a Mule Soap service.
 * 
 */

abstract class ComboSimulation(val body: String) extends HttpProxySimulation {

    val noOfUsers = getNoOfUsers(body);

    val pathNone:HttpRequestBuilder = request("none-" + body, body, endpointPathNone);  
    val pathDefaultXmlFilter:HttpRequestBuilder = request("DefaultXmlFilter-" + body, body, endpointPathDefaultXmlFilter);  
    val pathSingleXPathAnonymizeStAXSoapHeaderXmlFilter:HttpRequestBuilder = request("SingleXPathAnonymizeStAXSoapHeaderXmlFilter-" + body, body, endpointPathSingleXPathAnonymizeStAXSoapHeaderXmlFilter);  
    val pathSingleXPathPruneStAXSoapHeaderXmlFilter:HttpRequestBuilder = request("SingleXPathPruneStAXSoapHeaderXmlFilter-" + body, body, endpointPathSingleXPathPruneStAXSoapHeaderXmlFilter);  
    val pathSingleXPathPruneSoapHeaderXmlFilter:HttpRequestBuilder = request("SingleXPathPruneSoapHeaderXmlFilter-" + body, body, endpointPathSingleXPathPruneSoapHeaderXmlFilter);  
    val pathSingleXPathAnonymizeSoapHeaderXmlFilter:HttpRequestBuilder = request("SingleXPathAnonymizeSoapHeaderXmlFilter-" + body, body, endpointPathSingleXPathAnonymizeSoapHeaderXmlFilter);  
    val pathSingleXPathPruneMaxNodeLengthStAXXmlFilter:HttpRequestBuilder = request("SingleXPathPruneMaxNodeLengthStAXXmlFilter-" + body, body, endpointPathSingleXPathPruneMaxNodeLengthStAXXmlFilter);  
    val pathSingleXPathAnonymizeMaxNodeLengthStAXXmlFilter:HttpRequestBuilder = request("SingleXPathAnonymizeMaxNodeLengthStAXXmlFilter-" + body, body, endpointPathSingleXPathAnonymizeMaxNodeLengthStAXXmlFilter);  
    val pathSingleXPathAnonymizeXmlFilter:HttpRequestBuilder = request("SingleXPathAnonymizeXmlFilter-" + body, body, endpointPathSingleXPathAnonymizeXmlFilter);  
    val pathSingleXPathPruneXmlFilter:HttpRequestBuilder = request("SingleXPathPruneXmlFilter-" + body, body, endpointPathSingleXPathPruneXmlFilter);  
    val pathMultiXPathXmlFilter:HttpRequestBuilder = request("MultiXPathXmlFilter-" + body, body, endpointPathMultiXPathXmlFilter);  
    val pathSingleXPathAnonymizeMaxNodeLengthXmlFilter:HttpRequestBuilder = request("SingleXPathAnonymizeMaxNodeLengthXmlFilter-" + body, body, endpointPathSingleXPathAnonymizeMaxNodeLengthXmlFilter);  
    val pathSingleXPathPruneMaxNodeLengthXmlFilter:HttpRequestBuilder = request("SingleXPathPruneMaxNodeLengthXmlFilter-" + body, body, endpointPathSingleXPathPruneMaxNodeLengthXmlFilter);  
    val pathMultiXPathMaxNodeLengthXmlFilter:HttpRequestBuilder = request("MultiXPathMaxNodeLengthXmlFilter-" + body, body, endpointPathMultiXPathMaxNodeLengthXmlFilter);  
    val pathW3cDomXPathXmlIndentationFilter:HttpRequestBuilder = request("W3cDomXPathXmlIndentationFilter-" + body, body, endpointPathW3cDomXPathXmlIndentationFilter);      
    
    
    
    val allScenarios = scenario("scenario-" + body + "-" + noOfUsers + "-users")
        .repeat((warmupRequests + noOfUsers) / noOfUsers) {
            //exec(request("warmup-" + body, body, endpointPathNone))
            exec(request("warmup-" + body, body, endpointPathDefaultXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathAnonymizeStAXSoapHeaderXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathPruneStAXSoapHeaderXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathPruneSoapHeaderXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathAnonymizeSoapHeaderXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathPruneMaxNodeLengthStAXXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathAnonymizeMaxNodeLengthStAXXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathAnonymizeXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathPruneXmlFilter))
            exec(request("warmup-" + body, body, endpointPathMultiXPathXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathAnonymizeMaxNodeLengthXmlFilter))
            exec(request("warmup-" + body, body, endpointPathSingleXPathPruneMaxNodeLengthXmlFilter))
            exec(request("warmup-" + body, body, endpointPathMultiXPathMaxNodeLengthXmlFilter))
            //exec(request("warmup-" + body, body, endpointPathW3cDomXPathXmlIndentationFilter))
        }
        .pause(pauseTimeSecs)
/*        
        .during(testTimeSecs) {
            exec(pathNone)              
        }
        */
        .during(testTimeSecs) {
            exec(pathDefaultXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathAnonymizeStAXSoapHeaderXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathPruneStAXSoapHeaderXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathPruneSoapHeaderXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathAnonymizeSoapHeaderXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathPruneMaxNodeLengthStAXXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathAnonymizeMaxNodeLengthStAXXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathAnonymizeXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathPruneXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathMultiXPathXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathAnonymizeMaxNodeLengthXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathSingleXPathPruneMaxNodeLengthXmlFilter)              
        }
        .during(testTimeSecs) {
            exec(pathMultiXPathMaxNodeLengthXmlFilter)              
        }
        /*
        .during(testTimeSecs) {
            exec(pathW3cDomXPathXmlIndentationFilter)              
        }
        */
    ; 

    setUp(
        allScenarios
            .inject(rampUsers(noOfUsers) over(rampUpTimeSecs seconds)))
            .protocols(httpConf)  

            
}


class Combo01kSimulation extends ComboSimulation("1k") {
  
}

class Combo02kSimulation extends ComboSimulation("2k") {

}

class Combo06kSimulation extends ComboSimulation("6k") {
  
}

class Combo21kSimulation extends ComboSimulation("21k") {
  
}

