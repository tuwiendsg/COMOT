/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author hungld
 */
public class RunCollector {

    private static final Logger logger = EliseConfiguration.logger;
    private static final String mainFolder = EliseConfiguration.ELISE_HOME + "/extensions";
    private static final String FILE_JAR_EXT = ".jar";

    public static void RunAllCollector() {
        logger.debug("Running all collector ...");

        String[] folders = listSubFolder(mainFolder);
        logger.debug("Number of child folders: " + folders.length);
        for (String f : folders) {
            String checkingDir = mainFolder + "/" + f;
            logger.debug("Checking folder: " + f);
            File file = new File(checkingDir);

            String[] jars = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                 return name.endsWith(".jar");  
                }
            });
      
      logger.debug("Number of jar file: " + jars.length);
            for (String jar : jars) {
                logger.debug("Runing collector: " + jar);
                try {
                    FileUtils.copyFile(new File(EliseConfiguration.ELISE_CONFIGURATION_FILE), new File(checkingDir+"/elise.conf"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                logger.debug(executeCommand3("java -jar " + checkingDir + "/" + jar, checkingDir));
            }
        }
    }

    private static String[] listSubFolder(String mainFolder) {
        File file = new File(mainFolder);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });
    
    return directories;
    }

    public static void executeCommand2(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec("echo \"" + cmd + "\" > /tmp/command.sh");
            p.waitFor();
            p = Runtime.getRuntime().exec("bash /tmp/command.sh > output.log");
            p.waitFor();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static String executeCommand3(String command, String workingDir) {
        StringBuffer output = new StringBuffer();
        String[] env = {"/bin", "/usr/bin", "/opt/java/bin"};
        try {
            Process p = Runtime.getRuntime().exec(command, env, new File(workingDir));
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            while ((line = reader1.readLine()) != null) {
                output.append(line + "\n");
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public static String executeCommand(String cmd, String workingDir, String executeFrom) {
        logger.debug("Running command: " + cmd);
        if (workingDir == null) {
            workingDir = "/tmp";
        }
        try {
            String[] splitStr = cmd.split("\\s+");
            ProcessBuilder pb = new ProcessBuilder(splitStr).inheritIO();
            pb.directory(new File(workingDir));

            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            StringBuffer output = new StringBuffer();
            int lineCount = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (lineCount < 20) {
                    output.append(line);
                }
                lineCount++;
                logger.debug(line);
            }
            p.waitFor();
            System.out.println("Execute Commang output: " + output.toString().trim());
            if (p.exitValue() == 0) {
                logger.debug("Command exit 0, result: " + output.toString().trim());
                return output.toString().trim();
            }
            logger.debug("Command return non zero code: " + p.exitValue());
            return null;
        } catch (InterruptedException | IOException e1) {
            logger.error("Error when execute command. Error: " + e1);
        }
        return null;
    }
}
