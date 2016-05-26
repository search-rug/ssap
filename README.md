SSA+
========
This tool extend the capabilities of the GoF pattern detection tool developed by Tsantalis et al. {1} (hereby referred as [SSA][1]), extracting extended pattern participating (PP) classes.

According to Aversano et al. {2}, PP classes are subdivided into two categories: (a) main PP classes, comprising those that provide the structure of the pattern solution (commonly abstract classes); and (b) extended PP classes, which are subclasses of the former that extend the functionality of the pattern solution.

SSA+ takes as input the output of SSA, and is able to identify 10 extra roles based on the information provided by SSA for each pattern occurrence. The extra roles are:
* Concrete Creator and Product, for Factory Method pattern;
* Concrete Prototype, for Prototype pattern;
* Leaf, for Composite pattern;
* Concrete Decorator and Concrete Component, for Decorator pattern;
* Concrete Observer, for Observer pattern;
* Concrete State/Strategy, for State/Strategy pattern;
* Concrete Class, for Template Method pattern; and
* Subject, for Proxy pattern.

#### References

{1} N. Tsantalis, A. Chatzigeorgiou, G. Stephanides, and S. T. Halkidis, “Design pattern detection using similarity scoring,” Softw. Eng. IEEE Trans., vol. 32, no. 11, pp. 896–909, 2006.

{2} L. Aversano, L. Cerulo, and M. Di Penta, “Relationship between design patterns defects and crosscutting concern scattering degree: an empirical study,” IET Softw., vol. 3, no. 5, p. 395, 2009.

[1]: http://users.encs.concordia.ca/~nikolaos/pattern_detection.html
