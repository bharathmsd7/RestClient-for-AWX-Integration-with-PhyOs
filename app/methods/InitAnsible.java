package methods;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import dao.IAnsibleDAO;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.dao.DAO;
import play.Logger;
import play.libs.Json;


import static utils.Constants.*;


public class InitAnsible {

    @Inject
    private IAnsibleDAO iAnsibleDAO;
    private String InventoryId;
    private String ProjectId;
    private String JobTemplateId;

    private Integer responseStatus;
    private String IPADDRESS;
    private RestClient RC;
    private Config CONFIG;

    private Config ANSIBLE_PRODUCTS;
    private List ANSIBLEPRODUCTSLISTS;

    @Inject
    public InitAnsible(RestClient rc, Config config)
    {
        this.RC = rc;
        this.CONFIG = config;
    }

    public void InitAnsibleSteps()
    {
        IPADDRESS = CONFIG.getString("ANSIBLE_NODE_IP");

        try {
            responseStatus = RC.getRequest(IPADDRESS, ANSIBLE_PING_PATH);
            System.out.println("response : "+ responseStatus );
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            Logger.error("Unable to PING Ansible Tower : ", e);
        }

        if (responseStatus == 200)
        {
            ANSIBLE_PRODUCTS = CONFIG.getConfig("ANSIBLE_PRODUCTS");
            ANSIBLEPRODUCTSLISTS = CONFIG.getStringList("ANSIBLEPRODUCTSLISTS");
            ObjectNode ansibleconf = (ObjectNode) Json.toJson(ANSIBLE_PRODUCTS.root().unwrapped());

            for (Object t : ANSIBLEPRODUCTSLISTS){

                Ansible ansible = new Ansible();
                String appName = t.toString();

                JsonNode a = ansibleconf.get(t.toString());
                String scm_url = String.valueOf(a.get("scmurl"));
                String scmurl = scm_url.substring(1, scm_url.length()-1);
                String playbookName = String.valueOf(a.get("playbook"));
                String playbookname = playbookName.substring(1, playbookName.length()-1);
                InventoryId = CreateInventory();

                System.out.println("INVENTORY Id :" + InventoryId);
                ProjectId = CreateProject(scmurl);

                System.out.println("PROJECT Id :" + ProjectId);
                JobTemplateId = CreateJobTemplate(InventoryId, ProjectId, playbookname);

                System.out.println("JOB TEMPLATE Id :" + JobTemplateId);

                ansible.setName(appName);
                ansible.setInventoryid(InventoryId);
                ansible.setProjectid(ProjectId);
                ansible.setJobtemplateid(JobTemplateId);
                System.out.println(iAnsibleDAO);
                iAnsibleDAO.save(ansible);
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

    private String getRandomName() {
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
