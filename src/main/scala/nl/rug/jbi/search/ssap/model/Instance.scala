package nl.rug.jbi.search.ssap.model

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.xml.Node

class Instance(val roles: mutable.Buffer[Role], val id:Long, val versions: mutable.Buffer[Version]) {

  def toXml() = {
    <instance>
      {versions.map(_.toXml())}
      {roles.map(_.toXml())}
    </instance>
  }

  override def equals(o: Any) = o match {
    case that: Instance => that.roles.forall(r => roles.contains(r)) && that.roles.size == roles.size
    case that: Node =>
        val roleSeq = that \\ "role"
        roleSeq.forall(r => roles.contains(Role.fromXml(r))) && roleSeq.size == roles.size
    case _ => false
  }
  override def hashCode = roles.hashCode()
}

object Instance {
  private var currId:Long = -1

  def resetId() = currId = -1

  def fromXml(node: scala.xml.Node):Instance = {
    currId+=1
    val i:Instance = new Instance(ArrayBuffer[Role](), currId, ArrayBuffer[Version]())
    (node \\ "role").map(r => i.roles+=Role.fromXml(r))
    (node \\ "version").map(v => i.versions+=Version.fromXml(v))
    i
  }
}
