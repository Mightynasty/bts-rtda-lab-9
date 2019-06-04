import com.holdenkarau.spark.testing.{DatasetSuiteBase}
import views.DeveloperOpenSourcePercentageView
import org.apache.spark.sql.{DataFrame, Dataset}
import org.scalatest.FunSuite


class SurveyProcessingTest extends FunSuite with DatasetSuiteBase {
  def readFromTestFile(): DataFrame ={
    val surveyDataFrame = spark.read.option("header", "true").csv("data/survey_results_public.csv")
    surveyDataFrame
  }

  test("Test functionality: how many programmers participated in the survey?"){
    val surveyDataFrame = readFromTestFile();
    val  expectedDeveloperCount: Long =  88883;
    val surveyProcessing: SurveyProcessing = new SurveyProcessing(surveyDataFrame);
    val realDeveloperCount: Long = surveyProcessing.developerCount();
    assert(realDeveloperCount == expectedDeveloperCount)
  }

  test("Test functionality: Percentage of developers how do open source") {
    import spark.implicits._
    val surveyDataFrame = readFromTestFile();
    val expectedOpenSourcePercentageSeq = Seq(
      DeveloperOpenSourcePercentageView(
        "Less than once a month but more than once per year", 20561, 23.13265753856193
      ),
      DeveloperOpenSourcePercentageView(
        "Once a month or more often", 11055, 12.437698997558588
      ),
      DeveloperOpenSourcePercentageView(
        "Never", 32295, 36.33428214619219
      ),
      DeveloperOpenSourcePercentageView(
        "Less than once per year", 24972, 28.095361317687296
      )
    )

    val expectedOpenSourcePercentageDS: Dataset[DeveloperOpenSourcePercentageView] =
      sc.parallelize(expectedOpenSourcePercentageSeq).toDS()

    val surveyProcessing: SurveyProcessing = new SurveyProcessing(surveyDataFrame);
    val realDevelopOpenSourcePercentage = surveyProcessing.createDeveloperOpenSourcePercentage()

    assertDatasetApproximateEquals(expectedOpenSourcePercentageDS, realDevelopOpenSourcePercentage, 0.01)
  }
}