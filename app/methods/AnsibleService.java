package methods;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import dao.IAnsibleDAO;
import org.mongodb.morphia.query.UpdateResults;
import play.Logger;
import play.libs.Json;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import static utils.Constants.*;


public class AnsibleService {

    @Inject
    private IAnsibleDAO iAnsibleDAO;

    private String IPADDRESS;
    private final RestClient RC;
    private final Config CONFIG;

    @Inject
    public AnsibleService(RestClient rc, Config config)
    {
        this.RC = rc;
        this.CONFIG = config;
    }

    public void AnsibleRuntime(String appName, String hostip){
        IPADDRESS = CONFIG.getString("ANSIBLE_NODE_IP");
        Integer responseStatus = 0;
        try {
            responseStatus = RC.getRequest(IPADDRESS, ANSIBLE_PING_PATH);
            System.out.println("response : "+ responseStatus );
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            Logger.error("Unable to PING Ansible Tower : ", e);
        }


        if (responseStatus == 200)
        {
            String inventoryID = null;
            String projectID = null;
            String jobTemplateID = null;

            String username = null;
            String password = null;
            String name = null;
            String jobId = null;

            Optional<AnsibleDatabase> ansibleDatabase = iAnsibleDAO.getAnsibleDatabaseByName(appName);
            Config HostIp = CONFIG.getConfig("HOSTIP_CREDENTIALS");
            List<String> HostCredentials = CONFIG.getStringList("HOSTIPLIST");
            ObjectNode hostcredentials = (ObjectNode) Json.toJson(HostIp.root().unwrapped());

            for (Object host : HostCredentials) {
                if(host.equals(hostip)){
                    JsonNode jsonNode = hostcredentials.get(host.toString());
                    username = String.valueOf(jsonNode.get("username"));
                    //System.out.println(username);
                    username = username.substring(1, username.length() - 1);
                    password = String.valueOf(jsonNode.get("password"));
                    password = password.substring(1, password.length() - 1);
                    name = getRandomName(host.toString());
                }
            }
            if (ansibleDatabase.isPresent()){
                inventoryID = ansibleDatabase.get().getInventoryid();
                projectID = ansibleDatabase.get().getProjectid();
                jobTemplateID = ansibleDatabase.get().getJobtemplateid();

                System.out.println("InventoryID : "+inventoryID+" ProjectID: "+projectID+" JobTemplateID:"+jobTemplateID);
            }
            if (inventoryID != null && projectID != null && jobTemplateID != null){
                String result = UpdateInventory(inventoryID, hostip);
                String result1 = UpdateJobTemplateWithCredentials(jobTemplateID, name, username, password);
                //System.out.println(result);
                if (result!=null){
                    if(result.equals("Already exists")){
                        System.out.println("Host with this Name and Inventory already exists.");
                    }
                    else{
                        jobId = LaunchJobTemplate(jobTemplateID);
                        System.out.println("JobID : "+jobId);
                        List<String> runningJobsList = new ArrayList<>();
                        runningJobsList.add(jobId);
                        AnsibleDatabase ansibleDatabase1 = new AnsibleDatabase();
                        ansibleDatabase1.setName("RunningJobsList");
                        ansibleDatabase1.setRunningJobsList(runningJobsList);
                        iAnsibleDAO.save(ansibleDatabase1);

                    }
                }
            }
            // JOB SUMMARY

        }
        else Logger.error("Response is not valid : ", responseStatus);
    }

    private String JobEvents(String jobId){
        String PATH = ANISBLE_JOBS + jobId+"/job_events/";
        try{
            JsonNode temp = RC.getRequestWithJson(IPADDRESS, PATH, ANSIBLE_TOWER_USERNAME,ANSIBLE_TOWER_PASSWORD);
            System.out.println(temp);
            return temp.get("stdout").asText();
        } catch (InterruptedException | ExecutionException | TimeoutException e){
            e.printStackTrace();
        }

        return null;
    }

    private String JobSummary(String jobId){
        String PATH = ANISBLE_JOBS + jobId+"/";
        try{
            JsonNode temp = RC.getRequestWithJson(IPADDRESS, PATH, ANSIBLE_TOWER_USERNAME,ANSIBLE_TOWER_PASSWORD);
            return temp.get("status").asText();
        } catch (InterruptedException | ExecutionException | TimeoutException e){
            e.printStackTrace();
        }

        return null;

    }

    private String UpdateJobTemplateWithCredentials(String jobtemplateid ,String name, String username, String password){
            String PATH = ANSIBLE_JOB_TEMPLATE_PATH + jobtemplateid + "/credentials/";

            String Json = "{\"credential_type\":1, \"name\":\"%s\", \"organization\":1, \"inputs\": { \"password\":\"%s\",\"username\":\"%s\"}}";
            String DATA = String.format(Json, name, password, username);
            //System.out.println(DATA);
        try {
            JsonNode res = RC.postRequestWithData(IPADDRESS, PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (res != null) {
                //System.out.println(res);
                return res.get("id").asText();

            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return  null;
    }

    private String LaunchJobTemplate(String jobtemplateId) {
        String PATH = ANSIBLE_JOB_TEMPLATE_PATH + jobtemplateId + "/launch/";
        try {
            JsonNode temp = RC.postRequestWithoutData( IPADDRESS , PATH  ,ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (temp != null) {
                System.out.println("Job has been Launched successfully...");
                return temp.get("id").asText();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e){
            e.printStackTrace();
        }
        return null;
    }

    private String UpdateInventory(String inventoryId, String hostIP) {
        String json = "{\n" +
                "  \"description\": \"Hello world\",\n" +
                "  \"name\": \"%s\"\n" +
                "}";
        String DATA = String.format(json, hostIP);
        String PATH = ANSIBLE_INVENTORY_PATH + inventoryId + "/hosts/";
        try {
            JsonNode res = RC.postRequestWithData(IPADDRESS, PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (res != null) {
                String t = String.valueOf(res.get("__all__"));
                t = t.substring(1, t.length() - 1);

                if(t.equals("Host with this Name and Inventory already exists.")){
                    return "Already exists";
                }
                else{
                    //System.out.println(temp);
                    return res.get("id").asText();
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    return null;
    }

    private String getRandomName(String appName) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String name = salt.toString();
        return (appName+name);
    }

}
