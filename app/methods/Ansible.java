package methods;

import java.util.*;
import javax.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import dao.IAnsibleDAO;
import play.Logger;
import play.libs.Json;
import static utils.Constants.*;

public class Ansible {

    @Inject
    private IAnsibleDAO iAnsibleDAO;

    private String IPADDRESS;
    private final RestClient RC;
    private final Config CONFIG;

    @Inject
    public Ansible(RestClient rc, Config config)
    {
        this.RC = rc;
        this.CONFIG = config;
    }

    public void initalAnsibleSetup()
    {
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
            // Initial configuration for Ansible

            Config ANSIBLE_PRODUCTS = CONFIG.getConfig("ANSIBLE_PRODUCTS");
            List<String> ANSIBLEPRODUCTSLISTS = CONFIG.getStringList("ANSIBLEPRODUCTSLISTS");
            ObjectNode ansibleconf = (ObjectNode) Json.toJson(ANSIBLE_PRODUCTS.root().unwrapped());

            for (Object product : ANSIBLEPRODUCTSLISTS) {

                JsonNode jsonNode = ansibleconf.get(product.toString());

                String scm_url = String.valueOf(jsonNode.get("scmurl"));
                scm_url = scm_url.substring(1, scm_url.length() - 1);
                String playbookName = String.valueOf(jsonNode.get("playbook"));
                playbookName = playbookName.substring(1, playbookName.length() - 1);

                String appName = product.toString();
                String inventoryId = CreateInventory(appName);
                String projectId = CreateProject(scm_url, appName);
                String jobTemplateId = CreateJobTemplate(inventoryId, projectId, playbookName, appName);

                AnsibleDatabase ansibledatabase = new AnsibleDatabase();
                ansibledatabase.setName(appName);
                ansibledatabase.setInventoryid(inventoryId);
                ansibledatabase.setProjectid(projectId);
                ansibledatabase.setJobtemplateid(jobTemplateId);

                iAnsibleDAO.save(ansibledatabase);

            }
            System.out.println("Added to DB");
        }
        else{
            Logger.error("Response is not valid : ",responseStatus);
        }
    }

    private String CreateInventory(String appName) {
        String name = getRandomName(appName);
        String json = "{\n" +
                "  \"organization\": 1,\n" +
                "  \"name\": \"%s\"\n"+
                "}";
        String DATA =  String.format(json, name);

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

    private String CreateProject(String scm_url, String appName) {
        String name = getRandomName(appName);
        String json = "{\n" +
                "  \"allow_override\": true,\n" +
                "  \"name\": \"%s\",\n" +
                "  \"organization\": 1,\n" +
                "  \"scm_type\": \"git\",\n" +
                "  \"scm_url\": \"%s\"\n"+
                "}";
        String DATA =  String.format(json, name, scm_url);
        //System.out.println(DATA);
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

    private String CreateJobTemplate(String inventoryId, String projectId, String playbookName, String appName) {
        String name = getRandomName(appName);
        System.out.println(name);
        String json ="{\n" +
                "  \"inventory\": \"%s\" ,\n" +
                "  \"name\": \"%s\" ,\n" +
                "  \"organization\": 1,\n" +
                "  \"playbook\": \"%s\" ,\n" +
                "  \"project\": \"%s\" \n" +
                "}";
        String DATA =  String.format(json, inventoryId, name, playbookName, projectId);
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
