package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class PersonRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class BorrowersTable(tag: Tag) extends Table[Person](tag, _tableName = "borrowers") {

    /** The username column */
    def username: Rep[String] = column[String]("username")

    /** The firstName column */
    def firstName: Rep[String] = column[String]("firstName")

    /** The lastName column */
    def lastName: Rep[String] = column[String]("lastName")

    /** The month column */
    def month: Rep[Option[String]] = column[Option[String]]("month")

    /** The dayOfMonth column */
    def dayOfMonth: Rep[Option[String]] = column[Option[String]]("dayOfMonth")

    /** The year column */
    def year: Rep[Option[String]] = column[Option[String]]("year")

    /** The emailAddress column */
    def emailAddress: Rep[Option[String]] = column[Option[String]]("emailAddress")

    /** The address column */
    def address: Rep[Option[String]] = column[Option[String]]("address")

    /** The city column */
    def city: Rep[Option[String]] = column[Option[String]]("city")

    /** The state column */
    def state = column[Option[String]]("state")

    /** The zip column */
    def zip: Rep[Option[String]] = column[Option[String]]("zip")

    /** The phone column */
    def phone: Rep[Option[String]] = column[Option[String]]("phone")

    /** The ssn column */
    def ssn: Rep[Option[String]] = column[Option[String]]("ssn")

    /** The customer column */
    def customer: Rep[Option[String]] = column[Option[String]]("customer")

    /** The customerTime column */
    def customerTime: Rep[Option[Long]] = column[Option[Long]]("customerTime")

    /** The consumer column */
    def consumer: Rep[Option[String]] = column[Option[String]]("consumer")

    /** The consumerTime column */
    def consumerTime: Rep[Option[Long]] = column[Option[Long]]("consumerTime")

    /** The report column */
    def report: Rep[Option[String]] = column[Option[String]]("report")

    /** The reportTime column */
    def reportTime: Rep[Option[Long]] = column[Option[Long]]("reportTime")

    /** The ID column, which is the primary key, and auto incremented */
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, other parameters to the Person case classes
     * apply and unapply methods.
     */
    def * : ProvenShape[Person] = (
      username,
      firstName,
      lastName,
      month,
      dayOfMonth,
      year,
      emailAddress,
      address,
      city,
      state,
      zip,
      phone,
      ssn,
      customer,
      customerTime,
      consumer,
      consumerTime,
      report,
      reportTime,
      id) <> ((Person.apply _).tupled, Person.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val borrowers = TableQuery[BorrowersTable]

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(
      username: String, 
      firstName: String, 
      lastName: String, 
      month: Option[String],
      dayOfMonth: Option[String],
      year: Option[String],
      emailAddress: Option[String],
      address: Option[String],
      city: Option[String],
      state: Option[String],
      zip: Option[String],
      phone: Option[String],
      ssn: Option[String],
      customer: Option[String],
      customerTime: Option[Long],
      consumer: Option[String],
      consumerTime: Option[Long],
      report: Option[String],
      reportTime: Option[Long]): Future[Person] = db.run {
    (borrowers.map(b => (
        b.username, 
        b.firstName, 
        b.lastName, 
        b.month, 
        b.dayOfMonth, 
        b.year, 
        b.emailAddress, 
        b.address, 
        b.city, 
        b.state, 
        b.zip, 
        b.phone, 
        b.ssn, 
        b.customer, 
        b.customerTime, 
        b.consumer, 
        b.consumerTime, 
        b.report, 
        b.reportTime))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning borrowers.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((data, id) => Person(
        data._1,
        data._2,
        data._3,
        data._4,
        data._5,
        data._6,
        data._7,
        data._8,
        data._9,
        data._10,
        data._11,
        data._12,
        data._13,
        data._14,
        data._15,
        data._16,
        data._17,
        data._18,
        data._19,
        id))
          
    // And finally, insert the person into the database
    ) += (
      username,
      firstName,
      lastName,
      month,
      dayOfMonth,
      year,
      emailAddress,
      address,
      city,
      state,
      zip,
      phone,
      ssn,
      null,
      null,
      null,
      null,
      null,
      null)
  }

  /**
   * List all the borrowers in the database.
   */
  def list(): Future[Seq[Person]] = db.run {
    borrowers.result
  }

  /**
    * List the last borrower in the database.
    */
  def last(): Future[Option[Person]] = db.run {
//    borrowers.filter(_.id === 1L).result.headOption
    borrowers.sortBy(_.id.desc).result.headOption
  }

  /**
    * Patch the borrower with customer ID.
    */
  def patchCustomer(id: Long, customer: String, time: Long): Future[Int] = db.run {
    borrowers.filter(_.id === id).map(b => (b.customer, b.customerTime)).update(Some(customer), Some(time))
  }

  /**
    * Patch the borrower with customer ID.
    */
  def patchConsumer(customer: String, consumer: String, time: Long): Future[Int] = db.run {
    borrowers.filter(_.customer === customer).map(b => (b.consumer, b.consumerTime)).update(Some(consumer), Some(time))
  }

  /**
    * Patch the borrower with customer ID.
    */
  def patchReport(customer: String, report: String, time: Long): Future[Int] = db.run {
    borrowers.filter(_.customer === customer).map(b => (b.report, b.reportTime)).update(Some(report), Some(time))
  }

  /**
    * Does the borrower with this username exist?
    */
  def isBorrowerByUsername(username: String) : Future[Boolean] = db.run {
    borrowers.filter(_.username === username).exists.result
  }

  /**
    * Does the borrower with this customer ID exist?
    */
  def isBorrowerByCustomer(customer: String) : Future[Boolean] = db.run {
    borrowers.filter(_.customer === customer).exists.result
  }

  /**
    * Does the borrower with this consumer ID exist?
    */
  def isBorrowerByConsumer(consumer: String) : Future[Boolean] = db.run {
    borrowers.filter(_.consumer === consumer).exists.result
  }

  /**
    * Does the borrower with this consumer ID exist?
    */
  def isBorrowerByReport(report: String) : Future[Boolean] = db.run {
    borrowers.filter(_.report === report).exists.result
  }

  /**
    * Patch the borrower with customer ID.
    */
  def addNewCustomer(customer: Customer): Future[Int] = db.run {
    borrowers += Person(
      customer.username,
      customer.firstName,
      customer.lastName,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      Some(customer.id),
      Some(customer.createdDate.toLong),
      null,
      null,
      null,
      null)
    // http://books.underscore.io/essential-slick/essential-slick-3.html#moreControlOverInserts
    // Exception: selectExpression is null
//    val selectExpression = Query((
//      customer.username,
//      customer.firstName,
//      customer.lastName,
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      null: Option[String],
//      Some(customer.id): Option[String],
//      Some(customer.createdDate.toLong): Option[Long],
//      null: Option[String],
//      null: Option[Long],
//      null: Option[String],
//      null: Option[Long])).filterNot(_ => borrowers.filter(_.username === customer.username).exists)
//
//    borrowers.map(b => (
//      b.username,
//      b.firstName,
//      b.lastName,
//      b.month,
//      b.dayOfMonth,
//      b.year,
//      b.emailAddress,
//      b.address,
//      b.city,
//      b.state,
//      b.zip,
//      b.phone,
//      b.ssn,
//      b.customer,
//      b.customerTime,
//      b.consumer,
//      b.consumerTime,
//      b.report,
//      b.reportTime)).forceInsertQuery(selectExpression)
  }
}
