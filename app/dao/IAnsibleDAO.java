package dao;

import com.google.inject.ImplementedBy;
import methods.Ansible;
import org.mongodb.morphia.Key;

import java.util.Optional;

@ImplementedBy(AnsibleDAO.class)
public interface IAnsibleDAO {

    Key<Ansible> save(Ansible key);

    Optional<Ansible> getName(String name);

    Optional<Ansible> getInventoryid(String inventoryid);

    Optional<Ansible> getProjectid(String projectid);

    Optional<Ansible> getjobtemplateid(String jobtemplateid);

}
