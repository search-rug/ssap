package nl.rug.jbi.search.ssap.model

class Version(val order:Int, val name:String) {
  def toXml() = <version order={order.toString} name={name}/>
}

object Version {
  def fromXml(node: scala.xml.Node):Version =
    new Version((node \ "@order").text.toInt, (node \ "@name").text)
}
