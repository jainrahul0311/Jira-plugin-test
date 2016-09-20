# Jira-plugin
Create jira issue based on CAST Rest API on Action Plan 

4 groovy files :
C2Jconf.groovy : contains the properties for CAST-RESTAPI and JIRA connections and information needed for requesting action plans and creating issues
C2Jmain.groovy : contains the steps followed by the process to get action plan and create bugs in jira
C2JCASTUtils.groovy : contains the methods that get data from the CAST-RESTAPI
C2JJIRAUtils.groovy : contains the methods that get issues from jira and push actions into bugs

To run the script : 
- Update C2Jconf.groovy with your own parameters
- Run C2Jmain.groovy script
