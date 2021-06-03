package methods;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import dao.IAnsibleDAO;
import play.Logger;

import javax.inject.Inject;
import java.util.Optional;
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
            String credentialsID = null;
            Optional<AnsibleDatabase> ansibleDatabase = iAnsibleDAO.getAnsibleDatabaseByName(appName);
            Optional<AnsibleDatabase> ansibleDatabase1 = iAnsibleDAO.getAnsibleDatabaseByName(hostip);
            if (ansibleDatabase.isPresent() && ansibleDatabase1.isPresent()){
                inventoryID = ansibleDatabase.get().getInventoryid();
                projectID = ansibleDatabase.get().getProjectid();
                jobTemplateID = ansibleDatabase.get().getJobtemplateid();
                credentialsID = ansibleDatabase1.get().getCredentialsid();
                System.out.println("inv:"+inventoryID+" pro:"+projectID+" job:"+jobTemplateID+" credentials: "+credentialsID);
            }
            if (inventoryID != null && projectID != null && jobTemplateID != null){
                String result = UpdateInventory(inventoryID, hostip);
                System.out.println(result);
                if (result!=null){
                    if(result.equals("Already exists")){
                        System.out.println("Host with this Name and Inventory already exists.");
                    }
                    else{
                        String jobId = LaunchJobTemplate(jobTemplateID, credentialsID);
                        System.out.println(jobId);
                    }
                }
            }
        }
        else{
            Logger.error("Response is not valid : ",responseStatus);
        }
    }


    private String LaunchJobTemplate(String jobtemplateId, String credentialsId) {
        String PATH = ANSIBLE_JOB_TEMPLATE_PATH + jobtemplateId + "/launch/";
        String SATA = "{\n" +
                "  \"extra_vars\": {\n" +
                "    \"credentials\": 7\n" +
                "  }\n" +
                "}";
        String DATA = String.format(SATA, credentialsId);
        try {
            JsonNode temp = RC.postRequestWithoutData( IPADDRESS , PATH , ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (temp != null) {
                return temp.get("id").asText();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e){
            e.printStackTrace();
        }
        return null;
    }



    private String UpdateInventory(String inventoryId, String hostIP) {
        String SATA = "{\n" +
                "  \"description\": \"Hello world\",\n" +
                "  \"name\": \"%s\"\n" +
                "}";
        String DATA = String.format(SATA, hostIP);
        String PATH = ANSIBLE_INVENTORY_PATH + inventoryId + "/hosts/";
        try {
            JsonNode temp = RC.postRequestWithData(IPADDRESS, PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (temp != null) {
                String t = String.valueOf(temp.get("__all__"));
                t = t.substring(1, t.length() - 1);
                if(t.equals("Host with this Name and Inventory already exists.")){
                    return "Already exists";
                }
                else{
                    return temp.get("id").asText();
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    return null;
    }

}
