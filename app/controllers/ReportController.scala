package controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}
import services.ReportService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ReportController @Inject()(
                                  reportService: ReportService,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def patchReportsByCustomer(customer: String): Action[AnyContent] = Action.async { implicit request =>
    Future{
      val report = Await.result(reportService.patchReportsByCustomer(customer), Duration.Inf)
      if (report != None) Ok(report.get.id)
      else Ok("No such report")
    }
  }

  def patchReports: Action[AnyContent] = Action.async { implicit request =>
    Future{
      Ok(Await.result(reportService.patchReports, Duration.Inf))
    }
  }
}
