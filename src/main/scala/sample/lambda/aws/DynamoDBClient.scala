package sample.lambda.aws

import com.amazonaws.ClientConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.{PrimaryKey, DynamoDB}

import scala.collection.JavaConversions._

/**
  * Created by akihito on 2016/02/13.
  */

class DynamoDBClient(dynamodb: DynamoDB) {

  final val KEY = "#key"
  final val VALUE = ":value"

  def updateSummary(updateRequest: UpdateRequest) = {

    val table = dynamodb.getTable("sampleLambdaSummary")

    // FIXME: MapのキーとバリューのIterrableは両者間の順序の保証が無かった気がするのでこのままだと危ない。
    val keys = updateRequest.updates.keys
    val values = updateRequest.updates.values

    val keyMap = keys.zipWithIndex.map {
      case (key, index) => s"$KEY$index" -> key
    } toMap

    println(s"## keyMap $keyMap")

    val valueMap = values.zipWithIndex.map {
      case (value, index) => s"$VALUE$index" -> new Integer(value)
    } toMap

    println(s"## valueMap $valueMap")

    val tmpUpdateExpression =
      keyMap.keys zip valueMap.keys map {
        case (key, value) => s"$key $value"
      } mkString(",")

    val updateExpression = s"ADD $tmpUpdateExpression"
    println(s"## updateExpression : $updateExpression")

    val primaryKey = new PrimaryKey("id", updateRequest.id, "date", updateRequest.date)

    val outcome = table.updateItem(primaryKey,
                     updateExpression,
                     keyMap,
                     valueMap
                    )


    println(s"### result : ${outcome.getUpdateItemResult.getAttributes}")

  }

}

case class UpdateRequest(id: String, date: String, updates: Map[String, Int])



object DynamoDBClient {

  def apply(): DynamoDBClient = {
    val config = new ClientConfiguration()
      .withMaxErrorRetry(3)
      .withConnectionTimeout(1000)

    val client = new AmazonDynamoDBClient(config)
    client.withRegion(Regions.AP_NORTHEAST_1)
    val dynamoDB = new DynamoDB(client)

    new DynamoDBClient(dynamoDB)
  }
}
