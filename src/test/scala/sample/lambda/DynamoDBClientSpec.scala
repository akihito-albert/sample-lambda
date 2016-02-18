package sample.lambda

import org.specs2.mutable.Specification
import sample.lambda.aws.{UpdateRequest, DynamoDBClient}


/**
  * Created by akihito on 2016/02/13.
  */
class DynamoDBClientSpec extends Specification {

  "DynamoDBClient" >> {
    "updateSummary should" >> {

      "updateSummary" >> {

        val client = DynamoDBClient()

        client.updateSummary(
          UpdateRequest("abc.123", "2016-02-13", Map("i-a" -> 1, "i-b" -> 3))
        )

        1 must be_==(1)
      }


    }


  }

}
