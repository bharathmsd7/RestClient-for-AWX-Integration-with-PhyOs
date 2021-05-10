package methods;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

@Entity(value="Ansible", noClassnameStored = true)
public class Ansible {

    @Id
    private String name;
    @Indexed
    private String inventoryid;
    @Indexed
    private String projectid;
    @Indexed
    private String jobtemplateid;

    public Ansible(String name, String inventoryid, String projectid, String jobtemplateid){
        this.name = name;
        this.inventoryid = inventoryid;
        this.projectid = projectid;
        this.jobtemplateid = jobtemplateid;
    }

    public Ansible(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
}