package controllers

import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index())
  }

  def search(query: String) = Action {
    Async {
      WS.url("http://localhost:9001/search?query=" + query).get().map { response =>
        response.status match {
          case 200 => {
            val results = (response.json \ "results").as[Seq[JsObject]]
            Ok(results.map(_ \ "description").mkString("Found results: ", ", ", ".\n"))
          }
          case _ => InternalServerError(s"Error calling search service.\nResponse status ${response.status}\n")
        }
      }
    }
  }

  val paymentForm = Form("amount" -> number)

  def payments = Action { implicit request =>
    val amount = paymentForm.bindFromRequest.get
    Async {
      WS.url("http://localhost:9002/payments").post(Json.toJson(Map("amount" -> amount))).map { response =>
        response.status match {
          case 200 => Ok("Payment processed.\n")
          case _ => InternalServerError(s"Error calling payment service.\nResponse status ${response.status}\n")
        }
      }
    }
  }
}
