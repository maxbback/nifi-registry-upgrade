import groovy.json.*

import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*

// Nifi-registry flow file name
registryFlowName = "Masking Addresses From A Lookup Table".toLowerCase()
// taret version for upgrade if 0 upgrade to latest version
targetVersion = 0

// global variables
//-----------------
// Nifi API url
url_nifi = 'http://localhost:8080/nifi-api'
// Nifi registry api url
url_nifi_registry = 'http://localhost:18080/nifi-registry-api'
// Url to get all process groups in Nifi, recursive is enabled to catch all
urlProcessGroups = url_nifi + '/flow/process-groups/root/status?recursive=true'

// http client
client = HttpClientBuilder.create().build()

// Function for executing  get request and return a groovy map
def getUrl(url) {
	def get = new HttpGet(url)
	def response = client.execute(get)

	def bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
	def jsonResponse = bufferedReader.getText()
	//println "response: \n" + jsonResponse

	def slurper = new JsonSlurper()
	def resultMap = slurper.parseText(jsonResponse)
	return resultMap
}

// Function for executing a post request and return a result map
def postUrl(url,map)  {
	def jsonBody = new JsonBuilder(map).toString()
	def post = new HttpPost(url)
	post.addHeader("content-type", "application/json")
	post.setEntity(new StringEntity(jsonBody))
	def response = client.execute(post)

	def bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
	def jsonResponse = bufferedReader.getText()
	//println "response: \n" + jsonResponse

	def slurper = new JsonSlurper()
	def resultMap = slurper.parseText(jsonResponse)
	return resultMap
}

// Get Latest version of a nifi-registry flow
def getLatestVersion (bucketid,flowid) {
	def url = url_nifi_registry + '/buckets/' + bucketid + '/flows/' + flowid + '/versions/latest'
	def resultMap = getUrl(url)
	return resultMap.snapshotMetadata.version
}


// build JSON
/*
def map = [:]
map["name"] = "Maritime DevCon"
map["address"] = "Fredericton"
map["handle"] = "maritimedevcon"

def jsonBody = new JsonBuilder(map).toString()
*/

// get all process groups
def resultMap = getUrl(urlProcessGroups)
// Lets loop over all procss groups in NiFi
resultMap.processGroupStatus.aggregateSnapshot.processGroupStatusSnapshots.each {
	// Collect version information for each process group
	url=url_nifi + "/versions/process-groups/" + it.id
	def versionMap = getUrl(url)
	// If process group is not under versioining ignore it
	// Check if process group is using the registry flow we are upgrading
	if (versionMap.versionControlInformation  && registryFlowName == versionMap.versionControlInformation.flowName.toLowerCase()) {
		println "Found a registry to upgrade" + registryFlowName
		// If targetVersion is 0 (get latest version) and versionedFlowState is sourceTable
		// wich indicates that it is not latest in use
		// we collect latest version number for nifi-registry and set it as targetVersion
		// This code updates the global targetVersion variables so it is only executed one time
		if (targetVersion == 0 && it.processGroupStatusSnapshot.versionedFlowState == "STALE") {
				println "STALE version"
				targetVersion = getLatestVersion(versionMap.versionControlInformation.bucketId,versionMap.versionControlInformation.flowId)
			}

			// Check if we shall upgrade the process group
			if (targetVersion != 0 && versionMap.versionControlInformation.version != targetVersion) {
				versionMap.versionControlInformation.version = targetVersion
				println "Upgrade "
				def url = url_nifi + "/versions/update-requests/process-groups/" + it.id
				resultMap = postUrl(url,versionMap)
			}

	}


}
