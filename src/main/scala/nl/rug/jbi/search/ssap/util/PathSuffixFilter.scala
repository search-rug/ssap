package nl.rug.jbi.search.ssap.util

import java.io.File
import org.apache.commons.io.filefilter.AbstractFileFilter

class PathSuffixFilter(suffix: String) extends AbstractFileFilter {
  override def accept(file: File): Boolean = file.getCanonicalPath.endsWith(suffix)
}
