package dao;

import com.google.inject.ImplementedBy;
import methods.Ansible;
import java.util.Optional;

@ImplementedBy(AnsibleDAO.class)
public interface IAnsibleDAO {

    Optional<Ansible> getName(String name);

    Optional<Ansible> getInventoryid(String inventoryid);

    Optional<Ansible> getProjectid(String projectid);

    Optional<Ansible> getjobtemplateid(String jobtemplateid);

}
