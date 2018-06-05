package services

import javax.inject._

trait DBConnectionService {
  def showDB(): String
  def createDB(): String
  def dropDB(): String
  def createToken(): String
  def createBorrowers(): String
}

@Singleton
class MySQLConnection extends DBConnectionService {
  Class.forName("com.mysql.jdbc.Driver").newInstance
  def showDB(): String = {
    val dbc = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/finicity?user=root&password=sa")
    val st = dbc.createStatement
    val rs = st.executeQuery("SHOW DATABASES")
    var result = "not found"
    if (rs.first) result = rs.getString(1)
    dbc.close
    result
  }

  def createDB(): String = {
    val dbc = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306?user=root&password=sa")
    val st = dbc.createStatement
    st.executeUpdate("CREATE DATABASE finicity")
    dbc.close
    "Database 'finicity' created"
  }

  def dropDB(): String = {
    val dbc = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/finicity?user=root&password=sa")
    val st = dbc.createStatement
    st.executeUpdate("DROP DATABASE finicity")
    dbc.close
    "Database 'finicity' dropped"
  }

  def createToken(): String = {
    val dbc = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/finicity?user=root&password=sa")
    val st = dbc.createStatement
    st.executeUpdate("CREATE TABLE token (token CHAR(20), time BIGINT(13))")
    dbc.close
    "Table  'token' created"
  }

  def createBorrowers(): String = {
    val dbc = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/finicity?user=root&password=sa")
    val st = dbc.createStatement
    st.executeUpdate(s"""CREATE TABLE borrowers (
            id INT(11) NOT NULL AUTO_INCREMENT,
            username VARCHAR(63),
            firstName VARCHAR(63),
            lastName VARCHAR(63),
            month CHAR(2),
            dayOfMonth CHAR(2),
            year CHAR(4),
            emailAddress VARCHAR(63),
            address VARCHAR(63),
            city VARCHAR(63),
            state CHAR(2),
            zip CHAR(5),
            phone CHAR(13),
            ssn CHAR(11),
            customer CHAR(8),
            customerTime BIGINT(13),
            consumer CHAR(32),
            consumerTime BIGINT(13),
            report CHAR(13),
            reportTime BIGINT(13),
            PRIMARY KEY (id))""")
    dbc.close
    "Table  'borrowers' created"
  }
}
