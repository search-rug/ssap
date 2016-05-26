package nl.rug.jbi.search.ssap.model

case class Role (val element:String, val name:String) {
  def toXml() = <role element={element} name={name}/>
}

object Role {
  def fromXml(node: scala.xml.Node):Role =
    new Role((node \ "@element").text, (node \ "@name").text)
}
