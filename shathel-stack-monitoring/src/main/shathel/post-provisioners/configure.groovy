#!/usr/bin/env groovy
import static groovyx.net.http.ContentType.JSON
import groovyx.net.http.HttpResponseDecorator

import groovyx.net.http.RESTClient
import groovyx.net.http.Status
import org.apache.commons.lang.StringUtils
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody



//@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
//@Grab('org.apache.httpcomponents:httpmime:4.2.1')
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.scripts.HttpApis

ExecutableApiFacade apii = api;
Map<String,String> envs = env
HttpApis httpApi = http
String ip = apii.getIpForManagementNode();
int portainerPort = 3000
def address = "http://${ip}:${portainerPort}"
def grafana = httpApi.waitAndGetClient(address,[401,403],"/api/datasources")


def getClient(address){
    def ret = new RESTClient(address)
    ret.handler['401'] = ret.handler.get(Status.SUCCESS)
    ret.handler['404'] = ret.handler.get(Status.SUCCESS)
    ret
}
//envs = [:]
//grafana  = getClient("http://localhost:3000")


def token = Base64.getEncoder().encodeToString(("admin:adminadmin").bytes)

HttpResponseDecorator result = grafana.get([
        requestContentType: JSON,
        headers           : [Authorization: "Basic $token"],
        path              : '/api/datasources'
])
assert result.status == 200
def initialized = result.data.collect {it.name} . contains("Prometheus")

if(!initialized){
    result = grafana.post([
            requestContentType: JSON,
            headers           : [Authorization: "Basic $token"],
            path              : '/api/datasources',
            body: [
                    name:"Prometheus",
                    type:"prometheus",
                    url:"http://prometheus:9090${envs.getOrDefault("PROMETHEUS_CONTEXT_PATH", "/")}".toString(),
                    access:"proxy",
                    isDefault:true
            ]
    ])
    assert result.status == 200
    println("Grafana Initialized")
}else{
    println("Grafana Already initialized")
}

