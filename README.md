# Analysis of Max-Min Ant System with Local Search Applied to the Asymmetric and Dynamic Travelling Salesman Problem with Moving Vehicle

![Ant Colony Optimization](imgs/path.png)

This repository has the source code related with the paper ([link](https://link.springer.com/chapter/10.1007/978-3-030-34029-2_14)) presented on SEA^2 2019 conference ([link](https://www.springer.com/gp/book/9783030340285)).

SEA2 (Special Event on Analysis of Experimental Algorithms) is an international forum for researchers in the area of design, analysis, and experimental evaluation and engineering of algorithms, as well as in various aspects of computational optimization and its applications ([link](https://www.easychair.org/cfp/SEA2019)).

Paper abstract:
> Vehicle routing problems require efficient computational solutions to reduce operational costs. Therefore, this paper presents a benchmark analysis of Max-Min Ant System (MMAS) combined with local search applied to the Asymmetric and Dynamic Travelling Salesman Problem with Moving Vehicle (ADTSPMV). Different from the well known ADTSP, in the moving vehicle scenario the optimization algorithm continues to improve the TSP solution while the vehicle is visiting the clients. The challenge of this scenario is mainly concerned with the fulfilment of hard time restrictions. In this study we evaluate how MMAS performs combined with US local search, 3-opt local search, and a memory mechanism. Besides that, we demonstrate how to model the moving vehicle restrictions under the MMAS algorithm. To perform the benchmark analysis instances from TSBLIB were selected. The dynamism was emulated by means of changes in traffic factors. The results indicate that for ADTSP the MMAS-US is the best algorithm while for ADTSPMV the MMAS-3opt is the most suitable.

Project folder structure:
* src/main/java/com/github/schmittjoaopedro/tsp/algorithms - contains the algorithms evaluated in the paper.
* src/main/java/com/github/schmittjoaopedro/tsp/aco - contains the MMAX implementation.
 src/main/java/com/github/schmittjoaopedro/tsp/aco/ls - contains the local search implementations.
* src/main/resources/tsp - contains the benchmark test instances.
* src/test/java/com/github/schmittjoaopedro/tsp - contains the test cases used to validate the algorithm implementations.

To run this software be sure that Java and Maven are properly installed. After that, go to the project root folder and execute the following command:
> mvn test

This command will execute all teste cases.
If you want to edit the source code, is recomended to use Eclipse IDE or IntelliJ Community Edition.

## References:

Schmitt, J., Parpinelli, R., & Baldo, F. (2019). Analysis of Max-Min Ant System with Local Search Applied to the Asymmetric and Dynamic Travelling Salesman Problem with Moving Vehicle. Lecture Notes In Computer Science, 202-218. doi: 10.1007/978-3-030-34029-2_14