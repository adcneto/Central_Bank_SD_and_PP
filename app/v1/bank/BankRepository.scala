package v1.bank

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class BankData(id: BankId, name: String, location: String)

class BankId private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object BankId {
  def apply(raw: String): BankId = {
    require(raw != null)
    new BankId(Integer.parseInt(raw))
  }
}


class BankExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the BankRepository.
  */
trait BankRepository {
  def create(data: BankData)(implicit mc: MarkerContext): Future[BankId]

  def list()(implicit mc: MarkerContext): Future[Iterable[BankData]]

  def get(id: BankId)(implicit mc: MarkerContext): Future[Option[BankData]]
}

/**
  * A trivial implementation for the Bank Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class BankRepositoryImpl @Inject()()(implicit ec: BankExecutionContext) extends BankRepository {

  private val logger = Logger(this.getClass)

  private val bankList = List(
    BankData(BankId("1"), "Bradesco", "192.168.10.1"),
    BankData(BankId("2"), "Santander", "192.168.10.2"),
    BankData(BankId("3"), "Itau", "192.168.10.3"),
    BankData(BankId("4"), "Banco do Brasil", "192.168.10.4"),
    BankData(BankId("5"), "Safra", "192.168.10.5")
  )

  override def list()(implicit mc: MarkerContext): Future[Iterable[BankData]] = {
    Future {
      logger.trace(s"list: ")
      bankList
    }
  }

  override def get(id: BankId)(implicit mc: MarkerContext): Future[Option[BankData]] = {
    Future {
      logger.trace(s"get: id = $id")
      bankList.find(bank => bank.id == id)
    }
  }

  def create(data: BankData)(implicit mc: MarkerContext): Future[BankId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}