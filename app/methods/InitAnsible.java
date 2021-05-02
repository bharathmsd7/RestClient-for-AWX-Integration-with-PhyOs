package methods;

import javax.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import play.Logger;
import static utils.Constants.*;

//@Entity(value="Ansible", noClassnameStored = true)
public class InitAnsible {
    //@Id
    private Integer responseStatus;
    private String IPADDRESS;

    private final RestClient RC;
    private final Config CONFIG;

    @Inject
    public InitAnsible(RestClient rc, Config config)
    {
        this.RC = rc;
        this.CONFIG = config;
    }

    @Inject
    public void InitAnsibleTemplates()
    {
        IPADDRESS = CONFIG.getString("ANSIBLE_NODE_IP");
        try {
            responseStatus = RC.getRequest(IPADDRESS, ANSIBLE_PING_PATH);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            Logger.error("Unable to PING Ansible Tower : ", e);
        }

        if (responseStatus == 200)
        {
            String inventory_name = "default";
            String scm_url = "https://github.com/ansible/test-playbooks.git";
            String playbookName = "hello world.yml";
            String InventoryId = CreateInventory(inventory_name);
            String ProjectId = CreateProject(scm_url);
            String JobTemplateId = CreateJobTemplate(InventoryId, ProjectId, playbookName);
            System.out.println("I Id :" + InventoryId);
            System.out.println("P Id :" + ProjectId);
            System.out.println("J Id :" + JobTemplateId);
        }

        else{
            Logger.error("Response is not valid : ",responseStatus);
        }
    }

    private void CreateHost(){
        String DATA = "{\n" +
                "  \"enabled\": true,\n" +
                "  \"inventory\": 17,\n" +
                "  \"name\": \"192.168.1.73\"\n" +
                "}";
        try {
            JsonNode temp = RC.postRequestWithData(IPADDRESS, ANSIBLE_HOSTS_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            System.out.println(temp);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private String CreateInventory(String inventory_name) {
        String DATA = "{\n" +
                "  \"name\": " + inventory_name +
                "  \"organization\": 1\n" +
                "}";
        try {
            JsonNode temp = RC.postRequestWithData(IPADDRESS, ANSIBLE_INVENTORY_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (temp != null)
            {
                return temp.get("id").asText();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String CreateProject( String scm_url) {

        String DATA = "{\n" +
                "  \"allow_override\": true,\n" +
                "  \"name\": \"sample_project\",\n" +
                "  \"organization\": 1,\n" +
                "  \"scm_type\": \"git\",\n" +
                "  \"scm_url\": " + scm_url +
                "}";
        try {
            JsonNode temp = RC.postRequestWithData(IPADDRESS, ANSIBLE_PROJECT_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (temp != null) {
                return temp.get("id").asText();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String CreateJobTemplate(String inventoryId, String projectId, String playbookName) {
        String DATA ="{\n" +
                "  \"inventory\":" + inventoryId + ",\n" +
                "  \"name\": \"foobar\",\n" +
                "  \"organization\": 1,\n" +
                "  \"playbook\": "+ playbookName+ ",\n" +
                "  \"project\": "+projectId+"\n" +
                "}";
        try {
            JsonNode temp = RC.postRequestWithData(IPADDRESS, ANSIBLE_JOB_TEMPLATE_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (temp != null) {
                return temp.get("id").asText();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }


}
