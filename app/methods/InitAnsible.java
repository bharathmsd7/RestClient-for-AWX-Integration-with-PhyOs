package methods;

import java.util.Random;
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

    private RestClient RC;
    private Config CONFIG;

    @Inject
    public InitAnsible(RestClient rc, Config config)
    {
        this.RC = rc;
        this.CONFIG = config;
    }


    public void InitAnsibleSteps()
    {
        IPADDRESS = CONFIG.getString("ANSIBLE_NODE_IP");
        System.out.println(IPADDRESS);
        try {
            responseStatus = RC.getRequest(IPADDRESS, ANSIBLE_PING_PATH);
            System.out.println("response : "+ responseStatus );
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            Logger.error("Unable to PING Ansible Tower : ", e);
        }

        if (responseStatus == 200)
        {
            String scm_url = "https://github.com/ansible/test-playbooks.git";
            String playbookName = "hello world.yml";
            String InventoryId = CreateInventory();
            System.out.println("I Id :" + InventoryId);
            String ProjectId = CreateProject(scm_url);
            System.out.println("P Id :" + ProjectId);
            String JobTemplateId = CreateJobTemplate(InventoryId, ProjectId, playbookName);
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

    private String CreateInventory() {
        String name = getRandomName();

        String SATA = "{\n" +
                "  \"organization\": 1,\n" +
                "  \"name\": \"%s\"\n"+
                "}";
        String DATA =  String.format(SATA, name);
       
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

    private String CreateProject(String scm_url) {
        String name = getRandomName();
        String SATA = "{\n" +
                "  \"allow_override\": true,\n" +
                "  \"name\": \"%s\",\n" +
                "  \"organization\": 1,\n" +
                "  \"scm_type\": \"git\",\n" +
                "  \"scm_url\": \"%s\"\n"+
                "}";
        String DATA =  String.format(SATA, name, scm_url);

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
        String name = getRandomName();
        String SATA ="{\n" +
                "  \"inventory\": \"%s\" ,\n" +
                "  \"name\": \"%s\" ,\n" +
                "  \"organization\": 1,\n" +
                "  \"playbook\": \"%s\" ,\n" +
                "  \"project\": \"%s\" \n" +
                "}";
        String DATA =  String.format(SATA, inventoryId, name, playbookName, projectId);
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

    private String getRandomName()
    {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return (salt.toString());
    }

}
