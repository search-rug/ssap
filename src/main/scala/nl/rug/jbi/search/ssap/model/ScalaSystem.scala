package nl.rug.jbi.search.ssap.model

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ScalaSystem(val patterns: mutable.Buffer[ScalaPattern]) {

  def toXml() = {
    <system>
      {patterns.map(_.toXml())}
    </system>
  }
}

object ScalaSystem {
  def fromXml(node: scala.xml.Node):ScalaSystem = {
    val s:ScalaSystem = new ScalaSystem(ArrayBuffer[ScalaPattern]())
    (node \\ "pattern").map(p => s.patterns+=ScalaPattern.fromXml(p))
    s
  }
}
