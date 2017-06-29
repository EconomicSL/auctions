package org.economicsl.auctions.singleunit.pricing

import org.economicsl.auctions.{ClearResult, SpotContract}
import org.economicsl.auctions.singleunit.orderbooks.FourHeapOrderBook
import org.economicsl.core.{Price, Tradable}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
sealed trait Pricing[T <: Tradable, A <: { def pricingPolicy: PricingPolicy[T]; def orderBook: FourHeapOrderBook[T] }] {

  def clear(a: A): ClearResult[A]

  protected def withOrderBook(auction: A, orderBook: FourHeapOrderBook[T]): A

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait DiscriminatoryPricing[T <: Tradable, A <: { def pricingPolicy: PricingPolicy[T]; def orderBook: FourHeapOrderBook[T] }]
  extends Pricing[T, A] {

  def clear(a: A): ClearResult[A] = {

    @annotation.tailrec
    def loop(pricingPolicy: PricingPolicy[T])(fills: Stream[SpotContract], ob: FourHeapOrderBook[T]): ClearResult[A] = {
      val currentPrice = pricingPolicy(ob)
      val (bestMatch, residualOrderBook) = ob.splitAtBestMatch
      bestMatch match {
        case Some((askOrder, bidOrder)) =>
          val fill = currentPrice.map(price => SpotContract.fromOrders(askOrder, bidOrder, price))
          loop(pricingPolicy)(fill.fold(fills)(_ #:: fills), residualOrderBook)
        case None =>
          val results = if (fills.nonEmpty) Some(fills) else None
          ClearResult(results, withOrderBook(a, ob))
      }
    }

    loop(a.pricingPolicy)(Stream.empty, a.orderBook)

  }

}


/**
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait UniformPricing[T <: Tradable, A <: { def pricingPolicy: PricingPolicy[T]; def orderBook: FourHeapOrderBook[T] }]
  extends Pricing[T, A] {

  def clear(a: A): ClearResult[A] = {
    val uniformPrice = a.pricingPolicy.apply(a.orderBook)
    uniformPrice match {
      case Some(price) =>
        val (fills, residualOrderBook) = accumulate(price)(Stream.empty, a.orderBook)
        val results = if (fills.nonEmpty) Some(fills) else None
        ClearResult(results, withOrderBook(a, residualOrderBook))
      case None => ClearResult(None, a)
    }
  }

  @annotation.tailrec
  private[this] def accumulate(price: Price)(fills: Stream[SpotContract], ob: FourHeapOrderBook[T]): (Stream[SpotContract], FourHeapOrderBook[T]) = {
    val (bestMatch, residualOrderBook) = ob.splitAtBestMatch
    bestMatch match {
      case Some((askOrder, bidOrder)) =>
        val fill = SpotContract.fromOrders(askOrder, bidOrder, price)
        accumulate(price)(fill #:: fills, residualOrderBook)
      case None =>
        (fills, ob)
    }
  }

}