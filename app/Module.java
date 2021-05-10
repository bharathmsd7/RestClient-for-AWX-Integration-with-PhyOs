import com.google.inject.AbstractModule;
import dao.AnsibleDAO;
import dao.IAnsibleDAO;
import org.mongodb.morphia.Datastore;

public class Module extends AbstractModule {


    @Override
    protected void configure() {
        try {
            bind(IAnsibleDAO.class).toConstructor(AnsibleDAO.class.getDeclaredConstructor(Datastore.class));
        }
        catch (NoSuchMethodException e){
            e.printStackTrace();
        }
    }
}
