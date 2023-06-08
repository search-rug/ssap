package nl.rug.jbi.search.ssap

import nl.rug.jbi.search.ssap.model._
import nl.rug.jbi.search.ssap.util.{ScalaProjectContainer, ProjectParser}
import org.slf4j.LoggerFactory
import scala.util.matching.Regex

object Incrementor {

  val logger = LoggerFactory.getLogger(Incrementor.getClass)

  private final val ElementRegex: Regex = """(.*)::(.*):(.*)""".r
  private final val ElementRegex_NoReturn: Regex = """(.*)::(.*)""".r
  private final val MethodRegex: Regex = """(.*)(\(.*\))""".r

  /**
   * Increments a provided list of pattern instances by adding extra information regarding instances elements.
   * The additions are described in the the document 'SSA+.md'
   *
   * @param s List of patterns to be updated
   * @param parents Map of system classes to their parents
   */
  def incrementPatternList(s: ScalaSystem, parents: Map[String,Set[String]], pc: ScalaProjectContainer) ={
    s.patterns.foreach(p => {
      p.name match {
        case "Factory Method"  => p.instances.foreach(i => incrementFactoryMethod(i,parents))
        case "Prototype"       => p.instances.foreach(i => incrementPrototype(i,parents,pc))
        //case "Singleton"     => //Nothing to be done
        //case "(Object)Adapter-Command" => //Nothing to be done
        case "Composite"       => p.instances.foreach(i => incrementComposite(i,parents,pc))
        case "Decorator"       => p.instances.foreach(i => incrementDecorator(i,parents,pc))
        case "Observer"        => p.instances.foreach(i => incrementObserver(i,parents,pc))
        case "State-Strategy"  => p.instances.foreach(i => incrementStateStrategy(i,parents,pc))
        case "Template Method" => p.instances.foreach(i => incrementTemplateMethod(i,parents,pc))
        //case "Visitor"       => //TODO (not necessary for the study)
        case "Proxy"           => p.instances.foreach(i => incrementProxy(i,parents,pc))
        case "Proxy2"          => p.instances.foreach(i => incrementProxy(i,parents,pc))
        case _ =>
      }
    })
  }

  /** Updates an instance of Factory Method by adding ConcreteCreator's and Product's. */
  def incrementFactoryMethod(i: ScalaInstance, parents: Map[String,Set[String]]) = {
    i.roles.filter(_.name == "Creator").foreach(r => {
      parents.view.filter(_._2.contains(r.element)).foreach(s => i.roles+= new ScalaRole(s._1,"ConcreteCreator"))
    })

    i.roles.filter(_.name == "FactoryMethod()").foreach(r => {
      val ElementRegex(cName,mName,rName) = r.element
      i.roles+= new ScalaRole(rName,"Product")
    })
  }

  /** Updates an instance of Prototype by adding ConcretePrototype's. */
  def incrementPrototype(i: ScalaInstance, parents: Map[String,Set[String]], pc: ScalaProjectContainer) = {
    i.roles.filter(_.name == "Prototype").foreach(r =>
      parents.filter(_._2.contains(r.element)).flatMap(s =>
        ProjectParser.getFirstNonInterfaces(pc,s._1,parents)
      ).toList.distinct.foreach(cc => i.roles+= new ScalaRole(cc,"ConcretePrototype"))
    )

  }

  /** Updates an instance of Composite by adding Leaf's. */
  def incrementComposite(i: ScalaInstance, parents: Map[String,Set[String]], pc: ScalaProjectContainer) = {
    val component = i.roles.find(_.name == "Component").getOrElse(new ScalaRole("","")).element

    val methods:List[String] = i.roles.filter(_.name == "Operation()").map(r => {
      val ElementRegex(cName,mSign,rName) = r.element
      val MethodRegex(mName,mAttrs) = mSign
      mName
    }).toList

    var candidates = methods.flatMap(ProjectParser.getFirstImplementations(pc, component, _, parents)).distinct
    if (candidates.isEmpty)
      candidates = parents.filter(_._2.contains(component)).flatMap(s =>
          ProjectParser.getFirstNonInterfaces(pc,s._1,parents)
        ).toList.distinct

    val composites = i.roles.filter(_.name == "Composite").map(_.element)

    candidates.foreach(c =>
      if (!composites.contains(c))
        i.roles+= new ScalaRole(c,"Leaf")
    )

  }

  /** Updates an instance of Decorator by adding ConcreteDecorator's and ConcreteComponent's. */
  def incrementDecorator(i: ScalaInstance, parents: Map[String,Set[String]], pc: ScalaProjectContainer) = {
    val component = i.roles.find(_.name == "Component").getOrElse(new ScalaRole("","")).element

    val methods:List[String] = i.roles.filter(_.name == "Operation()").map(r => {
      val ElementRegex(cName,mSign,rName) = r.element
      val MethodRegex(mName,mAttrs) = mSign
      mName
    }).toList

    var candidates = methods.flatMap(ProjectParser.getFirstImplementations(pc, component, _, parents)).distinct
    if (candidates.isEmpty)
      candidates = parents.filter(_._2.contains(component)).flatMap(s =>
        ProjectParser.getFirstNonInterfaces(pc,s._1,parents)
      ).toList.distinct

    val decorators = i.roles.filter(_.name == "Decorator").map(_.element)

    decorators.foreach(d =>
      parents.filter(_._2.contains(d)).flatMap(s =>
        ProjectParser.getFirstNonInterfaces(pc,s._1,parents)
      ).toList.distinct.foreach(cc => i.roles+= new ScalaRole(cc,"ConcreteDecorator"))
    )

    val concrDecorators = i.roles.filter(_.name == "ConcreteDecorator").map(_.element)

    candidates.foreach(c =>
      if (!decorators.contains(c) && !concrDecorators.contains(c))
        i.roles+= new ScalaRole(c,"ConcreteComponent")
    )
  }

  /** Updates an instance of Observer by adding ConcreteObserver's. */
  def incrementObserver(i: ScalaInstance, parents: Map[String,Set[String]], pc: ScalaProjectContainer) = {
    i.roles.filter(_.name == "Observer").foreach(r =>
      parents.filter(_._2.contains(r.element)).flatMap(s =>
        ProjectParser.getFirstNonInterfaces(pc,s._1,parents)
      ).toList.distinct.foreach(cc => i.roles+= new ScalaRole(cc,"ConcreteObserver"))
    )

  }

  /** Updates an instance of State/Strategy by adding ConcreteState/Strategy's. */
  def incrementStateStrategy(i: ScalaInstance, parents: Map[String,Set[String]], pc: ScalaProjectContainer) = {
    i.roles.filter(_.name == "State/Strategy").foreach(r =>
      parents.filter(_._2.contains(r.element)).flatMap(s =>
        ProjectParser.getFirstNonInterfaces(pc,s._1,parents)
      ).toList.distinct.foreach(cc => i.roles+= new ScalaRole(cc,"ConcreteState/Strategy"))
    )

  }

  /** Updates an instance of Template Method by adding ConcreteClass's. */
  def incrementTemplateMethod(i: ScalaInstance, parents: Map[String,Set[String]], pc: ScalaProjectContainer) = {
    i.roles.filter(_.name == "AbstractClass").foreach(r =>
      parents.filter(_._2.contains(r.element)).flatMap(s =>
        ProjectParser.getFirstNonInterfaces(pc,s._1,parents)
      ).toList.distinct.foreach(cc => i.roles+= new ScalaRole(cc,"ConcreteClass"))
    )
  }

  /** Updates an instance of Proxy by adding Subject. */
  def incrementProxy(i: ScalaInstance, parents: Map[String,Set[String]], pc: ScalaProjectContainer) = {
    val proxy = i.roles.find(_.name == "Proxy").getOrElse(new ScalaRole("","")).element
    val realSubject = i.roles.find(_.name == "RealSubject").getOrElse(new ScalaRole("","")).element
    val proxyParents = ProjectParser.getAllSuperclasses(proxy, parents)
    val rsParents = ProjectParser.getAllSuperclasses(realSubject, parents)
    val ElementRegex(cName,mSign,rName) = i.roles.find(_.name == "Request()").getOrElse(new ScalaRole("","::():")).element
    val MethodRegex(mName,mAttrs) = mSign

    proxyParents.intersect(rsParents).foreach(s =>
      if(parents.contains(s) && ProjectParser.getMethodsFromClassFile(pc,s).contains(mName))
          i.roles+= new ScalaRole(s,"Subject")
    )
  }
}
