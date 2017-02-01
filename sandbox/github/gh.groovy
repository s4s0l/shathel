#!/usr/bin/env groovy
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
@Grab('org.apache.httpcomponents:httpmime:4.2.1')
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import groovyx.net.http.Status
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody

import static groovyx.net.http.ContentType.JSON

def gh = new RESTClient("https://api.github.com")
gh.handler[Status.FAILURE] = gh.handler.get(Status.SUCCESS)
HttpResponseDecorator result = gh.get(
        path: '/repos/s4s0l/shathel/releases/tags/0.0.3',
//        path: '/user',

        headers: ['Authorization': 'token ',
                  'User-Agent'   : 's4s0l release enricher']
)

println new groovy.json.JsonBuilder(result.data).toPrettyString()
//5315591

def uploadUrl = result.data.upload_url

def url = result.data.url
gh.getEncoder().putAt("application/zip", gh.getEncoder().&encodeStream);
gh.ignoreSSLIssues()
def asset = '../../build/localrepo/org/s4s0l/shathel/gradle/sample2/simple-project2/DEVELOPER-SNAPSHOT/simple-project2-DEVELOPER-SNAPSHOT-shathel.zip'
result = gh.post(
        uri: uploadUrl - "{?name,label}",
//        path: '/user',
        query: [
                name:"sampleAssset",
                label:"xxxx"
        ],
        body: new File(asset).bytes,
        requestContentType: 'application/zip',
        headers: ['Authorization': 'token ',
                  'User-Agent'   : 's4s0l release enricher']
)

println new groovy.json.JsonBuilder(result.data).toPrettyString()