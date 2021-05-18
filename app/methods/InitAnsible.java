package methods;

import java.io.IOException;
import java.util.*;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import dao.IAnsibleDAO;
import play.Logger;
import play.libs.Json;


import static utils.Constants.*;


public class InitAnsible {

    @Inject
    private IAnsibleDAO iAnsibleDAO;

    private Integer responseStatus;
    private String IPADDRESS;
    private final RestClient RC;
    private final Config CONFIG;
    private Ansible ansible = new Ansible();
    ArrayList<String> idlist =new ArrayList<String>();
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

            // Initial configuration for Ansible
            /*Config ANSIBLE_PRODUCTS = CONFIG.getConfig("ANSIBLE_PRODUCTS");
            List<String> ANSIBLEPRODUCTSLISTS = CONFIG.getStringList("ANSIBLEPRODUCTSLISTS");
            ObjectNode ansibleconf = (ObjectNode) Json.toJson(ANSIBLE_PRODUCTS.root().unwrapped());

            HashMap<String, List> hashMap = new HashMap<>();
            for (Object t : ANSIBLEPRODUCTSLISTS){

                JsonNode a = ansibleconf.get(t.toString());

                String scm_url = String.valueOf(a.get("scmurl"));
                scm_url = scm_url.substring(1, scm_url.length()-1);
                String playbookName = String.valueOf(a.get("playbook"));
                playbookName= playbookName.substring(1, playbookName.length()-1);

                String appName = t.toString();
                String inventoryId = CreateInventory(appName);
                String projectId = CreateProject(scm_url, appName);
                String jobTemplateId = CreateJobTemplate(inventoryId, projectId, playbookName, appName);


                idlist.add(inventoryId);
                idlist.add(projectId);
                idlist.add(jobTemplateId);
                ArrayList<String> templist =new ArrayList<String>();
                if(idlist.size() > 3){
                    templist.add(idlist.get(idlist.size()-3));
                    templist.add(idlist.get(idlist.size()-2));
                    templist.add(idlist.get(idlist.size()-1));
                }
                else{
                    templist.addAll(idlist);
                }
                hashMap.put(appName, templist);

            }
            ansible.setAnsibleproducts(hashMap);
            iAnsibleDAO.save(ansible);

            System.out.println("Added to DB");*/

            // Update Inventory

            System.out.println(ansible);
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

    private String CreateInventory(String appname) {
        String name = getRandomName(appname);
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

    private String CreateProject(String scm_url, String appname) {
        String name = getRandomName(appname);
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

    private String CreateJobTemplate(String inventoryId, String projectId, String playbookName, String appname) {
        String name = getRandomName(appname);
        System.out.println(name);
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

    private String getRandomName(String appname) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String name = salt.toString();
        return (appname+name);
    }

}
