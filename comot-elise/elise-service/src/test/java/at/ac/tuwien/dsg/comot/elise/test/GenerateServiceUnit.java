/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.test;

import at.ac.tuwien.dsg.comot.model.elasticunit.executionmodels.DockerExecution;
import at.ac.tuwien.dsg.comot.model.elasticunit.executionmodels.ScriptExecution;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.Artifact;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.Feature;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.elasticunit.provider.Provider;
import at.ac.tuwien.dsg.comot.model.type.ServiceCategory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author hungld
 */
public class GenerateServiceUnit {

    public static void main(String[] args) throws IOException {
        OfferedServiceUnit offerSensorGovOps = new OfferedServiceUnit("sensorManagedByGovOps", ServiceCategory.SENSOR, "hung");
//        Feature sensorFeature = new Feature("gateway-connection"); // only at runtime
//        offerSensorGovOps.hasFeature(sensorFeature);
        
        offerSensorGovOps.hasPrimitiveOperation(new PrimitiveOperation("deploy", PrimitiveOperation.ExecutionMethod.Script, new ScriptExecution("deploy.sh", "./", "")));
        offerSensorGovOps.hasPrimitiveOperation(new PrimitiveOperation("deployForGovOps", PrimitiveOperation.ExecutionMethod.Dockerfile, new DockerExecution("80")));
        offerSensorGovOps.hasPrimitiveOperation(new PrimitiveOperation("changeRate", PrimitiveOperation.ExecutionMethod.Script, new ScriptExecution("cChangeSensorRate.sh {rate}", "./", null)).hasParameters("rate"));
        offerSensorGovOps.hasPrimitiveOperation(new PrimitiveOperation("changeProtocol", PrimitiveOperation.ExecutionMethod.Script, new ScriptExecution("cChangeProto.sh {protocol}", "./", null)).hasParameters("protcol"));

        offerSensorGovOps.hasArtifact(new Artifact("deploy.sh", Artifact.ArtifactType.sh, "http://128.130.172.215/salsa/upload/files/rtGovOps/deploySensorUnit.sh"));
        offerSensorGovOps.hasArtifact(new Artifact("start.sh", Artifact.ArtifactType.sh, "http://128.130.172.215/salsa/upload/files/rtGovOps/starter_ubuntu.sh"));
        offerSensorGovOps.hasArtifact(new Artifact("decommission.sh", Artifact.ArtifactType.sh, "http://128.130.172.215/salsa/upload/files/rtGovOps/decommission"));
        offerSensorGovOps.hasArtifact(new Artifact("DockerFile", Artifact.ArtifactType.Dockerfile, "http://128.130.172.215/salsa/upload/files/rtGovOps/Dockerfile-UB"));
        offerSensorGovOps.hasArtifact(new Artifact("agent", Artifact.ArtifactType.misc, "http://128.130.172.215/salsa/upload/files/rtGovOps/rtGovOps-agents.tar.gz"));
        offerSensorGovOps.hasArtifact(new Artifact("sensor", Artifact.ArtifactType.misc, "http://128.130.172.215/salsa/upload/files/rtGovOps/sensor.tar.gz"));
        
        ObjectMapper mapper = new ObjectMapper();
        //System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerSensorGovOps));
        
        
        OfferedServiceUnit offerSensorGas = new OfferedServiceUnit("sensorGas", ServiceCategory.SENSOR, "hung");
        offerSensorGas.hasPrimitiveOperation(new PrimitiveOperation("deploy", PrimitiveOperation.ExecutionMethod.Script, new ScriptExecution("run_sensor_has.sh", "./", "")));
        offerSensorGas.hasArtifact(new Artifact("run_sensor_gas.sh", Artifact.ArtifactType.sh, "http://128.130.172.215/salsa/upload/files/IoTDaaSDemo/run_sensor_gas.sh"));
                
        //System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerSensorGas));
        
        OfferedServiceUnit offerSensorGPS = new OfferedServiceUnit("sensorGPS", ServiceCategory.SENSOR, "hung");
        offerSensorGPS.hasPrimitiveOperation(new PrimitiveOperation("deploy", PrimitiveOperation.ExecutionMethod.Script, new ScriptExecution("run_sensor_has.sh", "./", "")));
        offerSensorGas.hasArtifact(new Artifact("run_sensor_gas.sh", Artifact.ArtifactType.sh, "http://128.130.172.215/salsa/upload/files/IoTDaaSDemo/run_sensor_gps.sh"));
        //System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(offerSensorGPS));
        
        
        List<OfferedServiceUnit> listOffering = new ArrayList<>();
        listOffering.add(offerSensorGovOps);
        listOffering.add(offerSensorGas);
        listOffering.add(offerSensorGPS);
        
        
        Provider provider = new Provider("COMOT_dev", Provider.ProviderType.CUSTOM);
        provider.hasOfferedServiceUnit(offerSensorGovOps);
        provider.hasOfferedServiceUnit(offerSensorGas);
        provider.hasOfferedServiceUnit(offerSensorGPS);
        
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(provider));
    }
}
