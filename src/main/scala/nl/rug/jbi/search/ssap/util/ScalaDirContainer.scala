package nl.rug.jbi.search.ssap.util

import java.io.{File, FileInputStream, InputStream}
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{SuffixFileFilter, TrueFileFilter}

import scala.collection.convert.wrapAsScala.asScalaIterator
import scala.reflect.internal.util.FileUtils

/**
  * Container for .class files in a directory.
  *
  * @author Daniel Feitosa
  */
class ScalaDirContainer(project: File) extends ScalaProjectContainer(project) {

  override def isValid = ScalaDirContainer.isValid(project)
  override def forEachClass[A](callback: (InputStream) => A) =
    FileUtils.iterateFiles(project, new SuffixFileFilter(".class"), TrueFileFilter.TRUE).foreach(e => callback(new FileInputStream(e)) )
  override def getClassStream(classname: String) = {
    val suffix = classname.replaceAll("""\.""", "/") + ".class"
    new FileInputStream(FileUtils.listFiles(project, new ScalaPathSuffixFilter(suffix), TrueFileFilter.TRUE).iterator().next())
  }
  override def close = {}
}

object ScalaDirContainer {
  final def isValid(project: File) = project.isDirectory
}