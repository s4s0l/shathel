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
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.scripts.HttpApis

EnvironmentContext environmentContext = context;
ExecutableApiFacade api = env;
StackCommand stackCommand = command;
HttpApis httpApi = http
String ip = api.getIpForManagementNode();
int portainerPort = 3000
def address = "http://${ip}:${portainerPort}"
def grafana = httpApi.waitAndGetClient(address,[401,403],"/api/datasources")


def getClient(address){
    def ret = new RESTClient(address)
    ret.handler['401'] = ret.handler.get(Status.SUCCESS)
    ret.handler['404'] = ret.handler.get(Status.SUCCESS)
    ret
}

//grafana  = getClient("http://111.111.111.99:3000")


def token = Base64.getEncoder().encodeToString(("admin:adminadmin").bytes)

HttpResponseDecorator result = grafana.get([
        requestContentType: JSON,
        headers           : [Authorization: "Basic $token"],
        path              : '/api/datasources'
])

def initialized = result.data.collect {it.name} . contains("Prometheus")

if(!initialized){
    result = grafana.post([
            requestContentType: JSON,
            headers           : [Authorization: "Basic $token"],
            path              : '/api/datasources',
            body: [
                    name:"Prometheus",
                    type:"prometheus",
                    url:"http://prometheus:9090/prometheus",
                    access:"proxy",
                    isDefault:true
            ]
    ])
    println("Grafana Initialized")
}else{
    println("Grafana Already initialized")
}

