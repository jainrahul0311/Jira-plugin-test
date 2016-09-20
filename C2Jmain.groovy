import C2JCASTUtils
import C2JJIRAUtils

class C2JMain {

    static void main(def args) {
        // Actions list to push in JIRA
        def actions = []
        // Added Keys in JIRA (corresponding to violation already in project)
        def addedKeys = []
        // Added Summaries in JIRA (corresponding to violation already in project)
        def addedSummaries = []

        // Opening config file
        def config = new ConfigSlurper().parse(new File('C2Jconf.groovy').toURL())

        println "Config Found in 'C2Jconf.groovy' :"
        println "  CAST Web Service URL = " + config.CASTWebServiceURL
        println "  CAST Web Service Login = " + config.CASTWebServiceLogin
        println "  CAST Web Service Password = " + config.CASTWebServicePassword
        println "  CAST Domain Name = " + config.CASTDomainName
        println "  CAST Application Name = " + config.CASTApplicationName
        println "  JIRA Web Service URL = " + config.JIRAWebServiceURL
        println "  JIRA Web Service Login = " + config.JIRAWebServiceLogin
        println "  JIRA Web Service Password = " + config.JIRAWebServicePassword
        println "  JIRA Project Key = " + config.JIRAProjectKey
        println "  JIRA Issue Type = " + config.JIRAIssueType
        println "  JIRA Version Id = " + config.JIRAVersionId

        println ""
        println "-----"
        println "Get CAST Application Id from CAST Web Service"
        println "-----"

        // Get the application id of the application setted in the config file
        def applicationHRef = C2JCASTUtils.getCASTApplicationHRef(config)

        println ""
        println "-----"
        println "Get CAST Last Snapshot Id for Application href = " + applicationHRef + " from CAST Web Service"
        println "-----"

        // Get the application id of the application setted in the config file
        def snapshotHRef = C2JCASTUtils.getCASTLastSnapshotHRef(config, applicationHRef)
        
        println ""
        println "-----"
        println "Get CAST Action Plan for Snapshot href = " + snapshotHRef + " from CAST Web Service"
        println "-----"

        // Get the CAST action plan of the application setted in the config file
        C2JCASTUtils.getActionsAndObjectsInActionPlan(config, snapshotHRef, actions)
        

        println ""
        println "-----"
        println "GET JIRA Project Tickets list from JIRA Web Service"
        println "-----"

        // Get the ticket list of the project in JIRA setted in the config file
        C2JJIRAUtils.getAddedKeysAndSummaries(config, addedKeys, addedSummaries)

        println ""
        println "-----"
        println "Push CAST Action Plan in JIRA project issues list"
        println "-----"

        // POST CAST Action Plan to JIRA project issues list
        C2JJIRAUtils.pushActionPlan(config, snapshotHRef, actions, addedSummaries)

        println ""
        println "-----"
        println "End of the script"
        println "-----"

    }
}