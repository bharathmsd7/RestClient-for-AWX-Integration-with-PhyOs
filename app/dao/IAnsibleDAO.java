package dao;

import com.google.inject.ImplementedBy;
import methods.AnsibleDatabase;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.UpdateResults;

import java.util.List;
import java.util.Optional;

@ImplementedBy(AnsibleDAO.class)
public interface IAnsibleDAO {

    Key<AnsibleDatabase> save(AnsibleDatabase key);

    Optional<AnsibleDatabase> getAnsibleDatabaseByName(String id);

    UpdateResults updateJobList(String id, List<String> joblist);
}
