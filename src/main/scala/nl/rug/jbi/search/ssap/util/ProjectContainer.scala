package nl.rug.jbi.search.ssap.util

import java.io.{InputStream, File}

/**
  * Represents the container of a Java project, from which one can access the project's .class files.
  *
  * @author Daniel Feitosa
  */
abstract class ProjectContainer(val project: File) extends AutoCloseable {
  if(!isValid)
    throw new IllegalArgumentException(s"'$project' isn't a valid project")

  def isValid: Boolean
  def forEachClass[A](callback: (InputStream) => A)
  def getClassStream(classname: String): InputStream
}
