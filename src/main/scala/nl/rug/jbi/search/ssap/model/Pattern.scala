package nl.rug.jbi.search.ssap.model

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Pattern(val name: String, val instances: mutable.Buffer[Instance]) {

  def toXml() = {
    <pattern name={name}>
      {instances.map(_.toXml())}
    </pattern>
  }

  override def equals(o: Any) = o match {
    case that: Pattern => that.name == name
    case _ => false
  }
  override def hashCode = name.hashCode()
}

object Pattern {
  def fromXml(node: scala.xml.Node):Pattern = {
    val p:Pattern = new Pattern((node \ "@name").text, ArrayBuffer[Instance]())
    Instance.resetId()
    (node \\ "instance").map(i => p.instances+=Instance.fromXml(i))
    p
  }

  def getGroup(name:String):String = name match {
    case "Factory Method" => "Creational"
    case "Prototype" => "Creational"
    case "Singleton" => "Creational"
    case "(Object)Adapter-Command" => "Structural-Behavioral"
    case "Composite" => "Structural"
    case "Decorator" => "Structural"
    case "Observer"  => "Behavioral"
    case "State-Strategy"  => "Behavioral"
    case "Template Method" => "Behavioral"
    case "Visitor"   => "Behavioral"
    case "Proxy"     => "Structural"
    case "Proxy2"    => "Structural"
    case _ => ""
  }
}
