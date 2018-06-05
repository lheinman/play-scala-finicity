package controllers

import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import services.DBConnectionService

class DBController @Inject()(
                              protected val dbConfigProvider: DatabaseConfigProvider,
                              cc: ControllerComponents,
                              connection: DBConnectionService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  def showDB() = Action { Ok(connection.showDB()) }
  def createDB() = Action { Ok(connection.createDB()) }
  def dropDB() = Action { Ok(connection.dropDB()) }
  def createSchema() = Action {
    var out = connection.createToken() + "\n"
    out += connection.createBorrowers()
    Ok(out)
  }
}