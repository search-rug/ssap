package nl.rug.jbi.search.ssap.model

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class System (val patterns: mutable.Buffer[Pattern]) {

  def toXml() = {
    <system>
      {patterns.map(_.toXml())}
    </system>
  }
}

object System {
  def fromXml(node: scala.xml.Node):System = {
    val s:System = new System(ArrayBuffer[Pattern]())
    (node \\ "pattern").map(p => s.patterns+=Pattern.fromXml(p))
    s
  }
}
