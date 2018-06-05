package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class TokenRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
   private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class TokenTable(tag: Tag) extends Table[Token](tag, _tableName = "token") {
    def token: Rep[String] = column[String]("token")
    def time: Rep[Long] = column[Long]("time")

    def * : ProvenShape[Token] = (token, time) <> ((Token.apply _).tupled, Token.unapply)
  }

  private val tokens = TableQuery[TokenTable]
 
  def list(): Future[Seq[Token]] = db.run {
    tokens.result
  }
  
  def last(): Future[Option[Token]] = db.run {
    tokens.sortBy(_.time.desc).result.headOption
  }

  def upsert(token: String): Future[Int] = db.run {
    val oldToken = Await.result(last(), Duration.Inf)
    if (oldToken == None) tokens += Token(token, System.currentTimeMillis()/1000)
    else tokens.filter(_.token === oldToken.get.token).map(t => t.token -> t.time)
      .update(token, System.currentTimeMillis()/1000)
  }

  def insert(token: String, time: Long): Future[Int] = db.run {
    val selectExpression = Query((token, time)).filterNot(_ => tokens.filter(_.token === token).exists)
    tokens.map(t => (t.token, t.time)).forceInsertQuery(selectExpression)
  }
}