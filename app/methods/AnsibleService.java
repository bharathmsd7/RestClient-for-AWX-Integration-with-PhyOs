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
            Optional<AnsibleDatabase> ansibleDatabase = iAnsibleDAO.getAnsibleDatabaseByName(appName);
            if (ansibleDatabase.isPresent()){
                String inventoryID = ansibleDatabase.get().getInventoryid();
                String projectID = ansibleDatabase.get().getProjectid();
                String jobTemplateID = ansibleDatabase.get().getJobtemplateid();
                System.out.println("inv:"+inventoryID+" pro:"+projectID+" job:"+jobTemplateID);
            }

        }
        else{
            Logger.error("Response is not valid : ",responseStatus);
        }
    }


    private void LaunchJobTemplate(String jobtemplateId) {
        String PATH = ANSIBLE_JOB_TEMPLATE_PATH + jobtemplateId + "/launch/";
        try {
            String temp = RC.getRequestWithJson( IPADDRESS , PATH , ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            System.out.println(temp);
        } catch (InterruptedException | ExecutionException | TimeoutException e){
            e.printStackTrace();
        }
    }

    private void UpdateInventory(String inventoryId, String hostIP) {
        String SATA = "{\n" +
                "  \"description\": \"Hello world\",\n" +
                "  \"name\": \"%s\"\n" +
                "}";
        String DATA = String.format(SATA, hostIP);
        String PATH = ANSIBLE_INVENTORY_PATH + inventoryId + "/hosts/";
        try {
            JsonNode temp = RC.postRequestWithData(IPADDRESS, PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
