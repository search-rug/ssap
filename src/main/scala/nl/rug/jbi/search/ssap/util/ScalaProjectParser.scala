package nl.rug.jbi.search.ssap.util

import java.io.{DataInputStream, InputStream}
import org.objectweb.asm.tree.{ClassNode, MethodNode}
import org.objectweb.asm.{ClassReader, Opcodes}
import scala.collection.convert.wrapAsScala.collectionAsScalaIterable
import scala.collection.mutable

/**
  * Utilities for Java projects ([[ScalaProjectContainer]]) (e.g., get methods from a given class).
  *
  * @author Daniel Feitosa
  */
object ScalaProjectParser {

  implicit class ImpClassNode(val cn: ClassNode) {
    def isInterface = (cn.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE
    def isAbstract = (cn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT
  }

  implicit class ImpMethodNode(val mn: MethodNode) {
    def isAbstract = (mn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT
  }

  private def readClassNode(classIS: InputStream): ClassNode = {
    val cn = new ClassNode()
    val cr = new ClassReader(new DataInputStream(classIS))
    cr.accept(cn, ClassReader.SKIP_DEBUG)
    cn
  }

  /**
    * Get a map of all classes to their respective parents (i.e., superclass + interfaces)
    *
    * @param pc Jar or directory containing all class files
    * @return Map of classes to parents
    */
  def getParentsMap(pc: ScalaProjectContainer) = {
    val map = new mutable.HashMap[String, Set[String]]
    pc.forEachClass(is => {
      val cn = readClassNode(is)
      val n = cn.name.replaceAll("/", ".")
      val p = List(cn.superName.replaceAll("/", ".")) ++:
        cn.interfaces.toArray.map(_.asInstanceOf[String].replaceAll("/", ".")).toSet
      map += (n -> p)
    })
    map.toMap
  }

  /**
    * Get all parents (incl. interfaces) of a class, recursively checking parents as well.
    *
    * @param classname Class to be processed
    * @param parents A map of all classes to their respective parents
    * @return Set of class names
    */
  def getAllSuperclasses(classname: String, parents: Map[String,Set[String]]): Set[String] ={
    val set = parents.getOrElse(classname,Set[String]())
    set ++: set.flatMap(getAllSuperclasses(_, parents)).toSet
  }

  /**
    * Get direct children of a given class
    *
    * @param classname Class to be processed
    * @param parents A map of all classes to their respective parents
    * @return List of classes
    */
  def getSubclasses(classname: String, parents: Map[String,Set[String]]) =
    parents.filter(_._2.contains(classname)).keySet

  /**
    * Get a list of all declared methods inside a class/interface
    *
    * @param pc Jar or directory containing all class files
    * @param classname Class to be processed
    * @return List of method names
    */
  def getMethodsFromClassFile(pc: ScalaProjectContainer, classname: String): List[String] =
    try {
      val cn = readClassNode(pc.getClassStream(classname))
      cn.methods.map(_.asInstanceOf[MethodNode].name).toList
    }catch {
      case _: Throwable => List[String]()
    }

  /**
    * Check if a class is implementing a given method. If not, it recursively checks all children, finding the first
    * implementation for each children. In the latter case, a list of classes is provided (i.e., one or more classes
    * per child).
    *
    * @param pc Jar or directory containing all class files
    * @param classname Class being tested
    * @param methodname Method to be searched
    * @param parents A map of all classes to their respective parents
    * @return Set of class names
    */
  def getFirstImplementations(pc: ScalaProjectContainer, classname: String, methodname: String, parents: Map[String,Set[String]]): Set[String] ={
    try {
      val cn = readClassNode(pc.getClassStream(classname))
      val mn = cn.methods.find(_.asInstanceOf[MethodNode].name == methodname).get.asInstanceOf[MethodNode]

      if(mn.isAbstract)
        getSubclasses(classname, parents).flatMap(getFirstImplementations(pc, _, methodname, parents))
      else
        Set(classname)
    }catch {
      case _: Throwable => Set[String]()
    }
  }

  /**
    * Check if a class isn't an interface. If it is, then recursively checks all children, finding the first non-
    * interface for each children. In the latter case, a list of classes is provided (i.e., one or more classes
    * per child).
    *
    * @param pc Jar or directory containing all class files
    * @param classname Class being tested
    * @param parents A map of all classes to their respective parents
    * @return Set of class names
    */
  def getFirstNonInterfaces(pc: ScalaProjectContainer, classname: String, parents: Map[String,Set[String]]): Set[String] ={
    try {
      val cn = readClassNode(pc.getClassStream(classname))

      if(cn.isInterface)
        getSubclasses(classname, parents).flatMap(getFirstNonInterfaces(pc, _, parents))
      else
        Set(classname)
    }catch {
      case _: Throwable => Set[String]()
    }
  }
}
