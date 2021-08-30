package integration

import hre.util.Verdict
import integration.helper.{IntegrationTestConfiguration, IntegrationTestHelper}
import org.scalatest.Ignore
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.tagobjects.Slow
import org.scalatest.time.{Millis, Span}



class BasicSlowTests extends AnyFlatSpec with TimeLimitedTests with Matchers {

  override def timeLimit: Span = Span(60000,Millis)

  it should "pass with silicon and examples/basic/AddAssignJava.java" taggedAs Slow in {
    val configuration = IntegrationTestConfiguration()
    configuration.files = Array("examples/basic/AddAssignJava.java")
    configuration.verdict = Verdict.Pass
    configuration.toolSilicon = true
    IntegrationTestHelper.test(configuration)
  }

  it should "pass with silicon and examples/basic/array-item-access.pvl" taggedAs Slow in {
    val configuration = IntegrationTestConfiguration()
    configuration.files = Array("examples/basic/array-item-access.pvl")
    configuration.verdict = Verdict.Pass
    configuration.toolSilicon = true
    IntegrationTestHelper.test(configuration)
  }

  it should "pass with silicon and examples/basic/pvl-array.pvl" taggedAs Slow in {
    val configuration = IntegrationTestConfiguration()
    configuration.files = Array("examples/basic/pvl-array.pvl")
    configuration.verdict = Verdict.Pass
    configuration.toolSilicon = true
    IntegrationTestHelper.test(configuration)
  }

  it should "pass with silicon and examples/basic/sumints.pvl" taggedAs Slow in {
    val configuration = IntegrationTestConfiguration()
    configuration.files = Array("examples/basic/sumints.pvl")
    configuration.verdict = Verdict.Pass
    configuration.toolSilicon = true
    IntegrationTestHelper.test(configuration)
  }


}
