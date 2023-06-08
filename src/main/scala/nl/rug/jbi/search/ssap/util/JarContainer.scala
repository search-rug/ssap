package nl.rug.jbi.search.ssap.util

import java.io.{InputStream, File}
import java.util.jar.JarFile
import scala.collection.convert.wrapAsScala.enumerationAsScalaIterator

/**
  * Container for .class files in a .jar.
  *
  * @author Daniel Feitosa
  */
class JarContainer(project: File) extends ScalaProjectContainer(project) {
  private val jar = new JarFile(project)

  override def isValid = JarContainer.isValid(project)
  override def forEachClass[A](callback: (InputStream) => A) =
    jar.entries().foreach( e => callback(getClassStream(e.getName)) )
  override def getClassStream(classname: String) = jar.getInputStream(jar.getJarEntry(classname))
  override def close = jar.close
}

object JarContainer {
  final def isValid(project: File) = project.isFile && project.getName.matches("(?i).*\\.jar$")
}