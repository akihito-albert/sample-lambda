package sample.lambda

import java.util.Date

import com.amazonaws.services.dynamodbv2.model.{StreamRecord, AttributeValue}
import com.amazonaws.services.lambda.runtime.{LambdaLogger, Context}
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import sample.lambda.aws.{UpdateRequest, DynamoDBClient}
import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * あまり良くないコードなのであまり真似しないでください。
  */
class SampleLambda {

  def handler(event: DynamodbEvent, context: Context): Unit = {
    implicit val logger = context.getLogger
    logger.log(s"start : ${new Date().toString}")
    val dynamodb = DynamoDBClient()

    val client = context.getClientContext
    event.getRecords.foreach { record =>
      logger.log(record.getEventSource)
      logger.log(record.getEventSourceARN)
      val streamRecord = record.getDynamodb

      logger.log(s"Old Image: ${streamRecord.getOldImage}")
      logger.log(s"New Image: ${streamRecord.getNewImage}")

      val diff = (Option(streamRecord.getOldImage), Option(streamRecord.getNewImage)) match {
        case (None, Some(newImage))           => calcDiff(mutable.Map(), mapAsScala(newImage))
        case (Some(oldImage), Some(newImage)) => calcDiff(mapAsScala(oldImage), mapAsScala(newImage))
        case (Some(oldImage), None)           => calcDiff(mapAsScala(oldImage), mutable.Map())
        case _ => throw new IllegalStateException("oldImage or newImage must be not null.")
      }

      if (diff.nonEmpty) {
        val keys = streamRecord.getKeys
        val updateReq =
          UpdateRequest(getKeyOrFail(keys.get("id")), getKeyOrFail(keys.get("date")), diff)

        dynamodb.updateSummary(updateReq)
      }
    }

    logger.log(event.getRecords.toString)
    logger.log(s"end : ${new Date().toString}")
  }

  def calcDiff(oldImage: mutable.Map[String, AttributeValue], newImage: mutable.Map[String, AttributeValue])(implicit logger: LambdaLogger): Map[String, Int] = {

    newImage.keys.filterNot(Seq("id", "date").contains(_)) map { key =>
      (key, getIntOrZero(newImage.get(key)) - getIntOrZero(oldImage.get(key)))
    } toMap
  }

  def getIntOrZero(attrValueOpt: Option[AttributeValue])(implicit logger: LambdaLogger): Int = {
    attrValueOpt.getOrElse(new AttributeValue().withN("0")).getN.toInt
  }

  def getKeyOrFail(attrValue: AttributeValue)(implicit logger: LambdaLogger): String = {
    attrValue match {
      case attrValue: AttributeValue => attrValue.getS
      case null => throw new IllegalStateException("key attributeValue must NOT be empty.")
    }
  }

  implicit def mapAsScala[A, B](jMap: java.util.Map[A, B]): mutable.Map[A, B] = {
    jMap match {
      case null => mutable.Map[A, B]()
      case _ => mapAsScalaMap[A, B](jMap)
    }

  }

}
