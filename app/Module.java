import com.google.inject.AbstractModule;
import dao.AnsibleDAO;
import dao.IAnsibleDAO;
import methods.Mongoclient;
import org.mongodb.morphia.Datastore;

public class Module extends AbstractModule {


    @Override
    public void configure() {
        try {
            bind(IAnsibleDAO.class).toConstructor(AnsibleDAO.class.getDeclaredConstructor(Datastore.class));
        }
        catch (NoSuchMethodException e){
            e.printStackTrace();
        }
        bind(Datastore.class).toInstance(Mongoclient.datastore());
    }
}
