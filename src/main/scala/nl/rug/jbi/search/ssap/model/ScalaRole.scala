package nl.rug.jbi.search.ssap.model

case class ScalaRole(val element:String, val name:String) {
  def toXml() = <role element={element} name={name}/>
}

object ScalaRole {
  def fromXml(node: scala.xml.Node):ScalaRole =
    new ScalaRole((node \ "@element").text, (node \ "@name").text)
}
