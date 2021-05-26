package dao;

import com.google.inject.ImplementedBy;
import methods.AnsibleDatabase;
import org.mongodb.morphia.Key;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@ImplementedBy(AnsibleDAO.class)
public interface IAnsibleDAO {

    Key<AnsibleDatabase> save(AnsibleDatabase key);

    Optional<AnsibleDatabase> getName(String name);

    Optional<AnsibleDatabase> getInventoryid(String inventoryid);

    Optional<AnsibleDatabase> getProjectid(String projectid);

    Optional<AnsibleDatabase> getjobtemplateid(String jobtemplateid);

    Optional<AnsibleDatabase> getAnsibleproducts (HashMap<String, List> products);
}
