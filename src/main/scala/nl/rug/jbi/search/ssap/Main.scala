package nl.rug.jbi.search.ssap

import java.io.File
import java.security.InvalidParameterException
import nl.rug.jbi.search.ssap.util.{ScalaProjectContainer, JarContainer, ScalaDirContainer, ProjectParser}
import org.slf4j.LoggerFactory
import scala.util.{Failure, Try}
import scala.xml.{PrettyPrinter, XML}

object Main extends App {

  val logger = LoggerFactory.getLogger("ssa-plus")

  var ssa: Try[model.ScalaSystem] = new Failure(new InvalidParameterException())
  var pc: Try[ScalaProjectContainer] = new Failure(new InvalidParameterException())
  var out: Option[File] = None

  lazy val parents = ProjectParser.getParentsMap(pc.get)
  lazy val xml = XML.loadString(new PrettyPrinter(400, 2).format(ssa.get.toXml()))

  val parser = new scopt.OptionParser[Unit]("java -jar ssap.jar") {
    head("ssap", "1.x")

    arg[File]("<ssa-file>")
      .foreach { x =>
        ssa = Try(model.ScalaSystem.fromXml(XML.loadFile(x)))
        out = Some(new File(x.getPath.replaceAll("\\.[^.]*$", "") + ".ssap.xml"))
      }
      .text("XML file from SSA tool")
      .validate { x => if (x.isFile) success else failure(s"'${x.getName}' doesn't exist")}
      .validate { x => if (x.getName.matches("(?i).*\\.xml$")) success else failure(s"'${x.getName}' isn't an XML file") }

    arg[File]("<project>")
      .foreach { x => pc = Try(if(JarContainer.isValid(x)) new JarContainer(x) else new ScalaDirContainer(x)) }
      .text("Folder or .jar containing the project's .class files")
      .validate { x => if (x.exists) success else failure(s"'${x.getName}' doesn't exist")}

    arg[File]("<output>")
      .optional()
      .foreach { x => out = Some(x) }
      .text("Output XML file")

    help("help") text "prints this usage text"

    checkConfig { c =>
      ssa match {
        case Failure(ex) => failure(s"Couldn't read SSA file: ${ex.getMessage}")
        case _ => success
      }
    }

    checkConfig { c =>
      pc match {
        case Failure(ex) => failure(s"${ex.getMessage}")
        case _ => success
      }
    }
  }

  if(parser.parse(args)){
    logger.info("Incrementing SSA data...")
    Incrementor.incrementPatternList(ssa.get, parents, pc.get)
    logger.info(s"Saving to ${out.get.getCanonicalPath}")
    XML.save(out.get.getCanonicalPath, xml, "UTF-8", true)
  }
}
