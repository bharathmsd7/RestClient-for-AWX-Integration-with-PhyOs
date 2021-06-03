package dao;

import com.google.inject.ImplementedBy;
import methods.AnsibleDatabase;
import org.mongodb.morphia.Key;

import java.util.Optional;

@ImplementedBy(AnsibleDAO.class)
public interface IAnsibleDAO {

    Key<AnsibleDatabase> save(AnsibleDatabase key);

    Optional<AnsibleDatabase> getAnsibleDatabaseByName(String appName);


}
