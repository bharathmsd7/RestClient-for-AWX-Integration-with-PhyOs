package methods;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

@Entity(value="Ansible", noClassnameStored = true)
public class AnsibleDatabase {

    @Id
    private String uId;
    private String name;
    private String inventoryid;
    private String projectid;
    private String jobtemplateid;

    private HashMap<String, List> ansibleproducts;

    public AnsibleDatabase(String name, String inventoryid, String projectid, String jobtemplateid, HashMap<String, List> ansibleproducts){
        this.name = name;
        this.inventoryid = inventoryid;
        this.projectid = projectid;
        this.jobtemplateid = jobtemplateid;
        this.ansibleproducts = ansibleproducts;
    }

    @Inject
    public AnsibleDatabase(){}

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name;}

    public String getInventoryid() {
        return inventoryid;
    }

    public void setInventoryid(String inventoryid) {
        this.inventoryid = inventoryid;
    }

    public String getProjectid() {
        return projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }

    public String getJobtemplateid() {
        return jobtemplateid;
    }

    public void setJobtemplateid(String jobtemplateid) {
        this.jobtemplateid = jobtemplateid;
    }

    public void setAnsibleproducts(HashMap<String, List> ansibleproducts){ this.ansibleproducts = ansibleproducts; }

    public HashMap<String, List> getAnsibleproducts(){
        return ansibleproducts;
    }


}
