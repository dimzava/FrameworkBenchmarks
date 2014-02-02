package ratpack.benchmarks.techempower.groovy

import groovy.sql.Sql
import ratpack.benchmarks.techempower.common.World
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.test.ServerBackedApplicationUnderTest
import ratpack.test.remote.RemoteControl
import spock.lang.Shared

import static ratpack.benchmarks.techempower.common.World.DB_ROWS

class TechempowerBenchmarksSpec extends ratpack.benchmarks.techempower.test.TechempowerBenchmarkSpec {

  @Shared RemoteControl remote = new RemoteControl(aut)

  ServerBackedApplicationUnderTest createApplicationUnderTest() {
    new LocalScriptApplicationUnderTest('other.remoteControl.enabled': 'true')
  }

  def setupSpec() {
    remote.exec {
      Sql sql = get(Sql)
      sql.executeInsert("create table if not exists World (id int primary key auto_increment, randomNumber int)")
      sql.withBatch(5000) { stmt ->
        DB_ROWS.times {
          stmt.addBatch("insert into World (randomNumber) values (${World.randomId()})")
        }
      }
    }
  }

  def cleanupSpec() {
    remote.exec {
      get(Sql).execute("drop table World")
    }
  }

  def "single query test type fulfils requirements"() {
    when:
    get("db")

    then:
    def responseBody = response.asString()
    def responseClone = cloneResponse(response, responseBody)
    with(responseClone.jsonPath()) {
      getMap("").size() == 2
      getInt("id") >= 1 && getInt("id") <= DB_ROWS
      getInt("randomNumber") != null
    }
    assertResponseHeaders(responseClone, 'application/json')
  }
}