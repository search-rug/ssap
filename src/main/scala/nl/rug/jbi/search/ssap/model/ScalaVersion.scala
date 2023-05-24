package nl.rug.jbi.search.ssap.model

class ScalaVersion(val order:Int, val name:String) {
  def toXml() = <version order={order.toString} name={name}/>
}

object ScalaVersion {
  def fromXml(node: scala.xml.Node):ScalaVersion =
    new ScalaVersion((node \ "@order").text.toInt, (node \ "@name").text)
}
