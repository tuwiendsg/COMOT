/**
 * Copyright 2013 Technische Universitaet Wien (TUW), Distributed Systems Group
 * E184
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package performance;


/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
public class PerformanceEvalOnline {

//    public static void main(String[] args) throws JAXBException, FileNotFoundException, IOException {
//        AbstractDataAccess dataAccess;
////        DataAggregationEngine instantMonitoringDataEnrichmentEngine;
//        InstantMonitoringDataAnalysisEngine instantMonitoringDataAnalysisEngine;
//        ElasticitySpaceFunction elasticitySpaceFunction;
//        AggregatedMonitoringDataSQLAccess aggregatedMonitoringDataSQLAccess;
//
//        dataAccess = DataAccessWithAutoStructureDetection.createInstance();
//
////        instantMonitoringDataEnrichmentEngine = new DataAggregationEngine();
//        instantMonitoringDataAnalysisEngine = new InstantMonitoringDataAnalysisEngine();
//
//        aggregatedMonitoringDataSQLAccess = new AggregatedMonitoringDataSQLAccess("mela", "mela");
//
//
//        //read service configuration, metrics composition and requiremets from XML
//
//        MonitoredElement serviceStructure = null;
//        CompositionRulesConfiguration compositionRulesConfiguration = null;
//        Requirements requirements = null;
//
//        //read service structure
//        {
//            InputStream is = new FileInputStream("/home/daniel-tuwien/Documents/CELAR_GIT/multilevel-metrics-evaluation/MELA-Core/MELA-AnalysisService/src/test/java/testFiles/serviceDescription.xml");
//            JAXBContext jc = JAXBContext.newInstance(MonitoredElement.class);
//            Unmarshaller u = jc.createUnmarshaller();
//
//            serviceStructure = (MonitoredElement) u.unmarshal(is);
//        }
//
//
//
//        //read CompositionRulesConfiguration
//        {
//            InputStream is = new FileInputStream("/home/daniel-tuwien/Documents/CELAR_GIT/multilevel-metrics-evaluation/MELA-Core/MELA-AnalysisService/src/test/java/testFiles/compositionRules.xml");
//            JAXBContext jc = JAXBContext.newInstance(CompositionRulesConfiguration.class);
//            Unmarshaller u = jc.createUnmarshaller();
//
//            compositionRulesConfiguration = (CompositionRulesConfiguration) u.unmarshal(is);
//        }
//
//
//        //read Requirements
//        {
//            InputStream is = new FileInputStream("/home/daniel-tuwien/Documents/CELAR_GIT/multilevel-metrics-evaluation/MELA-Core/MELA-AnalysisService/src/test/java/testFiles/requirements.xml");
//            JAXBContext jc = JAXBContext.newInstance(Requirements.class);
//            Unmarshaller u = jc.createUnmarshaller();
//
//            requirements = (Requirements) u.unmarshal(is);
//        }
//
//        String vmNrsKey = "VMs";
//        String monSnapshotsCountKey = "MonitoringSnapshotsUsed";
//        String monitoringSnapshotKey = "MonitoringSnapshotContruction";
//        String elasticitySpaceAnalysysKey = "ElasticitySpaceAnalysis";
//        String elasticityPathwayAnalysysKey = "ElasticityPathwayAnalysis";
//
//        int maxMonSnapshots = 1810;
//
//        //report without sql access
//        {
//
//            PerformanceReport performanceReport = new PerformanceReport(new String[]{vmNrsKey, monSnapshotsCountKey, monitoringSnapshotKey, elasticitySpaceAnalysysKey, elasticityPathwayAnalysysKey});
//            elasticitySpaceFunction = new ElSpaceDefaultFunction(serviceStructure);
//            elasticitySpaceFunction.setRequirements(requirements);
//
//            LightweightEncounterRateElasticityPathway elasticityPathway = null;
//
//            //do the test
//
//            for (int i = 0; i < maxMonSnapshots; i++) {
//                Runtime.getRuntime().gc();
//
//                System.out.println("Evaluating WITHOUT SQL on snapshot " + i);
//
//                ServiceMonitoringSnapshot monitoringData = dataAccess.getMonitoredData(serviceStructure);
//              
//                performanceReport.addReportEntry(vmNrsKey, "" + monitoringData.getMonitoredData(MonitoredElement.MonitoredElementLevel.VM).size());
//
//                if (monitoringData == null) {
//                    break;
//                }
//
//                //profile aggregation
//                Date beforeAggregation = new Date();
//                ServiceMonitoringSnapshot aggregated = aggregatedMonitoringDataSQLAccess.extractLatestMonitoringData();
//                Date afterAggregation = new Date();
//                performanceReport.addReportEntry(monitoringSnapshotKey, "" + (afterAggregation.getTime() - beforeAggregation.getTime()));
//
////                aggregatedMonitoringDataSQLAccess.writeMonitoringData(aggregated);
//
//                performanceReport.addReportEntry(monSnapshotsCountKey, "" + 1);
//
//                //profile trianing el space
//                Date beforeSpace = new Date();
//
//                elasticitySpaceFunction.trainElasticitySpace(aggregated);
//
//                //used to get only the lates monitored emtrics to be able to train the el pathway
//                ElSpaceDefaultFunction tmpElSpaceFct = new ElSpaceDefaultFunction(serviceStructure);
//                tmpElSpaceFct.trainElasticitySpace(aggregated);
//
//                Date afterSpace = new Date();
//                performanceReport.addReportEntry(elasticitySpaceAnalysysKey, "" + (afterSpace.getTime() - beforeSpace.getTime()));
//
//                Map<Metric, List<MetricValue>> map = tmpElSpaceFct.getElasticitySpace().getMonitoredDataForService(serviceStructure);
//
//
//                if (elasticityPathway == null) {
//                    List<Metric> metrics = new ArrayList<Metric>(map.keySet());
//                    //we need to know the number of weights to add in instantiation
//                    elasticityPathway = new LightweightEncounterRateElasticityPathway(metrics.size());
//                }
//                
//                Date beforePathway = new Date();
//                elasticityPathway.trainElasticityPathway(map);
//                Date afterPathway = new Date();
//                performanceReport.addReportEntry(elasticityPathwayAnalysysKey, "" + (afterPathway.getTime() - beforePathway.getTime()));
//
//            }
//
//            performanceReport.writeToCSVFile("/home/daniel-tuwien/Documents/CELAR_GIT/multilevel-metrics-evaluation/MELA-Core/MELA-AnalysisService/perfTestResults/evalOnlineWithoutSQLAccess.csv");
//        }
////
////
////        //report with sql access
////        {
////
////
////
////            PerformanceReport performanceReport = new PerformanceReport(new String[]{monSnapshotsCountKey, monitoringSnapshotKey, elasticitySpaceAnalysysKey, elasticityPathwayAnalysysKey});
////
////
////            //do the test
////            for (int i = 0; i < maxMonSnapshots; i++) {
////                Runtime.getRuntime().gc();
////                
////                elasticitySpaceFunction = new ElSpaceDefaultFunction(serviceStructure);
////                elasticitySpaceFunction.setRequirements(requirements);
////                if (i % 100 == 0) {
////                    System.out.println("Evaluating WITH SQL on snapshot " + i);
////                }
////
////                ServiceMonitoringSnapshot monitoringData = dataAccess.getMonitoredData(serviceStructure);
////
////                if (monitoringData == null) {
////                    break;
////                }
////
////                //profile aggregation
////                Date beforeAggregation = new Date();
////                ServiceMonitoringSnapshot aggregated = instantMonitoringDataEnrichmentEngine.enrichMonitoringData(compositionRulesConfiguration, monitoringData);
////                Date afterAggregation = new Date();
////                performanceReport.addReportEntry(monitoringSnapshotKey, "" + (afterAggregation.getTime() - beforeAggregation.getTime()));
////
////
////                aggregatedMonitoringDataSQLAccess.writeMonitoringData(aggregated);
////
////
////                LightweightEncounterRateElasticityPathway elasticityPathway = null;
////
////
////                List<Metric> metrics = null;
////
////                Date beforeSpace = new Date();
////
////                List<ServiceMonitoringSnapshot> extractedData = aggregatedMonitoringDataSQLAccess.extractMonitoringData(0, i);
//////
////
////                performanceReport.addReportEntry(monSnapshotsCountKey, "" + i);
////
////                //profile trianing el space
////
////                elasticitySpaceFunction.trainElasticitySpace(extractedData);
////
////                Date afterSpace = new Date();
////                performanceReport.addReportEntry(elasticitySpaceAnalysysKey, "" + (afterSpace.getTime() - beforeSpace.getTime()));
////
////
////                Map<Metric, List<MetricValue>> map = elasticitySpaceFunction.getElasticitySpace().getMonitoredDataForService(serviceStructure);
////
////                Date beforePathway = new Date();
////                if (map != null && metrics == null) {
////                    metrics = new ArrayList<Metric>(map.keySet());
////                    //we need to know the number of weights to add in instantiation
////                    elasticityPathway = new LightweightEncounterRateElasticityPathway(metrics.size());
////                }
////
////                elasticityPathway.trainElasticityPathway(map);
////                Date afterPathway = new Date();
////                performanceReport.addReportEntry(elasticityPathwayAnalysysKey, "" + (afterPathway.getTime() - beforePathway.getTime()));
////
////            }
////
////            performanceReport.writeToCSVFile("/home/daniel-tuwien/Documents/CELAR_GIT/multilevel-metrics-evaluation/MELA-Core/MELA-AnalysisService/perfTestResults/evalOnlineWITHSQLAccess.csv");
////        }
//    }
}
