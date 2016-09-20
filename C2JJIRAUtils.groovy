@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import C2JCASTUtils

class C2JJIRAUtils {
    
    // Perform a GET request on JIRA REST Web Service to get all the tickets added previously, expecting JSON response data
    static getAddedKeysAndSummaries(def config, def addedKeys, def addedSummaries) {
        def http = new HTTPBuilder()
        def auth = buildJIRAAuth(config)
        http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
        def url = config.JIRAWebServiceURL + "/rest/api/2/search?jql=project=" + config.JIRAProjectKey
        http.request(url, GET, JSON) {
            // response handler for a success response code
            response.success = { resp, json ->
                println resp.statusLine
                // resp.headers.each { h -> println " ${h.name} : ${h.value}" }
                // iterate over JSON 'issues' object in the response:
                json.issues.each { issue ->
                    addedKeys.add(issue.key)
                    addedSummaries.add(issue.fields.summary)
                }
            }
            // handler for any failure status code:
            response.failure = { resp ->
                println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
            }
        }
        println "addedKeys : " + addedKeys
        println "addedSummaries : " + addedSummaries
    }

    // Perform a GET request on JIRA REST Web Service to get all projects, expecting JSON response data
    static getProjects(def config, def addedKeys, def addedSummaries) {
        def http = new HTTPBuilder(config.JIRAWebServiceURL + "/rest/api/2/")
        def auth = buildJIRAAuth(config)
        http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
        def projects = http.get(path: 'project')
        projects
    }

    // Perform a POST request on JIRA REST Web Service for each action not already in JIRA issues list
    static pushActionPlan(def config, def snapshotHRef, def actions, def addedSummaries) {
        actions.each { action ->
            def qualityRuleDetail = C2JCASTUtils.getQualityRuleDetails(config, action.rulePattern.href)
            def key = buildSummary(action, qualityRuleDetail)
            println "key : " + key
            if(!addedSummaries.contains(key)) {
                // perform a POST request, expecting JSON response data
                def http = new HTTPBuilder()
                def auth = buildJIRAAuth(config)
                def url = config.JIRAWebServiceURL + "/rest/api/2/issue"
                http.headers["Authorization"] = "Basic " + auth.getBytes("iso-8859-1").encodeBase64()
                http.request(url, POST, JSON) {
                    // set the body of json data to push
                    def objectDetail = C2JCASTUtils.getComponentsDetails(config,action.component.treeNodes.href)
                    setBody(buildData(config, snapshotHRef, action, qualityRuleDetail, objectDetail))
                    // response handler for a success response code
                    response.success = { resp, json ->
                        println "Pushing ${action.component.href}.${action.rulePattern.href} from CAST to JIRA : OK"
                    }
                    // handler for any failure status code:
                    response.failure = { resp ->
                        println "Pushing ${action.component.href}.${action.rulePattern.href} from CAST to JIRA : KO"
                        println "   Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
                    }
                }
            } else {
                println "Pushing ${action.component.href}.${action.rulePattern.href} from CAST to JIRA : SKIPED ALREADY EXIST"
            }
        }
    }

    // Build summary of bug ticket
    static buildSummary(def action, def rule) {
        def technolist = ""
        rule.technologies.each { technolist = technolist + it + " " }
        "ACTION PLAN : [" + technolist + "] (" + rule.name + ") " + action.component.name
    }

    // Build Auth to JIRA Web Service
    static buildJIRAAuth(def config) {
        config.JIRAWebServiceLogin + ":" + config.JIRAWebServicePassword
    }

    static convertPriority = ["extreme" : "1", "high" : "2", "moderate" : "3", "low" : "4", "(keep)" : "5"]

    static buildData(def config, def snapshotHRef, def action, def qualityRuleDetail, def objectDetail) {
        def data = [:]
        def fields = [:]

        def project = [:]
        project.put("key", config.JIRAProjectKey)
        fields.put("project", project)
        fields.put("summary", buildSummary(action, qualityRuleDetail))

        def ruleId = action.rulePattern.href.toString().split('\\/')[2]
        def componentId = objectDetail.href.toString().split('\\/')[2]
        def objId = action.component.href.toString().split('\\/')[2]
        def description = "Violation status : " + action.remedialAction.status + "\n\n"
		// Following 2 lines can be added to add links to CAST AED portal
        // description += "Rule information : " + config.CASTWebServiceURL + "/engineering/index.html#" + snapshotHRef + "/business/60017/qualityInvestigation/60017/all/" + ruleId + "\n\n"
        // description += "Object information : " + config.CASTWebServiceURL + "/engineering/index.html#" + snapshotHRef + "/business/60017/componentsInvestigation/" +componentId  + "/" + ruleId + "/" + objId
        fields.put("description", description)
        
        def issuetype = [:]
        issuetype.put("name", config.JIRAIssueType)
        fields.put("issuetype", issuetype)

        def priority = [:]
        priority.put("id", convertPriority[action.remedialAction.priority])
        fields.put("priority", priority)

        def versions = []
        def version = [:]
        version.put("id",config.JIRAVersionId)
        versions.add(version)
        fields.put("versions", versions)
        
        data.put("fields", fields)
        data
    }

}