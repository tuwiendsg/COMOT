#!/usr/bin/python

import sys, httplib, uuid, random,time, resource
from threading import Thread
from urllib3 import HTTPConnectionPool
from multiprocessing import Process


httplib.HTTPConnection.debuglevel = 0

KeyspaceName = 'm2m'
tablename = 'sensor'

HAProxyIP="10.99.0.61"
HAProxyPort='8080'
BaseURL = HAProxyIP+':'+HAProxyPort

minOperations = 100
maxOperations = 300
changeInterval = 1
step = 1

generatedKeys = []
generatedTables = []

rowOps  = 30


def executeRESTCall(restMethod, serviceBaseURL, resourceName,  content):
        connection =  httplib.HTTPConnection(serviceBaseURL)

        headers={
                'Content-Type':'application/xml; charset=utf-8'#,
        }

        try:
          connection.request(restMethod, '/'+resourceName, body=content,headers=headers,)
          result = connection.getresponse()
          connection.close()
        except Exception,e:
          print e
          time.sleep(1) 
def _writeManyInParralel(writes):
            #print str(writes) + " writes "
            generatedProcesses = []
            for i in range(0,writes):
              p = Thread(target=_issueWriteRequest)
              generatedProcesses.append(p)
              p.start()
              time.sleep(1.0/writes)
            #time.sleep(1)
            #for p in generatedProcesses:
            #  p.join()

def _readManyInParralel(reads):
            generatedProcesses = []
            for i in range(0,reads):
              if len(generatedKeys) > 1:
                      p = Process(target=_issueReadRequest)
                      generatedProcesses.append(p)
                      p.start()
            time.sleep(1)
            for p in generatedProcesses:
              p.join()

def _issueWriteRequest():
            key = str(uuid.uuid1())
            table=tablename
            generatedKeys.append(key)
            rowsToCreate = rowOps
            #print str(rowsToCreate) 
            createRowStatement='<CreateRowsStatement><Table name="'+table+'"><Keyspace name="' + KeyspaceName + '"/></Table>'
            for i in range(0, int(rowsToCreate)):
              key = str(uuid.uuid1())
              generatedKeys.append(key)
              createRowStatement=createRowStatement+('<Row><Column name="key" value="%s"/><Column name="sensorName" value="SensorY"/><Column name="sensorValue" value="%s"/> </Row>' % (key,random.uniform(1, 20000)))

            createRowStatement=createRowStatement + '</CreateRowsStatement>'
            executeRESTCall('PUT', BaseURL, 'DaaS/api/xml/table/row', createRowStatement)

def _issueReadRequest():
            rowsToDelete = rowOps
            keysToDelete = generatedKeys.pop(random.randint(0,len(generatedKeys)-1));
            for i in range(0, int (rowsToDelete)):
               if len(generatedKeys) > 0:
                   keysToDelete = keysToDelete + ',' + generatedKeys.pop(random.randint(0,len(generatedKeys)-1))

            table = tablename
            deleteRowQuerry = '<Query><Table name="'+table+'"><Keyspace name="' + KeyspaceName + '"/></Table><Condition>key in ('+keysToDelete+')</Condition></Query>'
            executeRESTCall('DELETE', BaseURL, 'DaaS/api/xml/table/row', deleteRowQuerry)

 

if __name__=='__main__':
       
       args = sys.argv;
       if (len(args) > 1):  HAProxyIP= str(args[1])
       BaseURL = HAProxyIP+':'+HAProxyPort
       if (len(args) > 2):  minOperations = int(args[2])
       if (len(args) > 3):  maxOperations = int(args[3])
       if (len(args) > 4):  changeInterval = int(args[4])
       if (len(args) > 5):  step = int(args[5])

       #print "Running " + str(minOperations) +" at " +  str(rowOps) + " rows per op to " + str(BaseURL)
       #_initiateDB()
       stepIndex = 0
       changeAmount=step
       opCount = minOperations
       while True:
         print "Executing " + str(opCount) + " calls"
         stepIndex = stepIndex + 1
         if (stepIndex == changeInterval):
            stepIndex = 0
            opCount = opCount + changeAmount

         if (opCount == maxOperations and changeAmount>0):
           changeAmount = -1 * step
         elif (opCount == minOperations and changeAmount<0):
           changeAmount = step
         try:
           _writeManyInParralel(opCount)
         except Exception, e:
            time.sleep(1)





                                                         
