package nl.rug.jbi.search.ssap.model

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ScalaPattern(val name: String, val instances: mutable.Buffer[ScalaInstance]) {

  def toXml() = {
    <pattern name={name}>
      {instances.map(_.toXml())}
    </pattern>
  }

  override def equals(o: Any) = o match {
    case that: ScalaPattern => that.name == name
    case _ => false
  }
  override def hashCode = name.hashCode()
}

object ScalaPattern {
  def fromXml(node: scala.xml.Node):ScalaPattern = {
    val p:ScalaPattern = new ScalaPattern((node \ "@name").text, ArrayBuffer[ScalaInstance]())
    ScalaInstance.resetId()
    (node \\ "instance").map(i => p.instances+=ScalaInstance.fromXml(i))
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
