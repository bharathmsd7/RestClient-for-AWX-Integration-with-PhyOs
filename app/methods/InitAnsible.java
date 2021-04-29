package methods;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import play.Logger;

import static utils.Constants.*;

public class InitAnsible {

    private RestClient rc;
    private Config config;
    private Integer responseStatus;
    private String IPADDRESS;

    @Inject
    public InitAnsible(RestClient rc, Config config)
    {
        this.rc = rc;
        this.config = config;
    }

    @Inject
    public void InitAnsibleTemplates()
    {
        IPADDRESS = "http://" +config.getString("ansible_node_ip");

        try {
            responseStatus = rc.getRequest(IPADDRESS, ANSIBLE_PING_PATH);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            Logger.error("Unable to PING Ansible Tower : ", e);
        }

        if (responseStatus == 200)
        {
            CreateJobTemplate();
        }
        else{
            Logger.error("Response is not valid : ",responseStatus);
        }

    }

    private void CreateInventory() {

        String DATA = "{\n" +
                "  \"name\": \"inventoryname1\",\n" +
                "  \"organization\": 1\n" +
                "}";
        try {
            JsonNode temp = rc.postRequestWithData(IPADDRESS, ANSIBLE_INVENTORY_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            if (temp != null)
            {
                String INVENTORY_ID = temp.get("id").asText();

            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void CreateProject() {
        String DATA = "{\n" +
                "  \"allow_override\": true,\n" +
                "  \"name\": \"sample_project\",\n" +
                "  \"organization\": 1,\n" +
                "  \"scm_type\": \"git\",\n" +
                "  \"scm_url\": \"https://github.com/ansible/test-playbooks.git\"\n" +
                "}";
        try {
            JsonNode temp = rc.postRequestWithData(IPADDRESS, ANSIBLE_PROJECT_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            System.out.println(temp);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void CreateJobTemplate() {
        String DATA ="{\n" +
                "  \"inventory\": 19,\n" +
                "  \"name\": \"foobar\",\n" +
                "  \"organization\": 1,\n" +
                "  \"playbook\": \"hello world.yml\",\n" +
                "  \"project\": 42\n" +
                "}";
        try {
            JsonNode temp = rc.postRequestWithData(IPADDRESS, ANSIBLE_JOBTEMPLATE_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            System.out.println(temp);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void CreateHost(){
        String DATA = "{\n" +
                "  \"enabled\": true,\n" +
                "  \"inventory\": 17,\n" +
                "  \"name\": \"192.168.1.73\"\n" +
                "}";
        try {
            JsonNode temp = rc.postRequestWithData(IPADDRESS, ANSIBLE_HOSTS_PATH, DATA, ANSIBLE_TOWER_USERNAME, ANSIBLE_TOWER_PASSWORD);
            System.out.println(temp);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

}
