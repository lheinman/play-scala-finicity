import com.google.inject.AbstractModule
import services.{MySQLConnection,DBConnectionService}

class Module extends AbstractModule {
  override def configure() = {
    bind(classOf[DBConnectionService]).to(classOf[MySQLConnection])
  }  
}