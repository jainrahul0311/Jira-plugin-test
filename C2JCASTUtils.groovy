@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class C2JCASTUtils {

    // Build Auth to CAST Web Service
    static buildCASTAuth(def config) {
        config.CASTWebServiceLogin + ":" + config.CASTWebServicePassword
    }
    
    // Perform a GET request on CAST REST Web Service to get the applications of the Web Service, expecting JSON response data
    static getCASTApplicationHRef(def config) {
        def value
        def http = new HTTPBuilder()
        def auth = buildCASTAuth(config)
        def url = config.CASTWebServiceURL + "/rest/" + config.CASTDomainName + "/applications"
        println "url sent to CAST Rest API for applications : " + url

        http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
        http.request(url, GET, JSON) {
            // response handler for a success response code
            response.success = { resp, json ->
                println resp.statusLine
                // resp.headers.each { h -> println " ${h.name} : ${h.value}" }
                // iterate over JSON objects in the response:
                println json
                json.each {
                    println it
                    if(it.name == config.CASTApplicationName) {
                        value = it.href
                    }
                }
            }
            // handler for any failure status code:
            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }
        value
    }
    
    // Perform a GET request on CAST REST Web Service to get the snapshots of the Web Service, expecting JSON response data
    static getCASTLastSnapshotHRef(def config, def applicationHRef) {
        def value
        def http = new HTTPBuilder()
        def auth = buildCASTAuth(config)
        def url = config.CASTWebServiceURL + "/rest/" + applicationHRef + "/snapshots"
        println "Request sent to the CAST Rest API for snapshots : " + url
        http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
        http.request(url, GET, JSON) {
            // response handler for a success response code
            response.success = { resp, json ->
                println resp.statusLine
                // resp.headers.each { h -> println " ${h.name} : ${h.value}" }
                println json
                value = json[0].href
            }
            // handler for any failure status code:
            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }
        value
    }

    // Perform a GET request on CAST REST Web Service to get the Action Plan, expecting JSON response data
    static getActionsAndObjectsInActionPlan(def config,  def snapshotHRef, def actions) {
        def http = new HTTPBuilder()
        def auth = buildCASTAuth(config)
        def url = config.CASTWebServiceURL + "/rest/" +snapshotHRef + "/action-plan/issues?nbRows=10000"
        println "Request sent to the CAST Rest API for action plan : " + url
        http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
        
        http.request(url, GET, JSON) {
            // response handler for a success response code
            response.success = { resp, json ->
                println resp.statusLine
                //resp.headers.each { h -> println " ${h.name} : ${h.value}" }
                // println json
               
                // iterate over JSON 'actions' object in the response:
                json.each { 
                     if(it.remedialAction.status != "solved") {
                        actions.add(it)
                    }
                }
                println actions

            }
            // handler for any failure status code:
            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }
    }

    // Perform a GET request on CAST REST Web Service to get the Action Plan, expecting JSON response data
    static getQualityRuleDetails(def config, def ruleHRef) {
        def value
        def auth = buildCASTAuth(config)
        def url = config.CASTWebServiceURL + "/rest/" + ruleHRef
        def http = new HTTPBuilder()
        http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
        http.request(url, GET, JSON) {
            // response handler for a success response code
            response.success = { resp, json ->
                println " getQualityRuleDetails " + resp.statusLine
                //resp.headers.each { h -> println " ${h.name} : ${h.value}" }
                value = json
            }
            // handler for any failure status code:
            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }
        value
    }

    // Perform a GET request on CAST REST Web Service to get the Action Plan, expecting JSON response data
    static getComponentsDetails(def config, def componentHRef) {
        def value
        def auth = buildCASTAuth(config)
        def url = config.CASTWebServiceURL + "/rest/" + componentHRef
        def http = new HTTPBuilder()
        http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
        http.request(url, GET, JSON) {
            // response handler for a success response code
            response.success = { resp, json ->
                println " getComponentsDetails " + resp.statusLine
                //resp.headers.each { h -> println " ${h.name} : ${h.value}" }
                value = json
            }
            // handler for any failure status code:
            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }
        value
    }

}